package my.bookshop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;

import com.sap.cds.CdsDataStore;
import com.sap.cds.Result;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.reflect.CdsModel;

import cds.gen.aggregationservice.AggregationService_;
import my.bookshop.utils.AggregateTransformer;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AggregateServiceTest {

	@Autowired
	CdsDataStore dataStore;

	@Autowired
	CdsModel model;

	@Test
	public void testTransformer() {
		CqnSelect select = Select.from(AggregationService_.BOOKS).columns( //
				c -> c.genreID(), // dimension
				c -> c.price(), // dimension
				c -> c.worstRating(), // MIN
				c -> c.bestRating(), // MAX
				c -> c.totalNicePrice() // CASE WHEN price < 14 THEN 1 ELSE 0
		).where(c -> c.genreID().gt(11)).orderBy(c -> c.genreID().desc(), c -> c.worstRating().asc());
		System.out.println("ORIGINAL    QUERY: " + select);

		select = AggregateTransformer.create(model).transform(select);
		System.out.println("TRANSFORMED QUERY: " + select);

		Result result = dataStore.execute(select);
		System.out.println(result);
	}

	@Test
	public void testTransformerStar() {
		CqnSelect select = Select.from(AggregationService_.BOOKS).columns(c -> c._all());
		System.out.println("ORIGINAL    QUERY: " + select);

		select = AggregateTransformer.create(model).transform(select);
		System.out.println("TRANSFORMED QUERY: " + select);

		Result result = dataStore.execute(select);
		System.out.println(result);
		Assert.isTrue(result.list().size() == 1, "Must be 1");
	}

	@Test
	public void testTransformerStarOrderBy() {
		CqnSelect select = Select.from(AggregationService_.BOOKS).columns(c -> c._all())
				.orderBy(c -> c.genreID().asc());
		System.out.println("ORIGINAL    QUERY: " + select);

		select = AggregateTransformer.create(model).transform(select);
		System.out.println("TRANSFORMED QUERY: " + select);

		Result result = dataStore.execute(select);
		System.out.println(result);
		Assert.isTrue(result.list().size() == 3, "Must be 3");
	}

	@Test
	public void testTransformerStarOrderAndGroupBy() {
		CqnSelect select = Select.from(AggregationService_.BOOKS).columns(c -> c._all()).orderBy(c -> c.genreID().asc())
				.groupBy(c -> c.price());
		System.out.println("ORIGINAL    QUERY: " + select);

		select = AggregateTransformer.create(model).transform(select);
		System.out.println("TRANSFORMED QUERY: " + select);

		Result result = dataStore.execute(select);
		System.out.println(result);
		Assert.isTrue(result.list().size() == 5, "Must be 5");
	}

}
