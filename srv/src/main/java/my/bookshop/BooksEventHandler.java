package my.bookshop;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.cds.ql.cqn.CqnInsert;
import com.sap.cds.ql.cqn.CqnStatement;
import com.sap.cds.services.cds.CdsCreateEventContext;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cds.gen.catalogservice.Books_;
import cds.gen.catalogservice.CatalogService_;

@Component
@ServiceName(value = CatalogService_.CDS_NAME)
public class BooksEventHandler implements EventHandler {

    private final static Logger logger = LoggerFactory.getLogger(BooksEventHandler.class);

    @Autowired
    DataCollectorListener dataCollector;

    @On(entity = Books_.CDS_NAME)
    public void createBook(CdsCreateEventContext context) {
        context.getChangeSetContext().register(dataCollector);

        CqnInsert insert = context.getCqn();
        logger.info("Processing statement: " + insert);
        List<Map<String, Object>> entries = insert.entries();
        dataCollector.add(entries);

        context.setResult(entries);
    }
}
