package com.nibss.tqs.controllers;

import com.nibss.corporatelounge.dto.*;
import com.nibss.cryptography.AESKeyGenerator;
import com.nibss.cryptography.Hasher;
import com.nibss.cryptography.IVAesHasher;
import com.nibss.cryptography.IVKeyPair;
import com.nibss.exceptions.ConversionException;
import com.nibss.exceptions.EncryptionException;
import com.nibss.nip.util.NipResponseCodes;
import com.nibss.tqs.config.QueueConfig;
import com.nibss.tqs.corporatelounge.service.ApiService;
import com.nibss.tqs.corporatelounge.service.BankService;
import com.nibss.tqs.corporatelounge.service.ClientService;
import com.nibss.util.converters.XmlConverter;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by eoriarewo on 4/24/2017.
 */
@RestController
@RequestMapping("/corporateloungeapi")
@Slf4j
@Api(value = "corporateloungeapi", description = "Corporate Lounge REST API operations.")
public class CorporateLoungeApiController {

    public static final String INSTITUTION_CODE = "institutionCode";


    @Autowired
    private ApiService apiService;

    @Autowired
    @Qualifier("clClientService")
    private ClientService clientService;

    @Value("${cl.maxAccountProfiledPerRequest}")
    private int maxAccountProfiledPerRequest;

    @Autowired
    private BankService bankService;

    @Autowired
    private AESKeyGenerator aesKeyGenerator;

    @Autowired
    private XmlConverter xmlConverter;


    @Autowired
    private JmsTemplate jmsTemplate;



