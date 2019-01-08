package com.nibss.tqs.config.repository;

import com.nibss.tqs.config.ApplicationSettings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * Created by Emor on 7/2/16.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories( basePackages = "com.nibss.tqs.core.repositories",
entityManagerFactoryRef = "tqsEMF",transactionManagerRef = "tqsTransactionManager")
public class TqsRepositoryConfig {

    @Autowired
    private ApplicationSettings appSettings;

    @Bean(destroyMethod="close")
    @Primary
    public DataSource tqsDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName( appSettings.appDataSourceClassName());
        config.setUsername(appSettings.appDbUsername());
        config.setPassword( appSettings.appDbPassword());
        config.setMaximumPoolSize(appSettings.appMaxPoolSize());
        config.addDataSourceProperty("url", appSettings.appDbUrl());
        config.setConnectionTimeout(ApplicationSettings.CONNECTION_TIMEOUT);
        return new HikariDataSource(config);
    }

    @Bean
    @Primary
    public EntityManagerFactory tqsEMF() {
        HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
        vendor.setGenerateDdl(true);
        vendor.getJpaPropertyMap().put("hibernate.hbm2ddl.auto","update");
        vendor.getJpaPropertyMap().put("hibernate.ejb.entitymanager_factory_name","tqs");
        vendor.setShowSql(false);
        vendor.setDatabasePlatform(appSettings.appDbPlatform());


        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(tqsDataSource());
        bean.setJpaVendorAdapter(vendor);
        bean.setPackagesToScan("com.nibss.tqs.core.entities");
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Bean
    @Primary
    public PlatformTransactionManager tqsTransactionManager() {
        JpaTransactionManager trxnMgr = new JpaTransactionManager(tqsEMF());
        return trxnMgr;
    }
}
