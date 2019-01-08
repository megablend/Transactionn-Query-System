package com.nibss.tqs.report;

import com.nibss.corporatelounge.dto.Account;
import com.nibss.corporatelounge.dto.AccountBalance;
import com.nibss.merchantpay.entity.CreditTransaction;
import com.nibss.merchantpay.entity.DebitTransaction;
import com.nibss.merchantpay.entity.Merchant;
import com.nibss.merchantpay.entity.MerchantAccount;
import com.nibss.tqs.ajax.*;
import com.nibss.tqs.centralpay.dto.AccountTransaction;
import com.nibss.tqs.centralpay.dto.CardTransaction;
import com.nibss.tqs.ebillspay.dto.BaseTransaction;
import com.nibss.tqs.ussd.dto.UssdTransaction;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Stream;

/**
 * Created by eoriarewo on 9/6/2016.
 */
@Data
@NoArgsConstructor
public class TransactionReport implements Serializable {


    public static final String MERCHANT_NAME = "merchantName";
    public static final String AMOUNT = "amount";
    public static final String TELCO = "telco";
    public static final String STATUS = "status";
    public static final String SESSION_ID = "sessionId";
    public static final String TRANSACTION_REF = "transactionRef";
    public static final String MERCHANT_CODE = "merchantCode";
    public static final String CUSTOMER_NUMBER = "customerNumber";

    public static final String FEE = "fee";
    public static final String PAYMENT_REF = "paymentReference";
    public static final String TRANSACTION_DATE = "transactionDate";
    public static final String DATE_APPROVED = "dateApproved";
    public static final String PRODUCT = "product";
    public static final String GATEWAY = "gateway";
    public static final String SOURCE_BANK = "sourceBank";
    public static final String DESTINATION_BANK = "destinationBank";
    public static final String CALCULATED_AMOUNT = "calculatedAmount";

    public static final String USSD_AGGREGATOR = "ussdAggregator";
    public static final String CPAY_REF = "cpayRef";
    public static final String MERCHANT_REF = "merchantRef";

    public static final String PAYMENT_TYPE = "paymentType";

    public static final String SOURCE_BANK_CODE = "sourceBankCode";
    public static final String SOURCE_RESPONSE_DESCRIPTION = "sourceResponseDescription";
    public static final String DESTINATION_RESPONSE_DESCRIPTION = "destinationResponseDescription";

    public static final String TOTAL_AMOUNT = "totalAmount";

    public static final String PHONE_NUMBER = "phoneNumber";

    public static final String ACCOUNT_NUMBER = "accountNumber";

    public static final String ACCOUNT_NAME = "accountName";

    public static final String EMAIL = "email";


    public static final String LGA = "lga";

    public static final String STATE = "state";

    public static final String CREDIT_STATUS = "creditStatus";

    public static final String BRANCH_CODE = "branchCode";

    public static final String ORGANIZATION_NAME = "organizationName";


    public static final String REFERENCE_CODE = "referenceCode";


    private String merchantName;
    private String sessionId;
    private String paymentReference;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal calculatedAmount = BigDecimal.ZERO;
    private String sourceBank;
    private String sourceBankCode;

    private String telco;
    private String telcoCode;
    private String destinationBank;
    private String ussdAggregator;
    private String ussdAggregatorCode;
    private String aggregatorCode;
    private String cpayRef;
    private String destinationSessionId;
    private Number merchantId;
    private String transactionRef;
    private String paymentType;
    private String customerNumber;

    private String phoneNumber;


    private String destinationBankCode;
    private Date transactionDate;
    private Date dateApproved;
    private String product;

    private String status;

    private String gateway;
    private String merchantRef;
    private String sourceResponseDescription;

    private String merchantCode;

    private String destinationResponseDescription;

    private String email;
    private String lga;

    private String state;

    private String accountNumber;
    private String accountName;


    private BigDecimal totalAmount;

    private String creditStatus;

    private String branchCode;

    private String organizationName;

    private String referenceCode;


    //ebillspay txns
    public TransactionReport(final BaseTransaction bt) {
        merchantName = bt.getBiller().getName();
        sessionId = bt.getSessionId();
        amount = bt.getAmount();
        amount = amount == null ? BigDecimal.ZERO : amount;
        fee = bt.getTransactionFee();
        fee = fee == null ? BigDecimal.ZERO : fee;
        sourceBank = bt.getSourceBank() == null ? "" : bt.getSourceBank().getName();
        branchCode = bt.getBranchCode();
        destinationBank = bt.getDestinationBank() == null ? null : bt.getDestinationBank().getName();
        product = bt.getProduct() == null ? "" : bt.getProduct().getName();
        customerNumber = bt.getCustomerNumber();
        transactionDate = bt.getTransactionDate();
        dateApproved = bt.getDateApproved();

        status = bt.getResponseDescription();

        totalAmount = amount.add(fee);

    }


