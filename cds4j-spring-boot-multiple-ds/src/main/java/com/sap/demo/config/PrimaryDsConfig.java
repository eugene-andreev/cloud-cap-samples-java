package com.sap.demo.config;

import java.io.IOException;
import java.sql.Connection;
import java.util.function.Supplier;

import javax.sql.DataSource;

import com.sap.cds.CdsDataStoreConnector;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PrimaryDsConfig extends AbstractConfig {

    @Primary
    @Bean(name = "primaryDS")
    @ConfigurationProperties(prefix = "primary.datasource")
    public DataSource ds() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager transactionManager(DataSource ds) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(ds);
        return dataSourceTransactionManager;
    }

    @Primary
    @Bean
    public CdsDataStoreConnector cdsDataStoreConnector(DataSource ds, CdsTransactionManager transactionManager)
            throws IOException {
        final Supplier<Connection> managedConnection = () -> wrapConnection(ds, DataSourceUtils.getConnection(ds));
        return CdsDataStoreConnector.createJdbcConnector(getCdsModel(), transactionManager)
                .connection(managedConnection).build();
    }

    @Override
    protected String getCsnPath() {
        return "dbPrimary/csn.json";
    }
}
