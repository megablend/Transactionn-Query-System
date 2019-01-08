package com.nibss.tqs.config.repository;

import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.ebillspay.repositories.BankRepository;
import com.nibss.tqs.merchantpayment.MerchantPaymentAggregatorRepository;
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

/**
 * Created by eoriarewo on 8/16/2016.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = {MerchantPaymentAggregatorRepository.class},
        entityManagerFactoryRef = "merchantPaymentEMF", transactionManagerRef = "merchantPaymentTransactionManager")
public class MerchantPaymentRepositoryConfig {

    @Autowired
    private ApplicationSettings appSettings;

    @Bean(destroyMethod = "close")
    public DataSource merchantPaymentDS() {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName(appSettings.merchantPaymentDataSourceClassName());
        config.setUsername(appSettings.merchantPaymentDbUsername());
        config.setPassword(appSettings.merchantPaymentDbPassword());
        config.setMaximumPoolSize(appSettings.merchantPaymentMaxPoolSize());
        config.addDataSourceProperty("url", appSettings.merchantPaymentDbUrl());
        config.setConnectionTimeout(ApplicationSettings.CONNECTION_TIMEOUT);
        return new HikariDataSource(config);
    }

    @Bean(name = "merchantPaymentEMF")
    public EntityManagerFactory merchantPaymentEMF() {
        HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
        vendor.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "update");
        vendor.getJpaPropertyMap().put("hibernate.ejb.entitymanager_factory_name", "mcash");
        vendor.setGenerateDdl(true);
        vendor.setDatabasePlatform(appSettings.ebillsDbPlatform());
        vendor.setShowSql(false);

        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(merchantPaymentDS());
        bean.setJpaVendorAdapter(vendor);
        bean.setPackagesToScan("com.nibss.merchantpay.entity");
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Bean(name = "merchantPaymentTransactionManager")
    public PlatformTransactionManager merchantPaymentTransactionManager() {
        JpaTransactionManager trxnMgr = new JpaTransactionManager(merchantPaymentEMF());
        return trxnMgr;
    }
}
