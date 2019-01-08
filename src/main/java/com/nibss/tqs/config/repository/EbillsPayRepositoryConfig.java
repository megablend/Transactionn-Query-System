package com.nibss.tqs.config.repository;

import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.ebillspay.repositories.BankRepository;
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

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by Emor on 7/2/16.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = {BankRepository.class},
        entityManagerFactoryRef = "ebillspayEMF", transactionManagerRef = "ebillspayTransactionManager")
public class EbillsPayRepositoryConfig {

    @Autowired
    private ApplicationSettings appSettings;

    @Bean(destroyMethod = "close")
    public DataSource ebillspayDS() {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName(appSettings.ebillsDataSourceClassName());
        config.setUsername(appSettings.ebillsDbUsername());
        config.setPassword(appSettings.ebillsDbPassword());
        config.setMaximumPoolSize(appSettings.ebillsMaxPoolSize());
        config.addDataSourceProperty("url", appSettings.ebillsDbUrl());
        config.setConnectionTimeout(ApplicationSettings.CONNECTION_TIMEOUT);
        return new HikariDataSource(config);
    }

    @Bean(name = "ebillspayEMF")
    public LocalContainerEntityManagerFactoryBean ebillspayEMF() {
        HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
        vendor.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "update");
        vendor.getJpaPropertyMap().put("hibernate.ejb.entitymanager_factory_name", "ebillspay");
        vendor.setGenerateDdl(true);
        vendor.setDatabasePlatform(appSettings.ebillsDbPlatform());
        vendor.setShowSql(false);

        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(ebillspayDS());
        bean.setJpaVendorAdapter(vendor);

        bean.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "update");
        bean.getJpaPropertyMap().put("hibernate.ejb.entitymanager_factory_name", "ebillspay");

        bean.setPackagesToScan("com.nibss.tqs.ebillspay.dto");
//        bean.afterPropertiesSet();
        return bean;
    }

    @Bean(name = "ebillspayTransactionManager")
    public PlatformTransactionManager ebillspayTransactionManager(@Qualifier("ebillspayEMF") EntityManagerFactory emf) {
        JpaTransactionManager trxnMgr = new JpaTransactionManager(emf);
        return trxnMgr;
    }
}
