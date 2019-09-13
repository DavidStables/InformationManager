package org.endeavourhealth.informationmanager.common.dal;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.cache.GenericCache;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.MetricsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionPool extends GenericCache<Connection> {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionPool.class);
    private static final int VALID_TIMEOUT = 5;
    private static ConnectionPool instance = null;

    public static ConnectionPool getInstance() {
        if (instance == null)
            instance = new ConnectionPool();

        return instance;
    }

    @Override
    protected boolean isValid(Connection connection) {
        try {
            if (connection == null)
                return false;

            if (connection.isValid(VALID_TIMEOUT)) {
                connection.setAutoCommit(true);
                return true;
            }

            decSize();

            if (!connection.isClosed())
                connection.close();

            return false;
        } catch (SQLException e) {
            LOG.error("Error validating/cleaning up connection", e);
            return false;
        }
    }

    @Override
    protected Connection create() {
        try {
            JsonNode json = ConfigManager.getConfigurationAsJson("database");
            String url = json.get("url").asText();
            String user = json.get("username").asText();
            String pass = json.get("password").asText();
            String driver = json.get("class") == null ? null : json.get("class").asText();

            if (driver != null && !driver.isEmpty())
                Class.forName(driver);

            Properties props = new Properties();

            props.setProperty("user", user);
            props.setProperty("password", pass);

            Connection connection = DriverManager.getConnection(url, props);    // NOSONAR

            LOG.trace("New DB Connection created");

            incSize();
            return connection;
        } catch (Exception e) {
            LOG.error("Error getting connection", e);
        }
        return null;
    }

    @Override
    public Connection pop() {
        Connection conn = super.pop();
        incUse();
        return conn;
    }

    @Override
    public boolean push(Connection conn) {
        decUse();
        return super.push(conn);
    }

    private void incSize() {
        MetricsHelper.recordCounter("ConnectionPool.Size").inc();
        LOG.trace("Size++ ={}\tUse   ={}\tPool   = {}",
            MetricsHelper.recordCounter("ConnectionPool.Size").getCount(),
            MetricsHelper.recordCounter("ConnectionPool.InUse").getCount(),
            this.getSize()
        );
    }

    private void decSize() {
        MetricsHelper.recordCounter("ConnectionPool.Size").dec();
        LOG.trace("Size-- ={}\tUse   ={}\tPool   = {}",
            MetricsHelper.recordCounter("ConnectionPool.Size").getCount(),
            MetricsHelper.recordCounter("ConnectionPool.InUse").getCount(),
            this.getSize()
        );
    }

    private void incUse() {
        MetricsHelper.recordCounter("ConnectionPool.InUse").inc();
        LOG.trace("Size   ={}\tUse++ ={}\tPool   = {}",
            MetricsHelper.recordCounter("ConnectionPool.Size").getCount(),
            MetricsHelper.recordCounter("ConnectionPool.InUse").getCount(),
            this.getSize()
        );
    }

    private void decUse() {
        MetricsHelper.recordCounter("ConnectionPool.InUse").dec();
        LOG.trace("Size   ={}\tUse-- ={}\tPool   = {}",
            MetricsHelper.recordCounter("ConnectionPool.Size").getCount(),
            MetricsHelper.recordCounter("ConnectionPool.InUse").getCount(),
            this.getSize()
        );
    }
}
