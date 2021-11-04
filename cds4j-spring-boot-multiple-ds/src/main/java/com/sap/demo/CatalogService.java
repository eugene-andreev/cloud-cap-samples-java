package com.sap.demo;

import java.util.List;
import java.util.Map;

import com.sap.cds.CdsDataStore;
import com.sap.cds.CdsDataStoreConnector;
import com.sap.cds.Result;
import com.sap.cds.ql.Insert;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.cqn.CqnInsert;
import com.sap.cds.ql.cqn.CqnSelect;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CatalogService {

    private final CdsDataStore cdsStorePrimary;
    private final CdsDataStore cdsStoreSecondary;

    public CatalogService(CdsDataStoreConnector primaryConnector,
            @Qualifier("secondary") CdsDataStoreConnector secondaryConnector) {
        cdsStorePrimary = primaryConnector.connect();
        cdsStoreSecondary = secondaryConnector.connect();
    }

    @Transactional
    public void saveBooks(List<Map<String, Object>> books) {
        CqnInsert insert = Insert.into("Book").entries(books);
        cdsStorePrimary.execute(insert);
    }

    @Transactional
    public Result readAllBooks() {
        CqnSelect select = Select.from("Book");
        return cdsStorePrimary.execute(select);
    }

    @Transactional(transactionManager = "secondaryTx")
    public void saveAuthors(List<Map<String, Object>> authors) {
        CqnInsert insert = Insert.into("Author").entries(authors);
        cdsStoreSecondary.execute(insert);
    }

    @Transactional(transactionManager = "secondaryTx")
    public Result readAllAuthors() {
        CqnSelect select = Select.from("Author");
        return cdsStoreSecondary.execute(select);
    }
}
