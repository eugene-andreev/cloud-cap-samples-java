/**************************************************************************
 * (C) 2019-2021 SAP SE or an SAP affiliate company. All rights reserved. *
 **************************************************************************/
package my.bookshop.utils;

import static com.sap.cds.impl.builder.model.ElementRefImpl.element;
import static com.sap.cds.util.CdsModelUtils.entity;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.sap.cds.impl.parser.builder.SortSpecBuilder;
import com.sap.cds.ql.CQL;
import com.sap.cds.ql.ElementRef;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Value;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.ql.cqn.CqnSelectListItem;
import com.sap.cds.ql.cqn.CqnSelectListValue;
import com.sap.cds.ql.cqn.CqnSortSpecification;
import com.sap.cds.ql.cqn.CqnValidationException;
import com.sap.cds.ql.impl.SelectListValueBuilder;
import com.sap.cds.reflect.CdsAnnotatable;
import com.sap.cds.reflect.CdsAnnotation;
import com.sap.cds.reflect.CdsElement;
import com.sap.cds.reflect.CdsEntity;
import com.sap.cds.reflect.CdsModel;

public class AggregateTransformation {

	private static final String AGGREGATION_DEFAULT = "Aggregation.default";
	private static final String SUPPORTED_RESTRICTIONS = "Aggregation.ApplySupported.PropertyRestrictions";

	private static final String AVERAGE = "AVERAGE";
	private static final String COUNT_DISTINCT = "COUNT_DISTINCT";
	private static final List<String> ALLOWED_AGGREGATES = Arrays.asList("SUM", "MAX", "MIN", "AVG");

	private final CdsModel model;
	private CdsEntity targetEntity;

	private final List<CqnSelectListItem> dimensions = new ArrayList<CqnSelectListItem>();
	private final List<CqnSelectListItem> selectListItems = new ArrayList<CqnSelectListItem>();

	private AggregateTransformation(CdsModel model) {
		this.model = model;
	}

	public static AggregateTransformation create(CdsModel model) {
		return new AggregateTransformation(model);
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
		if (isAggregateEntity()) {
			select.items().forEach(this::processSelectListItem);
			Select<?> copy = Select.from(select.ref()).columns(selectListItems).groupBy(dimensions)
					.orderBy(getOrderBy(select));
			select.where().ifPresent((w) -> copy.where(w));
			return copy;
		}
		return select;
	}

	private void processSelectListItem(CqnSelectListItem item) {
		String itemName = item.asValue().displayName();
		CqnSelectListValue sli = SelectListValueBuilder.select(itemName).build();
		CdsElement element = targetEntity.getElement(itemName);

		if (isMeasure(element)) {
			// If SLI is measure -> transform it to corresponding aggregate function
			selectListItems.add(asAggregateFunction(element).as(itemName));
		} else {
			selectListItems.add(sli);
			// otherwise assume SLI is a dimension -> group by
			dimensions.add(sli);
		}
	}

	private Value<?> asAggregateFunction(CdsElement element) {
		Value<?> functionCall;
		ElementRef<Object> elementRef = element(element.getName());
		String aggregateFunctionName = getAggregation(element);

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

	/**
	 * Return true if entity is annotated with
	 * {@code @Aggregation.ApplySupported.PropertyRestrictions: true}
	 * 
	 * @return
	 */
	private boolean isAggregateEntity() {
		return getAnnotatedValue(targetEntity, SUPPORTED_RESTRICTIONS, false);
	}

	/**
	 * Returns true if select list item is annotated with
	 * {@code @Aggregation.default}
	 * 
	 * @param element
	 * @return true or false
	 */
	private boolean isMeasure(CdsElement element) {
		return null != getAnnotatedValue(element, AGGREGATION_DEFAULT, null);
	}

	private String getAggregation(CdsElement element) {
		Map<String, String> annotatedValue = getAnnotatedValue(element, AGGREGATION_DEFAULT, singletonMap("#", "#"));
		return annotatedValue.get("#");
	}

	private <T> T getAnnotatedValue(CdsAnnotatable annotatable, String annotation, T fallBackValue) {
		try {
			return annotatable.<T>findAnnotation(annotation).map(CdsAnnotation::getValue).orElse(fallBackValue);
		} catch (ClassCastException ex) {
			throw new CqnValidationException("The type of annotation value for " + annotatable + " is not a "
					+ fallBackValue.getClass().getName(), ex);
		}
	}

	private List<CqnSortSpecification> getOrderBy(CqnSelect select) {
		return select.orderBy().stream().map(o -> getItem(select, o)).collect(toList());
	}

	private CqnSortSpecification getItem(CqnSelect select, CqnSortSpecification orderByItem) {
		return select.items().stream()
				.filter(item -> item.asValue().displayName().equals(orderByItem.item().displayName()))
				.map(item -> SortSpecBuilder.by(item.asValue()).order(orderByItem.order()).build()).findFirst()
				.orElseGet(() -> orderByItem);
	}
}
