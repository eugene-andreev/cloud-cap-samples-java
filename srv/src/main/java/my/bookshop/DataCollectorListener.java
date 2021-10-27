package my.bookshop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sap.cds.ql.Insert;
import com.sap.cds.services.changeset.ChangeSetListener;
import com.sap.cds.services.persistence.PersistenceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import cds.gen.catalogservice.CatalogService_;

@Component
@RequestScope
public class DataCollectorListener implements ChangeSetListener {

    private final static Logger logger = LoggerFactory.getLogger(DataCollectorListener.class);

    @Autowired
    PersistenceService db;

    private final List<List<Map<String, Object>>> payload = new ArrayList<>();

    public void add(List<Map<String, Object>> entries) {
        logger.info("Collecting data: " + entries);
        payload.add(entries);
    }

    @Override
    public void beforeClose() {
        logger.info("Executing bulk insert with payload: " + payload);
        List<Map<String, Object>> data = payload.stream().flatMap(s -> s.stream()).collect(Collectors.toList());
        db.run(Insert.into(CatalogService_.BOOKS).entries(data));
    }
}