    //ebillspay txns
    public TransactionReport(final AjaxEbillsPayTransaction bt) {
        merchantName = bt.getBillerName();
        sessionId = bt.getSessionId();
        amount = bt.getAmount();
        amount = amount == null ? BigDecimal.ZERO : amount;
        fee = bt.getFee();
        fee = fee == null ? BigDecimal.ZERO : fee;
        sourceBank = bt.getSourceBankName() == null ? "" : bt.getSourceBankName();
        branchCode = bt.getBranchCode();
        destinationBank = bt.getDestinationBankName() == null ? null : bt.getDestinationBankName();
        product = bt.getProductName() == null ? "" : bt.getProductName();
        customerNumber = bt.getCustomerNumber();
        transactionDate = bt.getTransactionDate();
        dateApproved = bt.getDateApproved();

        status = bt.getResponseDescription();

        totalAmount = amount.add(fee);

    }

    public TransactionReport(final UssdTransaction ut) {
        merchantName = ut.getUssdBiller().getName();
        amount = ut.getAmount();
        fee = ut.getTransactionFee();
        telco = ut.getTelco().getName();
        sessionId = ut.getSourceSessionId();
        destinationSessionId = ut.getDestinationSessionId();
        ussdAggregator = ut.getUssdAggregator().getName();
        status = ut.getSourceResponseCode();
        sourceBank = ut.getSourceBankCode();
        merchantCode = ut.getUssdBiller().getMerchantCode();
        transactionRef = ut.getId();
        sourceResponseDescription = ut.getSourceResponseDescription();
        destinationResponseDescription = ut.getDestinationResponseDescription();
        phoneNumber = ut.getPhoneNumber() == null ? "" : ut.getPhoneNumber();
    }

    public TransactionReport(final AjaxUssdTransaction ut) {
        merchantName = ut.getMerchantName();
        amount = ut.getAmount();
        fee = ut.getFee();
        telco = ut.getTelcoName();
        sessionId = ut.getSessionId();
        destinationSessionId = ut.getDestinationSessionId();
        ussdAggregator = ut.getUssdAggregator();
        status = ut.getDebitResponseCode();
        sourceBank = ut.getSourceBankCode();
        sourceBankCode = ut.getSourceBankCode();
        merchantCode = ut.getMerchantCode();
        transactionRef = ut.getId();
        sourceResponseDescription = ut.getDebitResponseDescription();
        destinationResponseDescription = ut.getCreditResponseDescription();
        phoneNumber = ut.getPhoneNumber() == null ? "" : ut.getPhoneNumber();
        transactionDate = ut.getRequestTime();
    }


    public TransactionReport(final DebitTransaction dt) {
        merchantName = dt.getMerchant().getMerchantName();
        amount = dt.getAmount();
        CreditTransaction ct = dt.getCreditTransaction();
        fee = dt.getFee();

        if (null != ct) {
            fee = dt.getFee().add(ct.getFee() == null ? BigDecimal.ZERO : ct.getFee());
        }

        paymentReference = dt.getPaymentReference();
        sourceBankCode = dt.getInstitution().getBankCode();
        destinationBankCode = dt.getCreditTransaction().getInstitution().getBankCode();
        sourceBank = dt.getInstitution().getInstitutionName();
        telco = dt.getTelco().getTelcoName();
        telcoCode = dt.getTelco().getTelcoCode();
        ussdAggregator = dt.getAggregator().getAggregatorName();
        ussdAggregatorCode = dt.getAggregator().getAggregatorCode();
        sessionId = dt.getSessionID();
        merchantId = dt.getMerchant().getMerchantId();
        merchantCode = dt.getMerchant().getMerchantCode();
        transactionDate = dt.getTransactionDate();
        phoneNumber = dt.getPayerPhoneNumber();

        status = dt.getResponseCode();

        try {
            NipResponseCodes code = Arrays.stream(NipResponseCodes.values()).filter(x -> x.getResponseCode().equals(dt.getResponseCode())).findFirst().orElse(null);
            if (null != code)
                status = code.getResponseDesc();
        } catch (Exception e) {
        }

        creditStatus = "";

        if (dt.getCreditTransaction() != null && dt.getCreditTransaction().getResponseCode() != null) {
            String code = dt.getCreditTransaction().getResponseCode();

            creditStatus = Stream.of(NipResponseCodes.values()).filter(e -> e.getResponseCode().equals(code)).findFirst().orElse(NipResponseCodes.REQUEST_PROCESSING_IN_PROGRESS).getResponseDesc();
        }

        if (null != dt.getCreditTransaction() && null != dt.getCreditTransaction().getInstitution())
            destinationBank = dt.getCreditTransaction().getInstitution().getInstitutionName();

    }


