package com.sap.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.cds.CdsException;
import com.sap.cds.Result;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Sql(scripts = {
        "/dbPrimary/schema.sql" }, config = @SqlConfig(encoding = "utf-8", transactionMode = TransactionMode.ISOLATED))
@Sql(scripts = {
        "/dbSecondary/schema.sql" }, config = @SqlConfig(encoding = "utf-8", dataSource = "secondaryDS", transactionManager = "secondaryTx", transactionMode = TransactionMode.ISOLATED))
public class IntegrationTest {

    @Autowired
    CatalogService catService;

    private List<Map<String, Object>> booksData;
    private List<Map<String, Object>> authorsData;
    private String expectedBooksJson;
    private String expectedAuthorsJson;

    @Before
    public void init() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        booksData = DataProvider.books();
        authorsData = DataProvider.authors();

        expectedBooksJson = mapper.writeValueAsString(booksData);
        expectedAuthorsJson = mapper.writeValueAsString(authorsData);
    }

    @Test
    public void testWriteReadDataFrom2DataSources() {
        catService.saveBooks(booksData);
        catService.saveAuthors(authorsData);

        Result allBooks = catService.readAllBooks();
        Result allAuthors = catService.readAllAuthors();

        assertThat(allBooks.toJson()).isEqualTo(expectedBooksJson);
        assertThat(allAuthors.toJson()).isEqualTo(expectedAuthorsJson);
    }

    @Test
    public void testWriteDataToWrongDataSource() {
        Assertions.assertThatExceptionOfType(CdsException.class).isThrownBy(() -> catService.saveAuthors(booksData))
                .withMessage("Element 'title' does not exist in entity 'Author'");
    }
}
