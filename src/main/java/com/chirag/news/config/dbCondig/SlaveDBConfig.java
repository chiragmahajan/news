package com.chirag.news.config.dbCondig;

import com.chirag.news.config.appConfig.DBConfig;
import com.chirag.news.constants.Constants;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "slaveEntityManager",transactionManagerRef = Constants.SLAVE_TRANSACTION_MANAGER)
public class SlaveDBConfig {

    @Autowired
    private DBConfig dbConfig;

    @Bean
    public LocalContainerEntityManagerFactoryBean slaveEntityManager() {
        LocalContainerEntityManagerFactoryBean em= new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(slaveDataSource());
        em.setPackagesToScan(new String[] { "com.chirag.news.model.entity" });
        em.setPersistenceUnitName(Constants.SLAVE_PERSISTENCE_UNIT_NAME);
        HibernateJpaVendorAdapter vendorAdapter= new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto",dbConfig.getSlaveHibernateDdlAuto());
        properties.put("hibernate.dialect",dbConfig.getSlaveHibernateDilect());
        properties.put("hibernate.show_sql",dbConfig.isSlaveHibernateShowSql());
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean
    public DataSource slaveDataSource() {
        HikariConfig poolConfig = new HikariConfig();
        poolConfig.setPoolName(dbConfig.getSlavePoolName());
        poolConfig.setJdbcUrl(dbConfig.getSlaveUrl());
        poolConfig.setUsername(dbConfig.getSlaveUsername());
        poolConfig.setPassword(dbConfig.getSlavePassword());
        poolConfig.setMaximumPoolSize(dbConfig.getSlaveMaxPoolSize());
        poolConfig.setMinimumIdle(dbConfig.getSlaveMinIdle());
        poolConfig.setConnectionTimeout(dbConfig.getSlaveConnectionTimeout());
        poolConfig.setIdleTimeout(dbConfig.getSlaveIdleConnTimeout());
        return new HikariDataSource(poolConfig);
    }

    @Bean
    public PlatformTransactionManager slaveTransactionManager() {
        JpaTransactionManager transactionManager= new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(slaveEntityManager().getObject());
        return transactionManager;
    }
}

