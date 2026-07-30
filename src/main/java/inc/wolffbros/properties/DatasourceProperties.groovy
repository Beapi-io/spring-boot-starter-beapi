package io.beapi.api.properties;

import io.beapi.api.properties.yaml.factory.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@ConfigurationProperties(prefix = "db.datasource")
@PropertySource(value = "file:\${user.home}/.boot/\${spring.profiles.active}/beapi_db.yaml", factory = YamlPropertySourceFactory.class)
public class DatasourceProperties {

    public boolean jmxexport;
    public String driverclassname;
    public String dialect;
    public String username;
    public String password;
    public String url;
    public String dbCreate = "none";
    public DbProps properties = new DbProps();
    public Hibernate hibernate  = new Hibernate();


    void setJmxexport(boolean jmxExport) { this.jmxexport = jmxexport; }
    void setDriverclassname(String driverclassname) { this.driverclassname = driverclassname; }
    void setDialect(String dialect) { this.dialect = dialect; }
    void setUsername(String username) { this.username = username; }
    void setPassword(String password) { this.password = password; }
    void setUrl(String url) { this.url = url; }
    void setDbCreate(String dbCreate) { this.dbCreate = dbCreate; }
    void setProps(DbProps props) { this.properties = properties; }
    //void setHibernate(Hibernate hibernate) { this.hibernate = hibernate; }


    public boolean getJmxexport() { return jmxexport; }
    public String getDriverclassname() { return this.driverclassname; }
    public String getDialect() { return dialect; }
    public String getUsername() { return username; }
    public String getPassword() { return password;}
    public String getUrl() { return url; }
    public String getDbCreate() { return dbCreate; }
    public DbProps getProps() { return properties; }
    public Hibernate getHibernate() { return hibernate; }



    public static class DbProps {
        private boolean pooled = true;
        private boolean jmxEnabled = true;
        private int initialSize = 5;
        private int maxActive = 50;
        private int minIdle = 5;
        private int maxIdle = 25;
        private int maxWait = 10000;
        private int maxAge = 600000;
        private int timeBetweenEvictionRunsMillis = 5000;
        private int minEvictableIdleTimeMillis = 60000;
        private String validationQuery = "SELECT 1";
        private int validationQueryTimeout = 3;
        private int validationInterval = 15000;
        private boolean testOnBorrow = true;
        private boolean testWhileIdle = true;
        private boolean testOnReturn = false;
        private String jdbcInterceptors = "ConnectionState";
        // "java.sql.Connection.TRANSACTION_READ_COMMITTED";
        private int defaultTransactionIsolation = 2;

        void setPooled(boolean pooled) { this.pooled = pooled; }
        void setJmxEnabled(boolean jmxEnabled) { this.jmxEnabled = jmxEnabled; }
        void setInitialSize(int initialSize) { this.initialSize = initialSize; }
        void setMaxActive(int maxActive) { this.maxActive = maxActive; }
        void setMinIdle(int minIdle) { this.minIdle = minIdle; }
        void setMaxIdle(int maxIdle) { this.maxIdle = maxIdle; }
        void setMaxWait(int maxWait) { this.maxWait = maxWait; }
        void setMaxAge(int maxAge) { this.maxAge = maxAge; }
        void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) { this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis; }
        void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) { this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis; }
        void setValidationQuery(String validationQuery) { this.validationQuery = validationQuery; }
        void setValidationQueryTimeout(int validationQueryTimeout) { this.validationQueryTimeout = validationQueryTimeout; }
        void setValidationInterval(int validationInterval) { this.validationInterval = validationInterval; }
        void setTestOnBorrow(boolean testOnBorrow) { this.testOnBorrow = testOnBorrow; }
        void setTestWhileIdle(boolean testWhileIdle) { this.testWhileIdle = testWhileIdle; }
        void setTestOnReturn(boolean testOnReturn) { this.testOnReturn = testOnReturn; }
        void setJdbcInterceptors(String jdbcInterceptors) { this.jdbcInterceptors = jdbcInterceptors; }
        void setDefaultTransactionIsolation(int defaultTransactionIsolation) { this.defaultTransactionIsolation = defaultTransactionIsolation; }


        boolean getPooled() { return pooled; }
        boolean getJmxEnabled() { return jmxEnabled; }
        int getInitialSize() { return initialSize; }
        int getMaxActive() { return maxActive; }
        int getMinIdle() { return minIdle; }
        int getMaxIdle() { return maxIdle; }
        int getMaxWait() { return maxWait; }
        int getMaxAge() { return maxAge; }
        int getTimeBetweenEvictionRunsMillis() { return timeBetweenEvictionRunsMillis; }
        int getMinEvictableIdleTimeMillis() { return minEvictableIdleTimeMillis; }
        String getValidationQuery() { return validationQuery; }
        int getValidationQueryTimeout() { return validationQueryTimeout; }
        int getValidationInterval() { return validationInterval; }
        boolean getTestOnBorrow() { return testOnBorrow; }
        boolean getTestWhileIdle() { return testWhileIdle; }
        boolean getTestOnReturn() { return testOnReturn; }
        String getJdbcInterceptors() { return jdbcInterceptors; }
        int getDefaultTransactionIsolation() { return defaultTransactionIsolation; }
    }


    public static class Hibernate {
        private String dialect;
        private String showSql;

        public void setDialect(String dialect) { this.dialect = dialect; }
        public void setShowSql(String showSql) { this.showSql = showSql; }

        public String getDialect() { return dialect; }
        public String getShowSql() { return showSql; }
    }


    @Override
    public String toString() {
        return "YamlFooProperties{" +
                "username='" + username + '\'' +
                ", driverclassname=" + driverclassname +
                '}';
    }
}