    public TransactionReport(final AjaxMcashTransaction dt) {
        merchantName = dt.getMerchantName();
        amount = dt.getAmount();
        fee = dt.getFee();

        paymentReference = dt.getPaymentReference();
        sourceBank = dt.getSourceBankName();
        telco = dt.getTelcoName();
        ussdAggregator = dt.getUssdAggregator();
        sessionId = dt.getSessionId();
        merchantCode = dt.getMerchantCode();
        transactionDate = dt.getTransactionDate();
        phoneNumber = dt.getPhoneNumber();

        status = dt.getDebitResponseDescription();

        creditStatus = dt.getCreditResponseDescription();
        destinationBank = dt.getDestinationBankName();

        referenceCode = dt.getReferenceCode();
    }


    public TransactionReport(final CardTransaction ct) {
        merchantName = ct.getMerchant().getName();
        gateway = ct.getPaymentGateway() == null ? null : ct.getPaymentGateway().getName();
        merchantRef = ct.getMerchantRef();
        cpayRef = ct.getCpayRef();
        amount = ct.getAmount();
        product = ct.getProduct();
        customerNumber = ct.getCustomerId() == null ? "" : ct.getCustomerId().replace("+", " ");
        transactionDate = ct.getTransactionDate();
        dateApproved = ct.getDateApproved();
        status = ct.getResponseDescription();
    }


    public TransactionReport(final AjaxCpayCardTransaction ct) {
        merchantName = ct.getMerchantName();
        gateway = ct.getPaymentGateway() == null ? null : ct.getPaymentGateway();
        merchantRef = ct.getMerchantRef();
        cpayRef = ct.getCpayRef();
        amount = ct.getAmount();
        product = ct.getProductName();
        customerNumber = ct.getCustomerId() == null ? "" : ct.getCustomerId().replace("+", " ");
        transactionDate = ct.getTransactionDate();
        dateApproved = ct.getDateApproved();
        status = ct.getResponseDescription();
    }

    public TransactionReport(final AccountTransaction at) {
        merchantName = at.getMerchant().getName();
        merchantCode = at.getMerchant().getMerchantCode();
        sessionId = at.getSourceSessionId();
        transactionRef = at.getCpayRef();
        merchantRef = at.getMerchantRef();
        cpayRef = at.getCpayRef();
        amount = at.getAmount();
        fee = at.getFee();
        paymentType = at.getPaymentType();
        transactionDate = at.getTransactionDate();
        sourceBank = at.getSourceBank() == null ? null : at.getSourceBank().getName();
        customerNumber = at.getCustomerId();
    }


    public TransactionReport(final AjaxCpayAccountTransaction at) {
        merchantName = at.getMerchantName();
        merchantCode = at.getMerchantCode();
        sessionId = at.getSourceSessionId();
        transactionRef = at.getCpayRef();
        merchantRef = at.getMerchantRef();
        cpayRef = at.getCpayRef();
        amount = at.getAmount();
        fee = at.getFee();
        paymentType = at.getPaymentType();
        status = at.getResponseDescription();
        transactionDate = at.getTransactionDate();
        sourceBank = at.getSourceBankName();
        customerNumber = at.getCustomerId();
    }

    public TransactionReport(final Merchant merchant) {
        merchantName = merchant.getMerchantName();
        merchantCode = merchant.getMerchantCode();
        email = merchant.getEmail();
        phoneNumber = merchant.getPhoneNumber();
        if (null != merchant.getMerchantAccountList() && !merchant.getMerchantAccountList().isEmpty()) {
            MerchantAccount merchantAccount = merchant.getMerchantAccountList().get(0);
            accountName = merchantAccount.getAccountName();
            accountNumber = merchantAccount.getAccountNumber();
            destinationBank = merchantAccount.getInstitution().getInstitutionName();

        }

        lga = merchant.getLga();
        status = merchant.getFlag().getName();
        state = merchant.getState();
        transactionDate = merchant.getCreatedDate();
    }

    public TransactionReport(final AccountBalance accountBalance) {
        sessionId = accountBalance.getSessionId();
        accountName = accountBalance.getAccount().getAccountName();
        accountNumber = accountBalance.getAccount().getAccountNumber();
        sourceBank = accountBalance.getAccount().getBank().getName();
        organizationName = accountBalance.getOrganization().getName();
        dateApproved = accountBalance.getDateAdded();

    }

    public TransactionReport(final Account account) {
        accountName = account.getAccountName();
        accountNumber = account.getAccountNumber();
        sourceBank = account.getBank().getName();
        organizationName = account.getOrganization().getName();
    }

}
