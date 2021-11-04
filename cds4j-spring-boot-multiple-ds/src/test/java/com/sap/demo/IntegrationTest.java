package com.sap.demo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        "/dbPrimary/schema.sql" }, config = @SqlConfig(encoding = "utf-8", dataSource = "primaryDS", transactionMode = TransactionMode.ISOLATED))
@Sql(scripts = {
        "/dbSecondary/schema.sql" }, config = @SqlConfig(encoding = "utf-8", dataSource = "secondaryDS", transactionManager = "secondaryTx", transactionMode = TransactionMode.ISOLATED))
public class IntegrationTest {

    @Autowired
    CatalogService catService;

    private List<Map<String, Object>> booksData;
    private List<Map<String, Object>> authorsData;
    private String expectedBooks;
    private String expectedAuthors;

    @Before
    public void init() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        booksData = books();
        authorsData = authors();

        expectedBooks = mapper.writeValueAsString(booksData);
        expectedAuthors = mapper.writeValueAsString(authorsData);
    }

    @Test
    public void testWriteReadDataFrom2DataSources() {
        catService.saveBooks(booksData);       // Write books payload to data source 1
        catService.saveAuthors(authorsData);   // Write authors payload to data source 2

        Result allBooks = catService.readAllBooks();
        Result allAuthors = catService.readAllAuthors();

        Assertions.assertThat(allBooks.toJson()).isEqualTo(expectedBooks);
        Assertions.assertThat(allAuthors.toJson()).isEqualTo(expectedAuthors);
    }

    private List<Map<String, Object>> books() {
        Map<String, Object> book1 = new HashMap<>();
        book1.put("id", 1);
        book1.put("title", "Tom Sawyer");

        Map<String, Object> book2 = new HashMap<>();
        book2.put("id", 2);
        book2.put("title", "Jane Eyre");

        return Arrays.asList(book1, book2);
    }

    private List<Map<String, Object>> authors() {
        Map<String, Object> author1 = new HashMap<>();
        author1.put("id", 1);
        author1.put("name", "Mark Twain");

        Map<String, Object> author2 = new HashMap<>();
        author2.put("id", 2);
        author2.put("name", "Charlotte Bronte");

        return Arrays.asList(author1, author2);
    }
}
