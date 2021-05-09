/**************************************************************************
 * (C) 2019-2021 SAP SE or an SAP affiliate company. All rights reserved. *
 **************************************************************************/
package my.bookshop.utils;

import static com.sap.cds.impl.builder.model.ElementRefImpl.element;
import static com.sap.cds.util.CdsModelUtils.entity;
import static java.util.Collections.singletonMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sap.cds.ql.CQL;
import com.sap.cds.ql.ElementRef;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Value;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.ql.cqn.CqnSelectListItem;
import com.sap.cds.ql.cqn.CqnSelectListValue;
import com.sap.cds.ql.cqn.CqnValidationException;
import com.sap.cds.ql.impl.SelectListValueBuilder;
import com.sap.cds.reflect.CdsAnnotatable;
import com.sap.cds.reflect.CdsAnnotation;
import com.sap.cds.reflect.CdsElement;
import com.sap.cds.reflect.CdsEntity;
import com.sap.cds.reflect.CdsModel;

/**
 * Utility class which transforms the select query to the aggregate query based
 * on the {@code @Aggregation.default} annotations in CDS Model.
 * 
 */
public class AggregateTransformer {

	private static final String AGGREGATION_DEFAULT = "Aggregation.default";
	private static final String SUPPORTED_RESTRICTIONS = "Aggregation.ApplySupported.PropertyRestrictions";

	private static final String AVERAGE = "AVERAGE";
	private static final String COUNT_DISTINCT = "COUNT_DISTINCT";
	private static final List<String> ALLOWED_AGGREGATES = Arrays.asList("SUM", "MAX", "MIN", "AVG");

	private final CdsModel model;
	private CdsEntity targetEntity;

	private final List<CqnSelectListItem> dimensions = new ArrayList<>();
	private final List<CqnSelectListItem> selectListItems = new ArrayList<>();

	private AggregateTransformer(CdsModel model) {
		this.model = model;
	}

	public static AggregateTransformer create(CdsModel model) {
		return new AggregateTransformer(model);
	}

	/**
	 * Transforms the the select statement to the corresponding aggregate query
	 * based on the @Aggregation.default annotation in CDS Model. If the statement
	 * cannot be transformed, the original statement is returned.
	 * 
	 * @return aggregate select or original statement
	 */
	public CqnSelect transform(CqnSelect select) {
		targetEntity = entity(model, select.ref());
		// Is entity annotated with '@Aggregation.ApplySupported.PropertyRestrictions'?
		boolean isAggregateEntity = getAnnotatedValue(targetEntity, SUPPORTED_RESTRICTIONS, false);
		if (isAggregateEntity) {
			dimensions.clear();
			selectListItems.clear();
			// Iterate through SLIs of original select to collect measures and dimensions
			select.items().forEach(this::processSelectListItem);
			// Build new select query based on measures and dimensions
			Select<?> copy = Select.from(select.ref()).columns(selectListItems).groupBy(dimensions)
					.orderBy(select.orderBy());
			select.where().ifPresent((w) -> copy.where(w));
			return copy;
		}
		return select;
	}

	private void processSelectListItem(CqnSelectListItem item) {
		String itemName = item.asValue().displayName();
		CqnSelectListValue sli = SelectListValueBuilder.select(itemName).build();
		CdsElement element = targetEntity.getElement(itemName);

		// True if select list item is annotated with '@Aggregation.default: ...'
		boolean isMeasure = null != getAnnotatedValue(element, AGGREGATION_DEFAULT, null);
		if (isMeasure) {
			// If SLI is measure -> transform it to corresponding aggregate function
			selectListItems.add(asAggregateFunction(element).as(itemName));
		} else {
			selectListItems.add(sli);
			// Otherwise assume SLI is a dimension -> group by
			dimensions.add(sli);
		}
	}

	private Value<?> asAggregateFunction(CdsElement element) {
		Value<?> functionCall;
		ElementRef<Object> elementRef = element(element.getName());
		String aggregateFunctionName = getAnnotatedValue(element, AGGREGATION_DEFAULT, singletonMap("#", "#")).get("#");

		if (ALLOWED_AGGREGATES.contains(aggregateFunctionName)) {
			functionCall = CQL.func(aggregateFunctionName, elementRef);
		} else if (COUNT_DISTINCT.equals(aggregateFunctionName)) {
			functionCall = CQL.countDistinct(elementRef);
		} else if (AVERAGE.equals(aggregateFunctionName)) {
			functionCall = CQL.average(elementRef);
		} else {
			throw new CqnValidationException(aggregateFunctionName + " is not supported");
		}
		return functionCall;
	}

	private <T> T getAnnotatedValue(CdsAnnotatable annotatable, String annotation, T fallBackValue) {
		try {
			return annotatable.<T>findAnnotation(annotation).map(CdsAnnotation::getValue).orElse(fallBackValue);
		} catch (ClassCastException ex) {
			throw new CqnValidationException("The type of annotation value for " + annotatable + " is not a "
					+ fallBackValue.getClass().getName(), ex);
		}
	}
}
