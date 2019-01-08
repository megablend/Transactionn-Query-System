package com.nibss.tqs.config;

import org.aeonbits.owner.Config;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Config.Sources({"classpath:application.properties","classpath:application-production.properties"})
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.HotReload(type = Config.HotReloadType.SYNC, unit = TimeUnit.HOURS, value = 5)
public interface ApplicationSettings  extends Config{

	int CONNECTION_TIMEOUT = 30 * 1000 * 10000;

	/* tqs db config settings */
	@Key("tqs.dataSourceClassName")
	String appDataSourceClassName();
	@Key("tqs.maxPoolSize")
	int appMaxPoolSize();
	@Key("tqs.dbUsername")
	String appDbUsername();
	@Key("tqs.dbPassword")
	String appDbPassword();
	@Key("tqs.dbUrl")
	String appDbUrl();
	
	@Key("tqs.dbPlatform")
	@DefaultValue("org.hibernate.dialect.MySQL5Dialect")
	String appDbPlatform();
	/* tqs db settings end here */
	
	@Key("tqs.threadPoolSize")
	int appThreadPoolSize();

	/*merchant payment DS */
	@Key("merchantpayment.dataSourceClassName")
	String merchantPaymentDataSourceClassName();

	@Key("merchantpayment.maxPoolSize")
	int merchantPaymentMaxPoolSize();

	@Key("merchantpayment.dbUsername")
	String merchantPaymentDbUsername();

	@Key("merchantpayment.dbPassword")
	String merchantPaymentDbPassword();

	@Key("merchantpayment.dbUrl")
	String merchantPaymentDbUrl();

	@Key("merchantpayment.dbPlatform")
	String merchantPaymentDbPlatform();


	/*
	eBillsPay DB settings
	 */
	@Key("ebillspay.dataSourceClassName")
	String ebillsDataSourceClassName();

	@Key("ebillspay.maxPoolSize")
	int ebillsMaxPoolSize();

	@Key("ebillspay.dbUsername")
	String ebillsDbUsername();

	@Key("ebillspay.dbPassword")
	String ebillsDbPassword();

	@Key("ebillspay.dbUrl")
	String ebillsDbUrl();

	@Key("ebillspay.dbPlatform")
	String ebillsDbPlatform();

	@Key("ebillspay.external_receipt_url")
	String ebillsPayExternalReceiptUrl();

	/*
	Cpay DB config
	 */
	@Key("cpay.dbPlatform")
	String cpayDbPlatform();
	@Key("cpay.dataSourceClassName")
	String cpayDataSourceClassName();
	@Key("cpay.maxPoolSize")
	int cpayMaxPoolSize();
	@Key("cpay.dbUsername")
	String cpayDbUsername();
	@Key("cpay.dbPassword")
	String cpayDbPassword();
	@Key("cpay.dbUrl")
	String cpayDbUrl();

	/*ussd bill payment DB config */
	@Key("ussd.dbPlatform")
	String ussdDbPlatform();
	@Key("ussd.dataSourceClassName")
	String ussdDataSourceClassName();
	@Key("ussd.maxPoolSize")
	int ussdMaxPoolSize();
	@Key("ussd.dbUsername")
	String ussdDbUsername();
	@Key("ussd.dbPassword")
	String ussdDbPassword();
	@Key("ussd.dbUrl")
	String ussdDbUrl();


	/*excluded user params */
	@Key("ebillspay.excluded_params")
	List<String> excludedUserParams();

	@Key("ebillspay.receipt_url")
	String ebillspayReceiptUrl();

	@Key("ebillspay.user_param_url")
	String ebillspayUserParamsUrl();

	@Key("ebillspay.aes_key")
	String ebillspayAesKey();

	@Key("activemq.url")
	String amqBrokerUrl();

	@Key("ussd.channel_code")
	int ussdChannelCode();

	@Key("ebillspay.SortCodeSuffix")
	@DefaultValue("150000")
	String ebillspaySortCodeSuffix();

	@Key("freemarker.template_directory")
	File freemarkerTemplateDirectory();

	@Key("application.url")
	String applicationUrl();

	@Key("mail.bccList")
	List<String> bccList();

	@Key("nibss.logo")
	File nibssLogo();


	@Key("billing.mailing_group")
	List<String> billingMailGroup();

	@Key("billing.payment_folder")
	String billingPaymentFolder();

	@Key("billing.report_folder")
	String billingReportFolder();

	/* merchant pay billing config */


	@Key("merchantpayment.narration")
	String merchantPaymentNarration();

	@Key("merchantpayment.payerName")
	String merchantPaymentPayerName();

	@Key("merchantpay.smartdet_code")
	String merchantPaySmarDetCode();


	/* ussd billing config */
	@Key("ussd_billing.smartdet_code")
	String ussdBillPaymentSmartdetCode();

	@Key("ussd_billing.narration")
	String ussdBillPaymentNarration();

	@Key("ussd_billing.payerName")
	String ussdBillPaymentPayerName();



	/* ebillspay billing config */
	@Key("ebillspay.smardet_code")
	String ebillspaySmartDetCode();

	@Key("ebillspay.commissionNarration")
	String ebillspayCommissionNarration();

	@Key("ebillspay.payer")
	String ebillspayPayer();

	@Key("nibss.ebillspay_code")
	String nibssEbillspayCode();

	@Key("cpay.smartdet_code")
	String cpaySmartDetCode();

	@Key("cpay.narration")
	String cpayNarration();

	@Key("cpay.payerName")
	String cpayPayerName();

	@Key("ebillspay.debit_narration")
	String debitNarration();
}
