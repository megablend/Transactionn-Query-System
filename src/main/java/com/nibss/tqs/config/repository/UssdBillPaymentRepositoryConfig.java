package com.nibss.tqs.config.repository;

import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.merchantpayment.MerchantPaymentAggregatorRepository;
import com.nibss.tqs.ussd.repositories.UssdTransactionRepository;
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
 * Created by eoriarewo on 8/19/2016.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = {UssdTransactionRepository.class},
        entityManagerFactoryRef = "ussdBillPaymentEMF", transactionManagerRef = "ussdBillPaymentTransactionManager")
public class UssdBillPaymentRepositoryConfig {

    @Autowired
    private ApplicationSettings appSettings;

    @Bean(destroyMethod="close")
    public DataSource ussdPaymentDS() {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName( appSettings.ussdDataSourceClassName());
        config.setUsername(appSettings.ussdDbUsername());
        config.setPassword( appSettings.ussdDbPassword());
        config.setMaximumPoolSize(appSettings.ussdMaxPoolSize());
        config.addDataSourceProperty("url", appSettings.ussdDbUrl());
        config.setConnectionTimeout(ApplicationSettings.CONNECTION_TIMEOUT);
        return new HikariDataSource(config);
    }

    @Bean(name="ussdBillPaymentEMF")
    public EntityManagerFactory ussdBillPaymentEMF() {
        HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
        vendor.getJpaPropertyMap().put("hibernate.hbm2ddl.auto","update");
         vendor.getJpaPropertyMap().put("hibernate.ejb.entitymanager_factory_name","billpayment");
        vendor.setGenerateDdl(true);
        vendor.setShowSql(false);
        vendor.setDatabasePlatform(appSettings.ussdDbPlatform());


        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(ussdPaymentDS());
        bean.setJpaVendorAdapter(vendor);
        bean.setPackagesToScan("com.nibss.tqs.ussd.dto");
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Bean(name="ussdBillPaymentTransactionManager")
    public PlatformTransactionManager ussdBillPaymentTransactionManager() {
        JpaTransactionManager trxnMgr = new JpaTransactionManager(ussdBillPaymentEMF());
        return trxnMgr;
    }
}
