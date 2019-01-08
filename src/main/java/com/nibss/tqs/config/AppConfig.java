package com.nibss.tqs.config;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import com.nibss.cryptography.AESKeyGenerator;
import com.nibss.cryptography.KeyLength;
import com.nibss.util.converters.JAXBConverter;
import com.nibss.util.converters.XmlConverter;
import freemarker.template.TemplateExceptionHandler;
import org.aeonbits.owner.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AppConfig {
	
	@Bean
	public ApplicationSettings appSettings() {
		ApplicationSettings appSettings = ConfigFactory.create(ApplicationSettings.class);
		return appSettings;
	}
	

	@Autowired
	private Environment environment;
	
	@Bean(name = "tqsExecutorService", destroyMethod = "shutdown")
	public ExecutorService tqsExecutorService() {
		ExecutorService executorService = Executors.newFixedThreadPool(appSettings().appThreadPoolSize());
		return executorService;
//		return ForkJoinPool.commonPool();
	}

	@Bean(name = "freeMarkerConfig")
	public freemarker.template.Configuration freeMarkerConfig() throws IOException{
		freemarker.template.Configuration cfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_23);
		cfg.setDirectoryForTemplateLoading(appSettings().freemarkerTemplateDirectory());
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(true);

		return  cfg;
	}

	@Bean
	public JavaMailSenderImpl mailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		mailSender.setPassword(environment.getProperty("spring.mail.password"));
		mailSender.setHost(environment.getProperty("spring.mail.host"));
		mailSender.setProtocol(environment.getProperty("spring.mail.protocol"));
		mailSender.setUsername(environment.getProperty("spring.mail.username"));
		mailSender.setPort(Integer.parseInt(environment.getProperty("spring.mail.port", "587")));

		Properties props = new Properties();
		props.put("mail.smtp.auth", environment.getProperty("spring.mail.properties.mail.smtp.auth","true"));
		props.put("mail.smtp.debug", environment.getProperty("mail.smtp.debug","false"));
		props.put("mail.smtp.starttls.enable", environment.getProperty("spring.mail.properties.starttls.enable"));
		props.put("mail.transport.protocol", environment.getProperty("spring.mail.properties.mail.transport.protocol"));
		props.put("mail.smtp.from", environment.getProperty("spring.mail.properties.mail.smtp.from"));

		mailSender.setJavaMailProperties(props);
		return mailSender;
	}


}
