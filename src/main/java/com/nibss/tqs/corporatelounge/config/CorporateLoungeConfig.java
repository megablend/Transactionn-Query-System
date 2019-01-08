package com.nibss.tqs.corporatelounge.config;

import com.nibss.cryptography.AESKeyGenerator;
import com.nibss.cryptography.KeyLength;
import com.nibss.nip.crypto.NIPCryptoWS_Service;
import com.nibss.nip.dao.NipDAO;
import com.nibss.nip.webservice.NIPInterface_Service;
import com.nibss.nip.webservice.impl.NipWebServiceHelper;
import com.nibss.util.converters.JAXBConverter;
import com.nibss.util.converters.XmlConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.net.URL;

/**
 * Created by eoriarewo on 5/4/2017.
 */
@Configuration
@Slf4j
public class CorporateLoungeConfig {


    @Value("#{ new java.net.URL('${cl.nip_wsdl}' ) }")
    private URL nipWsdl;

    @Value( "#{ new java.net.URL( '${cl.crypto_wsdl}') }" )
    private URL cryptoUrl;

    @Value("${cl.nibssNipCode}")
    private String nibssNipCode;

    @Bean
    public AESKeyGenerator aesKeyGenerator() {
        return new AESKeyGenerator(KeyLength.AES_128);
    }

    @Bean
    @Scope("prototype")
    public XmlConverter xmlConverter() {
        return new JAXBConverter();
    }

    @Bean
    @Scope("prototype")
    public NIPCryptoWS_Service nipCryptoWS_service() {
        return  new NIPCryptoWS_Service(cryptoUrl);
    }

    @Bean
    @Scope("prototype")
    public NIPInterface_Service nipService() {
        return  new NIPInterface_Service(nipWsdl);
    }

    @Bean
    @Scope("prototype")
    public NipDAO nipDAO(NIPInterface_Service nipService, NIPCryptoWS_Service cryptoService, XmlConverter converter) {
        NipWebServiceHelper helper = new NipWebServiceHelper(nipService, cryptoService, converter);
        helper.setNibssBankCode(nibssNipCode);
        return helper;
    }


}
