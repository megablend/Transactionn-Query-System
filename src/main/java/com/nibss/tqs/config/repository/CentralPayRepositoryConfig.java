package com.nibss.tqs.config.repository;

import com.nibss.tqs.centralpay.repositories.CardTransactionRepository;
import com.nibss.tqs.config.ApplicationSettings;
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

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * Created by eoriarewo on 7/4/2016.
 */
@Configuration
@EnableJpaRepositories( basePackageClasses = CardTransactionRepository.class, entityManagerFactoryRef = "cpayEMF",
transactionManagerRef = "cpayTransactionManager")
public class CentralPayRepositoryConfig {
    @Autowired
    private ApplicationSettings appSettings;

    @Bean(destroyMethod="close")
    public DataSource cpayDS() {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName( appSettings.cpayDataSourceClassName());
        config.setUsername(appSettings.cpayDbUsername());
        config.setPassword( appSettings.cpayDbPassword());
        config.setMaximumPoolSize(appSettings.cpayMaxPoolSize());
        config.addDataSourceProperty("url", appSettings.cpayDbUrl());
        config.setConnectionTimeout(ApplicationSettings.CONNECTION_TIMEOUT);
        return new HikariDataSource(config);
    }

    @Bean(name="cpayEMF")
    public EntityManagerFactory cpayEMF() {
        HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
        vendor.setGenerateDdl(true);
        vendor.setDatabasePlatform(appSettings.cpayDbPlatform());
        vendor.getJpaPropertyMap().put("hibernate.hbm2ddl.auto","update");
         vendor.getJpaPropertyMap().put("hibernate.ejb.entitymanager_factory_name","cpay");
        vendor.setShowSql(false);

        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.getJpaPropertyMap().put("hibernate.generate_statistics","true");
        bean.setDataSource(cpayDS());
        bean.setJpaVendorAdapter(vendor);
        bean.setPackagesToScan("com.nibss.tqs.centralpay.dto");
        bean.afterPropertiesSet();
        return bean.getObject();
        
    }

    @Bean(name="cpayTransactionManager")
    public PlatformTransactionManager cpayTransactionManager() {
        JpaTransactionManager trxnMgr = new JpaTransactionManager(cpayEMF());
        return trxnMgr;
    }
}
