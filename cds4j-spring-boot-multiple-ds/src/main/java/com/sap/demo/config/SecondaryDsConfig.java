package com.sap.demo.config;

import java.io.IOException;
import java.sql.Connection;
import java.util.function.Supplier;

import javax.sql.DataSource;

import com.sap.cds.CdsDataStoreConnector;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SecondaryDsConfig extends AbstractConfig {

    @Bean(name = "secondaryDS")
    @ConfigurationProperties(prefix = "secondary.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean("secondaryTx")
    public PlatformTransactionManager transactionManager(@Qualifier("secondaryDS") DataSource ds) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(ds);
        return dataSourceTransactionManager;
    }

    @Bean("secondary")
    public CdsDataStoreConnector cdsDataStoreConnector(@Qualifier("secondaryDS") DataSource ds,
            CdsTransactionManager transactionManager) throws IOException {
        final Supplier<Connection> managedConnection = () -> wrapConnection(ds, DataSourceUtils.getConnection(ds));
        return CdsDataStoreConnector.createJdbcConnector(getCdsModel(), transactionManager)
                .connection(managedConnection).build();
    }

    @Override
    protected String getCsnPath() {
        return "dbSecondary/csn.json";
    }
}
