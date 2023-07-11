package io.beapi.api.config;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.orm.jpa.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import javax.persistence.*;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class DataSourceConfig {

    @Autowired
    private io.beapi.api.properties.DatasourceProperties datasourceProperties;

    @Resource
    @Bean(name = "dataSource")
    public DataSource getDataSource(){
        HikariConfig config = new HikariConfig();
        config.setPoolName("dbpool");
        config.setJdbcUrl(datasourceProperties.getUrl());
        config.setDriverClassName(datasourceProperties.getDriverclassname());
        config.setUsername(datasourceProperties.getUsername());
        config.setPassword(datasourceProperties.getPassword());
        config.setMinimumIdle(0);
        config.setConnectionTimeout(3000);
        config.setIdleTimeout(3500);
        config.setMaxLifetime(45000);
        config.setAutoCommit(true);
        config.setValidationTimeout(5000);
        config.setConnectionTestQuery("SELECT 1");
        config.setMaximumPoolSize(4);
        config.setAllowPoolSuspension(false);
        config.setReadOnly(false);
        config.setLeakDetectionThreshold(10000);
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        return new HikariDataSource(config);
    }


    @Bean(name = "entityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(){
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(getDataSource());
        entityManagerFactory.setPackagesToScan("demo.application.domain");
        entityManagerFactory.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        entityManagerFactory.setJpaProperties(getHibernateProperties());
        return entityManagerFactory;
    }

    private Properties getHibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", datasourceProperties.hibernate.getDialect());
        properties.put("hibernate.show_sql", datasourceProperties.hibernate.getShowSql());
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.max_fetch_depth",0);
        return properties;
    }

    @Bean(name = "sessionFactory")
    public LocalSessionFactoryBean getSessionFactory(DataSource datasource) throws Exception {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(datasource);
        sessionFactory.setHibernateProperties(getHibernateProperties());
        sessionFactory.setPackagesToScan("demo.application.domain");
        return sessionFactory;
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

/*
    @Bean(name = "transactionManager")
    public HibernateTransactionManager getTransactionManager(SessionFactory sessionFactory) {
        HibernateTransactionManager hibernateTransactionManager = new HibernateTransactionManager(sessionFactory);
        return hibernateTransactionManager;
    }
    */





}
