package com.nibss.tqs.config.repository;

import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.corporatelounge.repositories.AccountRepository;
import com.nibss.tqs.corporatelounge.repositories.BankRepository;
import com.nibss.tqs.corporatelounge.repositories.ClientRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * Created by eoriarewo on 4/27/2017.
 */
@Configuration
@EnableJpaRepositories(basePackageClasses = {ClientRepository.class, AccountRepository.class, BankRepository.class},
        entityManagerFactoryRef = "clEMF", transactionManagerRef = "clTxnMgr")
public class CorporateLoungeRepositoryConfig {

    @Autowired
    private ApplicationSettings appSettings;

    @Value("${cl.hikari}")
    private String corpLoungeHikari;

    @Bean(destroyMethod = "close")
    public DataSource corporateLoungeDS() {
        HikariConfig config = new HikariConfig(corpLoungeHikari);
        return new HikariDataSource(config);
    }


    @Bean
    public EntityManagerFactory clEMF() {
        HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
        vendor.setGenerateDdl(true);
        vendor.setDatabasePlatform(appSettings.cpayDbPlatform());
        vendor.getJpaPropertyMap().put("hibernate.hbm2ddl.auto","update");
        vendor.getJpaPropertyMap().put("hibernate.ejb.entitymanager_factory_name","corplounge");
        vendor.getJpaPropertyMap().put("hibernate.generate_statistics","true");
        vendor.setShowSql(false);

        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.getJpaPropertyMap().put("hibernate.generate_statistics","true");
        bean.setDataSource(corporateLoungeDS());
        bean.setJpaVendorAdapter(vendor);
        bean.setPackagesToScan("com.nibss.corporatelounge.dto");
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Bean
    public PlatformTransactionManager clTxnMgr() {
        JpaTransactionManager trxnMgr = new JpaTransactionManager(clEMF());
        return trxnMgr;
    }
}