    @ApiOperation(value = "list of accounts profiled by client", response = String.class)
    @ApiResponses(
            {
                    @ApiResponse(code = 400, message = "institutionCode not found in header"),
                    @ApiResponse(code = 401, message = "institutionCode does not exist"),
                    @ApiResponse(code = 500, message = "Error occurred while fetching accounts"),
                    @ApiResponse(code = 200, message = "successful")
            }
    )
    @RequestMapping(value = "/accounts", method = RequestMethod.GET)
    public ResponseEntity<String> getAccounts(@RequestHeader(required = false, value = INSTITUTION_CODE) String institutionCode,
                                              @ApiParam(value = "zero-index based page to fetch", defaultValue = "0")
                                              @RequestParam(value = "page", required = false, defaultValue = "0") int pageNo,
                                              @ApiParam(value = "the size of a page of accounts", defaultValue = "0")
                                              @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        Organization org;
        if (null == institutionCode || institutionCode.trim().isEmpty()) {
            log.error("institution code was not specified in header");
            return ResponseEntity.badRequest().body("Institution Code was not specified in header");
        } else {

            try {
                log.trace("GET ACCOUNTS. Inst Id: {}, Page: {}, Page Size: {}", institutionCode, pageNo, pageSize);
                org = clientService.findByInstitutionCode(institutionCode);

                if (null == org) {
                    log.trace("institution with code {} does not exist.", institutionCode);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(String.format("institutionCode %s does not exist", institutionCode));
                } else {
                    Response response = apiService.getAccounts(org, pageNo, pageSize);
                    String strResponse = xmlConverter.toXML(response);
                    log.trace("Plain response for inst.Code {}: {}", institutionCode, strResponse);

                    Hasher hasher = new IVAesHasher(org.getSecretKey(), org.getIvKey());
                    String encryptedResponse = hasher.encrypt(strResponse);
                    log.trace("Encrypted response for inst.Code {}: {}", institutionCode, encryptedResponse);
                    return ResponseEntity.ok().body(encryptedResponse);

                }
            } catch (Exception e) {
                log.error("could not get organization details for institution with code : {}", institutionCode, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(String.format("an error occurred while getting accounts for org with inst.Code %s", institutionCode));
            }
        }
    }

    @ApiOperation(value = "Get details of a particular account", response = String.class)
    @ApiResponses(
            {
                    @ApiResponse(code = 400, message = "institutionCode not found in header"),
                    @ApiResponse(code = 401, message = "institutionCode does not exist"),
                    @ApiResponse(code = 500, message = "Error occurred while fetching account details"),
                    @ApiResponse(code = 200, message = "successful")
            }
    )
    @ApiImplicitParams(
            @ApiImplicitParam(name = "accountNumber",value = "The account number", dataType = "string", example = "http://localhost/corporateloungeapi/account/0000342112", paramType = "path")
    )
    @RequestMapping(value = "/account/{accountNumber}", method = RequestMethod.GET)
    public ResponseEntity<String> getAccount(

            @PathVariable("accountNumber") String accountNumber,
            @RequestHeader(required = false, value = INSTITUTION_CODE) String institutionCode) {

        log.trace("GET ACCOUNT. Account Number: {}", accountNumber);
        Organization org = null;
        if (null == institutionCode || institutionCode.trim().isEmpty()) {
            log.error("institution code was not specified in header");
            return ResponseEntity.badRequest().body("Institution Code was not specified in header");
        } else {
            try {
                org = clientService.findByInstitutionCode(institutionCode);
                if (null == org) {
                    log.trace("institution with code {} does not exist.", institutionCode);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(String.format("institutionCode %s does not exist", institutionCode));
                } else {
                    Response response = apiService.getAccount(accountNumber, org);
                    String strResponse = xmlConverter.toXML(response);
                    log.trace("Plain response for inst.Code {}: {}", institutionCode, strResponse);

                    Hasher hasher = new IVAesHasher(org.getSecretKey(), org.getIvKey());
                    String encryptedResponse = hasher.encrypt(strResponse);
                    log.trace("Encrypted response for inst.Code {}: {}", institutionCode, encryptedResponse);
                    return ResponseEntity.ok().body(encryptedResponse);
                }
            } catch (Exception e) {
                log.error("could not retrieve organization", e);
                log.error("could not get organization details for institution with code : {}", institutionCode, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(String.format("an error occurred while getting accounts for org with inst.Code %s", institutionCode));
            }
        }

    }

    @ApiOperation(value = "Get real-time balances on specified accounts")
    @ApiResponses(
            {
                    @ApiResponse(code = 400, message = "institutionCode not found in header, could not decrypt the request, XML not properly formed, no account in request or number of accounts exceeds limit"),
                    @ApiResponse(code = 401, message = "institutionCode does not exist"),
                    @ApiResponse(code = 500, message = "Error occurred while fetching account details"),
                    @ApiResponse(code = 200, message = "successful")
            }
    )
    @RequestMapping(value = "/balances", method = RequestMethod.POST)
    public ResponseEntity<String> getBalances(@RequestHeader(required = false, value = INSTITUTION_CODE) String institutionCode,
                                              @ApiParam(value = "the AES encrypted XML containing accounts for which balances are required", required = true)
                                              @RequestBody String encRequest) {

        Organization org;
        if (null == institutionCode || institutionCode.trim().isEmpty()) {
            log.error("institution code was not specified in header");
            return ResponseEntity.badRequest().body("Institution Code was not specified in header");
        }
        try {
            org = clientService.findByInstitutionCode(institutionCode);
            if (null == org) {
                log.trace("institution with code {} does not exist.", institutionCode);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(String.format("institutionCode %s does not exist", institutionCode));
            }

            Hasher hasher = new IVAesHasher(org.getSecretKey(), org.getIvKey());
            log.trace("Encrypted GetBalance Request for organization {}: {}",org.getName(), encRequest);

            String plainRequest = null;
            try {
                plainRequest = hasher.decrypt(encRequest);
                log.trace("Plain XML GetBalance Request for organization {}: {}",org.getName(), plainRequest);
            } catch (EncryptionException e) {
                log.warn("could not decrypt org request");
                return ResponseEntity.badRequest().body("Your request could not be decrypted");
            }

            //convert xml request to object
            Request request = null;

            try {
                request = xmlConverter.toObject(plainRequest, Request.class);
            } catch (Exception e) {
                log.error("could not convert xml request to Request object");
                return ResponseEntity.badRequest().body("Your XML is not well formed");
            }

            if (request.getAccounts() == null || request.getAccounts().length == 0) {
                return ResponseEntity.badRequest().body("No accounts were specified in  request");
            } else if (request.getAccounts().length > org.getMaxRequestSize()) {
                return ResponseEntity.badRequest().body(String.format("You cannot request balances for more than %d account(s) in per request", org.getMaxRequestSize()));
            }
            Response response = apiService.getBalances(request, org);
            String strResponse = xmlConverter.toXML(response);
            log.trace("Plain GetBalance Response: {}", strResponse);

            String encryptedResponse = hasher.encrypt(strResponse);
            log.trace("Encrypted GetBalance Response: {}", encryptedResponse);
            return new ResponseEntity<>(encryptedResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("could not get organization details for institution with code : {}", institutionCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("Sorry, your request could not be processed. Please try again later", institutionCode));

        }
    }


    @ApiOperation(value = "Add new accounts")
    @ApiResponses(
            {
                    @ApiResponse(code = 400, message = "institutionCode not found in header, could not decrypt the request, XML not properly formed, no account in request or number of accounts exceeds limit"),
                    @ApiResponse(code = 401, message = "institutionCode does not exist"),
                    @ApiResponse(code = 500, message = "Error occurred while maintaining accounts"),
                    @ApiResponse(code = 202, message = "Accepted for processing")
            }
    )
    @RequestMapping(value = "/accounts", method = RequestMethod.PUT)
    public ResponseEntity<String> addAccounts(@RequestHeader(required = false, value = INSTITUTION_CODE) String institutionCode,
                                              @ApiParam(value = "the AES encrypted XML containing accounts for profiling", required = true)
                                              @RequestBody String encRequest) {

        if (null == institutionCode || institutionCode.trim().isEmpty()) {
            log.error("institution code was not specified in header");
            return ResponseEntity.badRequest().body("Institution Code was not specified in header");
        }

        Organization org;

        try {
            org = clientService.findByInstitutionCode(institutionCode);
        } catch (Exception e) {
            log.error("could not fetch organization with inst. code {}", institutionCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while getting organization with inst. code " + institutionCode);
        }

        if (org == null) {
            log.trace("institution with code {} does not exist.", institutionCode);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(String.format("institutionCode %s does not exist", institutionCode));
        }

        log.trace("Encrypted Add.Account request: {}", encRequest);

        Hasher hasher = new IVAesHasher(org.getSecretKey(), org.getIvKey());
        String plainReq = null;
        try {
            plainReq = hasher.decrypt(encRequest);
            log.trace("Plain Add.Account request: {}", plainReq);
        } catch (EncryptionException e) {
            return ResponseEntity.badRequest().body("Your request could not be decrypted");
        }

        Request req = null;
        try {
            req = xmlConverter.toObject(plainReq, Request.class);
        } catch (ConversionException e) {
            return ResponseEntity.badRequest().body("Your XML request is not well formed");
        }
        try {

            if (req.getAccounts() == null || req.getAccounts().length == 0) {
                log.trace("no accts were sent in request");
                return new ResponseEntity<>("No account was included in the request", HttpStatus.BAD_REQUEST);
            } else if (req.getAccounts().length > maxAccountProfiledPerRequest) {
                log.warn("accounts in request {} > maximum allowed for organization {}", req.getAccounts().length, org.getMaxRequestSize());
                return ResponseEntity.badRequest().body(String.format("You can only profile a maximum of %d account(s) per request", maxAccountProfiledPerRequest));
            }

            Set<Account> accounts = new HashSet<>(Arrays.asList(req.getAccounts()));

            NipResponseCodes validationResponse = apiService.validateAccounts(accounts);
            if( validationResponse != NipResponseCodes.SUCCESSFUL) {
                log.warn("account validation failed");
                return ResponseEntity.ok(buildResponse(org, validationResponse));
            }

            apiService.addAccounts(org, accounts);
            log.trace("handed account profiling to account service");

            ResponseEntity<String> response = ResponseEntity.accepted().body(buildResponse(org, NipResponseCodes.PROCESSINNG_IN_PROGRESS));
            log.trace("returning response");
            return response;


        } catch (RuntimeException e) {
            log.error("could not add accounts to DB", e);
            return new ResponseEntity<>(buildResponse(org, NipResponseCodes.SYSTEM_MALFUNCTION), HttpStatus.OK);
        }
    }


    @ApiOperation(value = "Change secret and IV keys")
    @ApiResponses(
            {
                    @ApiResponse(code = 400, message = "institutionCode not found in header"),
                    @ApiResponse(code = 401, message = "institutionCode does not exist"),
                    @ApiResponse(code = 500, message = "Error while resetting keys"),
                    @ApiResponse(code = 200, message = "successful")
            }
    )
    /* API method call to update API key for client */
    @RequestMapping(value = "/changekey", method = RequestMethod.POST)
    public ResponseEntity<String> changeAesKey(@RequestHeader(value = INSTITUTION_CODE, required = false) String institutionCode) {

        if (null == institutionCode || institutionCode.trim().isEmpty()) {
            log.error("institution code was not specified in header");
            return ResponseEntity.badRequest().body("Institution Code was not specified in header");
        }

        Organization org;

        try {
            org = clientService.findByInstitutionCode(institutionCode);

        } catch (Exception e) {
            log.error("could not fetch organization with inst. code {}", institutionCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("an error occurred while getting accounts for org with inst.Code %s", institutionCode));
        }

        if (org == null) {
            log.trace("institution with code {} does not exist.", institutionCode);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(String.format("institutionCode %s does not exist", institutionCode));
        }

        try {
            IVKeyPair pair = aesKeyGenerator.generateKeyPair();
            org.setSecretKey(pair.getSecretKey());
            org.setIvKey(pair.getIvKey());
            clientService.updateOrganizationKeys(org.getSecretKey(), org.getIvKey(), org.getId());
            jmsTemplate.convertAndSend(QueueConfig.CL_KEY_CHANGE, org);
            log.trace("API key successfully updated for {}", org.getInstitutionCode());
            return ResponseEntity.ok().body("Your request is being processed. You will be notified via email shortly");

        } catch (Exception e) {

            return new ResponseEntity<>("An error occurred while changing your AES Key", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @ApiOperation(value = "List of banks,detailing name and institution code")
    @ApiResponses(
            {
                    @ApiResponse(code = 500, message = "Error occurred while fetching banks"),
                    @ApiResponse(code = 200, message = "successful")
            }
    )
    /* returns list of all banks profiled in database */
    @RequestMapping(value = "/banks", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    List<Bank> getBanks() {
        return bankService.findAllStrippedDown();
    }

    private String buildResponse(Organization org, NipResponseCodes responseCode) {
        Hasher hasher = new IVAesHasher(org.getSecretKey(), org.getIvKey());
        Response response = new Response();
        response.setResponseDescription(responseCode.getResponseDesc());
        response.setResponseCode(responseCode.getResponseCode());

        try {
            String plainRes = xmlConverter.toXML(response);
            return hasher.encrypt(plainRes);
        } catch (EncryptionException | ConversionException e) {
            throw new RuntimeException("could not encrypt message", e);
        }
    }

}
