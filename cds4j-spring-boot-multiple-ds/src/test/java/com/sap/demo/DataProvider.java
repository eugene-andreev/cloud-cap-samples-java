package com.sap.demo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataProvider {
    private DataProvider() {
    }

    public static List<Map<String, Object>> books() {
        Map<String, Object> book1 = new HashMap<>();
        book1.put("id", 1);
        book1.put("title", "Tom Sawyer");

        Map<String, Object> book2 = new HashMap<>();
        book2.put("id", 2);
        book2.put("title", "Jane Eyre");

        return Arrays.asList(book1, book2);
    }

    public static List<Map<String, Object>> authors() {
        Map<String, Object> author1 = new HashMap<>();
        author1.put("id", 1);
        author1.put("name", "Mark Twain");

        Map<String, Object> author2 = new HashMap<>();
        author2.put("id", 2);
        author2.put("name", "Charlotte Bronte");

        return Arrays.asList(author1, author2);
    }
}
