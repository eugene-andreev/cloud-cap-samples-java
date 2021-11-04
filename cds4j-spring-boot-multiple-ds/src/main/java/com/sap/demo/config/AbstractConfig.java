package com.sap.demo.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

import javax.sql.DataSource;

import com.sap.cds.reflect.CdsModel;
import com.sap.cds.reflect.impl.CdsModelReader;

import org.springframework.jdbc.datasource.DataSourceUtils;

public abstract class AbstractConfig {

    protected abstract String getCsnPath();

    protected CdsModel getCdsModel() throws IOException {
        String csnPath = getCsnPath();
        try (InputStream is = AbstractConfig.class.getClassLoader().getResourceAsStream(csnPath)) {
            if (is == null) {
                throw new IOException("Model " + csnPath + " not found");
            }
            return CdsModelReader.read(is);
        }
    }

    protected Connection wrapConnection(final DataSource dsx, final Connection connection) {
        final InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                if ("close".equals(method.getName())) {
                    DataSourceUtils.releaseConnection(connection, dsx);
                    return null;
                }
                return method.invoke(connection, args);
            }
        };

        return (Connection) Proxy.newProxyInstance(SecondaryDsConfig.class.getClassLoader(),
                new Class[] { Connection.class }, handler);
    }
}
