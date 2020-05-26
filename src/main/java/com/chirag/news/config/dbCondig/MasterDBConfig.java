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
@EnableJpaRepositories(entityManagerFactoryRef = "masterEntityManager",transactionManagerRef = Constants.MASTER_TRANSACTION_MANAGER)
public class MasterDBConfig {
    @Autowired
    private DBConfig dbConfig;

    @Bean
    public LocalContainerEntityManagerFactoryBean masterEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(masterDataSource());
        em.setPackagesToScan(new String[] { "com.chirag.news.model.entity"});
        em.setPersistenceUnitName(Constants.MASTER_PERSISTENCE_UNIT_NAME);
        HibernateJpaVendorAdapter vendorAdapter= new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto",dbConfig.getMasterHibernateDdlAuto());
        properties.put("hibernate.dialect",dbConfig.getMasterHibernateDilect());
        properties.put("hibernate.show_sql",dbConfig.isMasterHibernateShowSql());
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean
    public DataSource masterDataSource() {
        HikariConfig poolConfig = new HikariConfig();
        poolConfig.setPoolName(dbConfig.getMasterPoolName());
        poolConfig.setJdbcUrl(dbConfig.getMasterUrl());
        poolConfig.setUsername(dbConfig.getMasterUsername());
        poolConfig.setPassword(dbConfig.getMasterPassword());
        poolConfig.setDriverClassName(dbConfig.getMasterDriverClass());
        poolConfig.setMaximumPoolSize(dbConfig.getMasterMaxPoolSize());
        poolConfig.setMinimumIdle(dbConfig.getMasterMinIdle());
        poolConfig.setConnectionTimeout(dbConfig.getMasterConnectionTimeout());
        poolConfig.setIdleTimeout(dbConfig.getMasterIdleConnTimeout());
        return new HikariDataSource(poolConfig);
    }

    @Bean
    public PlatformTransactionManager masterTransactionManager() {
        JpaTransactionManager transactionManager= new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(masterEntityManager().getObject());
        return transactionManager;
    }
}

