package com.nibss.tqs.controllers;

import com.nibss.tqs.ajax.AjaxResponse;
import com.nibss.tqs.ajax.SearchDTO;
import com.nibss.tqs.centralpay.repositories.CpayMerchantRepository;
import com.nibss.tqs.config.security.CurrentUser;
import com.nibss.tqs.core.entities.*;
import com.nibss.tqs.core.repositories.*;
import com.nibss.tqs.ebillspay.repositories.BillerRepository;
import com.nibss.tqs.merchantpayment.MPMerchantRepository;
import com.nibss.tqs.renderer.datatables.JQueryDataTableRequest;
import com.nibss.tqs.renderer.datatables.JQueryDataTableResponse;
import com.nibss.tqs.ussd.repositories.UssdBillerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 7/18/2016.
 */
@Controller
@RequestMapping("/organizations")
@Slf4j
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CpayMerchantRepository cpayMerchantRepository;

    @Autowired
    private BillerRepository billerRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private AggregatorRepository aggregatorRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UssdBillerRepository ussdBillerRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private MPMerchantRepository mpMerchantRepository;


    @Autowired
    private OrganizationSettingRepository orgSetRepo;

    @Autowired
    private OrganizationRepository organizationRepository;

    public static final String SESSION_EXPIRED = "Your session has expired. Kindly logout and log back into the application";
    public static final String EXCEPTION_OCCURRED = "Sorry, we could not process your request at the moment. Please try again later";


    @RequestMapping(value = "", method = RequestMethod.GET)
    public String list(Model model) {
        try {
            log.trace("In organizationController index");
            log.trace("about getting product list");
            StopWatch watch = new StopWatch();
            watch.start();
            List<IProduct> products = productService.findAllByProjection();
            watch.stop();
            log.trace("Done getting product list. Time taken in ms: {}",watch.getTotalTimeMillis());
//            model.addAttribute("organizations", organizations);
            model.addAttribute("products", products);
        } catch (Exception e) {
            log.error(null, e);
        }
        return "organizations/list";
    }


    @RequestMapping(value = "", method = RequestMethod.POST)
    public
    @ResponseBody
    AjaxResponse createOrganization(@Valid Organization org, BindingResult orgBR, @Valid OrganizationSetting orgSetting, BindingResult orgSetBR,
                                    @RequestParam(value = "code", required = false) String organizationCode,
                                    @RequestParam(value = "nipCode", required = false) String nipCode, @CurrentUser User user
            , @RequestParam(value = "product", required = false) Integer[] productIds) {


        AjaxResponse response = new AjaxResponse();
        if (null == user) {
            return buildFailedResponse(SESSION_EXPIRED, null);
        }

        List<String> errors = new ArrayList<>();
        if (orgBR.hasErrors()) {
            orgBR.getAllErrors().stream().map(s -> s.getDefaultMessage()).forEach(e -> errors.add(e));
            return buildFailedResponse("Your validation failed", errors);
        }

        if (orgSetBR.hasErrors()) {
            orgBR.getAllErrors().stream().map(s -> s.getDefaultMessage()).forEach(e -> errors.add(e));
            return buildFailedResponse("Your validation failed", errors);
        }

        int organizationType = org.getOrganizationType();
        boolean isBank = organizationType == Integer.parseInt(OrganizationType.BANK);
        boolean isAggregator = organizationType == Integer.parseInt(OrganizationType.AGGREGATOR);

        if (!isBank && (null == productIds || productIds.length == 0))
            return buildFailedResponse("No product was selected", null);

        try {
            List<Product> orgProducts = productService.findAll(Arrays.asList(productIds));
            if (orgProducts != null) {
                org.setProducts(new HashSet<>(orgProducts));
                log.trace("No. of productIds found: {}", orgProducts.size());
            } else
                log.trace("No productIds found for IDSs : {}", Arrays.asList(productIds));
        } catch (Exception e) {
            log.error(null, e);
        }

        org.setOrganizationSetting(orgSetting);


        org.setCreatedBy(user.getEmail());
        if (isBank || isAggregator) {
            if (organizationCode == null || organizationCode.trim().isEmpty()) {
                return buildFailedResponse("You did not specify the code for this organization", null);
            }
        }

        try {
            if (organizationService.countByName(org.getName()) > 0) {
                return buildFailedResponse("An organization with this name already exists", null);
            }
            org.setCreatedBy(user.getEmail());
            if (isBank) {
                if (nipCode == null || nipCode.trim().isEmpty())
                    return buildFailedResponse("NIP Code was not specified", null);

                if (bankRepository.countByCbnBankCode(organizationCode) > 0)
                    return buildFailedResponse("A bank already exist with the CBN code", null);

                if (bankRepository.countByNipCode(nipCode) > 0)
                    return buildFailedResponse("A bank already exists with the NIP code", null);

                Bank bank = new Bank(org);
                bank.setCbnBankCode(organizationCode);
                bank.setNipCode(nipCode);
                org = bankRepository.save(bank);
            } else if (isAggregator) {
                if (aggregatorRepository.countByCode(organizationCode) > 0)
                    return buildFailedResponse("An aggregator already exists with this code", null);
                Aggregator agg = new Aggregator(org);
                agg.setCode(organizationCode);
                org = aggregatorRepository.save(agg);
            } else {
                Merchant merchant = new Merchant(org);
                org = merchantRepository.saveAndFlush(merchant);
            }

            for(int i : productIds)
                productService.saveOrganizationProduct(org.getId(), i);

            response.setStatus(AjaxResponse.SUCCESS);
            response.setMessage(Integer.toString(org.getId()));
            return response;
        } catch (Exception e) {
            log.error("Could not create organization", e);
            return buildFailedResponse(EXCEPTION_OCCURRED, null);
        }

    }

    @RequestMapping(value = "/{organizationId}", method = RequestMethod.GET)
    public String details(@PathVariable("organizationId") int organizationId, Model model, @CurrentUser User user) {
        IOrganization organization = null;
        try {
            StopWatch watch = new StopWatch();
            log.trace("about getting org from db");
            watch.start();
            organization = organizationRepository.findOneForDisplay(organizationId);
            watch.stop();
            log.trace("done getting org from db. time taken in ms: {}", watch.getTotalTimeMillis());
        } catch (Exception e) {
            log.error("Exception occurred while getting organization", e);
            return "redirect:/error";
        }
        if (organization == null)
            return "redirect:/404";

        //fetch cpay merchants, ebillspay billers and every other thing needed for biller
        model.addAttribute("organization", organization);
        if (organization.getOrganizationType() == OrganizationType.BANK_INT)
            model.addAttribute("code", bankRepository.findCbnCodeByBank(organizationId));
        if (organization.getOrganizationType() == OrganizationType.AGGREGATOR_INT)
            model.addAttribute("code",aggregatorRepository.findCodeForAggregator(organizationId));

        try {

            model.addAttribute("products", productService.findAllByProjection());

            //get only organization admin users for organizations that r not NIBSS n NIBSS personel is logged in
            if (organization.getOrganizationType() != Integer.parseInt(OrganizationType.NIBSS) && user.getOrganizationInterface().getOrganizationType() == OrganizationType.NIBSS_INT) {
                model.addAttribute("users", userRepository.findAdminUsersByOrganization(organizationId, Role.ADMIN));
                model.addAttribute("organizationProducts", productService.findByOrganizationProjecttion(organization.getId()));
                model.addAttribute("bankAccounts", bankAccountRepository.findByOrganization(organizationId));
                model.addAttribute("organizationSetting", orgSetRepo.findByOrganization(organizationId));
            } else {
                model.addAttribute("users", userRepository.findByOrganization(organizationId));
            }

        } catch (Exception e) {
            log.error(null, e);
        }

        String orgLabel = organization.getName();
        if (organization.getOrganizationType() == Integer.parseInt(OrganizationType.BANK))
            orgLabel += " (BANK)";
        else if (organization.getOrganizationType() == Integer.parseInt(OrganizationType.AGGREGATOR))
            orgLabel += " (AGGREGATOR)";
        else if (organization.getOrganizationType() == Integer.parseInt(OrganizationType.MERCHANT))
            orgLabel += " (MERCHANT/BILLER)";

        model.addAttribute("orgLabel", orgLabel);

        return "organizations/details";

    }

    @RequestMapping(value = "/{orgId}/settings", method = RequestMethod.POST)
    public
    @ResponseBody
    AjaxResponse updateSettings(@PathVariable("orgId") int orgId, @Valid OrganizationSetting orgSet,
                                BindingResult bResult, Authentication auth) {

        if (auth.getPrincipal() == null)
            return buildFailedResponse(SESSION_EXPIRED, null);
        if (bResult.hasErrors()) {
            List<String> errors = new ArrayList<>();
            bResult.getAllErrors().stream().map(e -> e.getDefaultMessage()).forEach(e -> errors.add(e));
            return buildFailedResponse("You did not fill the form properly", errors);
        }
        try {
            OrganizationSetting organizationSetting = orgSetRepo.findByOrganization(orgId);

            if (null == organizationSetting)
                return buildFailedResponse("The organization could not be located", null);

            User loggedInUser = (User) auth.getPrincipal();

            //OrganizationSetting existing = organizationSettingRepository.findOne(orgSet.getId());
            organizationSetting.setModifiedBy(loggedInUser.getEmail());
            organizationSetting.setDateModified(new Date());
            organizationSetting.setNoOfAdmins(orgSet.getNoOfAdmins());
            organizationSetting.setNoOfOperators(orgSet.getNoOfOperators());
            organizationSetting.setEbillspayTransactionDateAllowed(orgSet.isEbillspayTransactionDateAllowed());

            orgSetRepo.save(organizationSetting);
            return buildSuccessResponse("Organization Setting has been successfully updated");

        } catch (Exception e) {
            log.error("could not update organization settings", e);
            return buildFailedResponse(EXCEPTION_OCCURRED, null);
        }

    }

    @RequestMapping(value = "/{orgId}/bankaccount", method = RequestMethod.POST)
    public
    @ResponseBody
    AjaxResponse addProductAccount(@Valid BankAccount bankAccount, BindingResult br, Authentication auth,
                                   @PathVariable int orgId) {
        if (null == auth.getPrincipal())
            return buildFailedResponse(SESSION_EXPIRED, null);
        if (br.hasErrors()) {
            List<String> errors = br.getAllErrors().stream().map(e -> e.getDefaultMessage()).collect(Collectors.toList());
            return buildFailedResponse("Validation failed", errors);
        }

        try {
            Organization theOrg = organizationService.findOne(orgId);
            if (null == theOrg)
                return buildFailedResponse("The organization could not be found", null);


            BankAccount existingAcct = bankAccountRepository.findByOrganizationAndProductCode(orgId, bankAccount.getProduct().getCode());
            if (null != existingAcct) {
                existingAcct.setAccountName(bankAccount.getAccountName());
                existingAcct.setAccountNumber(bankAccount.getAccountNumber());
                existingAcct.setBankCode(bankAccount.getBankCode());
                bankAccountRepository.save(existingAcct);
            } else {
                bankAccount.setOrganization(theOrg);
                theOrg.getBankAccounts().add(bankAccount);
                bankAccountRepository.save(bankAccount);
                organizationService.save(theOrg);
            }

            return buildSuccessResponse("Bank account was successfully maintained");

        } catch (Exception e) {
            return buildFailedResponse(EXCEPTION_OCCURRED, null);
        }
    }

    @RequestMapping(value = "/{orgId}/products", method = RequestMethod.POST)
    public
    @ResponseBody
    AjaxResponse updateProducts(@PathVariable("orgId") int orgId, @RequestParam(value = "product") Integer[] productIds, @CurrentUser User user) {
        if (null == user)
            return buildFailedResponse(SESSION_EXPIRED, null);
        if (productIds == null || productIds.length == 0)
            return buildFailedResponse("No product was selected", null);

        try {
            Organization theOrg = organizationService.findOne(orgId);
            if (null == theOrg)
                return buildFailedResponse("Could not locate organization", null);

            List<Product> selectedProducts = productService.findAll(Arrays.asList(productIds));
            Set<Product> removedProducts = productService.findByOrganization(theOrg.getId());
            removedProducts.removeAll(selectedProducts);

            removedProducts.forEach(p -> {
                p.getOrganizations().remove(theOrg);
                if (p.getCode().equalsIgnoreCase("ebills"))
                    theOrg.setEbillspayBillerIds(new HashSet<>());
                else if (p.getCode().equalsIgnoreCase("cpay"))
                    theOrg.setCentralPayMerchantCodes(new HashSet<>());
                else if( p.getCode().equalsIgnoreCase("mpay"))
                    theOrg.setMerchantPaymentIds(new HashSet<>());
                else if( p.getCode().equalsIgnoreCase("bpay"))
                    theOrg.setUssdBillerCodes(new HashSet<>());
                p.getOrganizations().remove(theOrg);
            });

            if (!removedProducts.isEmpty())
                productService.save(removedProducts);


            if (null != selectedProducts) {
                theOrg.setProducts(new HashSet<>(selectedProducts));
                selectedProducts.forEach(s -> s.getOrganizations().add(theOrg));
            }

            organizationService.save(theOrg);
            productService.save(selectedProducts);

            return buildSuccessResponse("Product changes have been successfully saved");
        } catch (Exception e) {
            log.error("product update failed", e);
            return buildFailedResponse(EXCEPTION_OCCURRED, null);
        }
    }

    @RequestMapping(value = "/{orgId}/ebillspay", method = RequestMethod.POST)
    public
    @ResponseBody
    AjaxResponse updateEbillsPayBillers(@PathVariable("orgId") int orgId,
                                        @RequestParam("billers") Integer[] billerIds, @CurrentUser User user) {

        /*
        the implementation of this method and every other product method makes the following assumptions:
        1. for billers/productIds already mapped to this biller, the checkboxes (where necessary) are already selected on the page

         */
        if (user == null)
            return buildFailedResponse(SESSION_EXPIRED, null);

        if (billerIds == null || billerIds.length == 0)
            return buildFailedResponse("No biller was selected", null);

        try {
            Organization theOrg = organizationService.findOne(orgId);
            if (null == theOrg)
                return buildFailedResponse("Could not locate the organization", null);

            if (theOrg instanceof Merchant) {
                int billerId = billerIds[0];

                log.trace("adding new biller id {}", billerId);
                if (organizationRepository.eBillsBillerInOrganizationType(billerId, OrganizationType.MERCHANT_INT))
                    return buildFailedResponse("The ebills pay biller has already been mapped to a different merchant", null);

                organizationRepository.saveEbillsBiller(billerId, theOrg.getId());
            } else if ((theOrg instanceof Aggregator) || (theOrg instanceof Bank)) {
                log.trace("org is aggregator");
                for (int i : billerIds) {
                    organizationRepository.deleteEbillsBillerByIdAndOrganizationType(i, OrganizationType.AGGREGATOR_INT);
                    organizationRepository.deleteEbillsBillerByIdAndOrganizationType(i, OrganizationType.BANK_INT);
                    organizationRepository.saveEbillsBiller(i,theOrg.getId());
                }
            }
            return buildSuccessResponse("The changes to the eBillsPay biller(s) have been updated successfully");
        } catch (Exception e) {
            log.error("update billers failed", e);
            return buildFailedResponse(EXCEPTION_OCCURRED, null);
        }
    }

    @RequestMapping(value = "/{orgId}/cpay", method = RequestMethod.POST)
    public
    @ResponseBody
    AjaxResponse updateCpayMerchants(@PathVariable("orgId") int orgId, @RequestParam("merchants") String[] merchants, Authentication auth) {

        if (auth.getPrincipal() == null)
            return buildFailedResponse(SESSION_EXPIRED, null);

        if (null == merchants || 0 == merchants.length)
            return buildFailedResponse("No merchant was selected", null);

        try {

            Organization theOrg = organizationService.findOne(orgId);
            if (null == theOrg)
                return buildFailedResponse("The organization was not located", null);
            if (theOrg instanceof Merchant) {
                String merchant = merchants[0];
                if (organizationRepository.cpayMerchantInOrganizationType(merchant, OrganizationType.MERCHANT_INT))
                    return buildFailedResponse("The central pay merchant has already been mapped to a different merchant", null);
                organizationRepository.saveCpayMerchant(merchant, theOrg.getId());
            } else if ((theOrg instanceof Bank) || (theOrg instanceof Aggregator)) {
                for (String m : merchants) {
                    organizationRepository.deleteCpayMerchantByIdAndOrganizationType(m, OrganizationType.AGGREGATOR_INT);
                    organizationRepository.deleteCpayMerchantByIdAndOrganizationType(m, OrganizationType.BANK_INT);
                    organizationRepository.saveCpayMerchant(m,theOrg.getId());
                }
            }
            return buildSuccessResponse("The CentralPay merchant(s) have been successfully maintained");
        } catch (Exception e) {
            log.error("Merchant update failed", e);
            return buildFailedResponse(EXCEPTION_OCCURRED, null);
        }

    }

    @RequestMapping(value = "/{orgId}/merchantpay", method = RequestMethod.POST)
    public
    @ResponseBody
    AjaxResponse updateMerchantPayMerchants(@PathVariable("orgId") int orgId, @RequestParam("merchants") Long[] merchants, Authentication auth) {

        if (auth.getPrincipal() == null)
            return buildFailedResponse(SESSION_EXPIRED, null);

        if (null == merchants || 0 == merchants.length)
            return buildFailedResponse("No merchant was selected", null);

        try {

            Organization theOrg = organizationService.findOne(orgId);
            if (null == theOrg)
                return buildFailedResponse("The organization was not located", null);

            if (theOrg instanceof Merchant) {
                for( Long id : merchants) {
                  if(!organizationRepository.mcashMerchantInOrganizationType(id,OrganizationType.MERCHANT_INT))
                      organizationRepository.saveMcashMerchant(id,theOrg.getId());
                }

            } else if ((theOrg instanceof Bank) || (theOrg instanceof Aggregator)) {

                for (Long m : merchants) {
                    organizationRepository.deleteMcashMerchantByIdAndOrganizationType(m, OrganizationType.AGGREGATOR_INT);
                    organizationRepository.deleteMcashMerchantByIdAndOrganizationType(m, OrganizationType.BANK_INT);
                    organizationRepository.saveMcashMerchant(m,theOrg.getId());
                }
            }
//            organizationService.save(theOrg);
            return buildSuccessResponse("The mCASH merchant(s) have been successfully maintained");
        } catch (Exception e) {
            log.error("Merchant update failed", e);
            return buildFailedResponse(EXCEPTION_OCCURRED, null);
        }

    }


    @RequestMapping(value = "/{orgId}/ussd", method = RequestMethod.POST)
    public
    @ResponseBody
    AjaxResponse updateBillPaymentMerchants(@PathVariable("orgId") int orgId, @RequestParam("ussd") String[] merchants, Authentication auth) {

        if (auth.getPrincipal() == null)
            return buildFailedResponse(SESSION_EXPIRED, null);

        if (null == merchants || 0 == merchants.length)
            return buildFailedResponse("No merchant was selected", null);

        try {

            Organization theOrg = organizationService.findOne(orgId);
            if (null == theOrg)
                return buildFailedResponse("The organization was not located", null);
            if (theOrg instanceof Merchant) {
                String merchant = merchants[0];
                if (organizationRepository.ussdBillerInOrganiaztionType(merchant, OrganizationType.MERCHANT_INT))
                    return buildFailedResponse("The USSD biller has already been mapped to a different merchant", null);
               organizationRepository.saveUssdMerchant(merchant,theOrg.getId());
            } else if ((theOrg instanceof Bank) || (theOrg instanceof Aggregator)) {
                for (String m : merchants) {

                    organizationRepository.deleteUssdMerchantByIdAndOrganizationType(m, OrganizationType.AGGREGATOR_INT);
                    organizationRepository.deleteUssdMerchantByIdAndOrganizationType(m, OrganizationType.BANK_INT);

                    organizationRepository.saveUssdMerchant(m,theOrg.getId());
                }
            }
//            organizationService.save(theOrg);
            return buildSuccessResponse("The MerchantPayment merchant(s) have been successfully maintained");
        } catch (Exception e) {
            log.error("Merchant update failed", e);
            return buildFailedResponse(EXCEPTION_OCCURRED, null);
        }

    }


    public static AjaxResponse buildFailedResponse(String message, List<String> errors) {
        synchronized (new Object()) {
            AjaxResponse response = new AjaxResponse();
            response.setStatus(AjaxResponse.FAILED);
            response.setMessage(message);
            response.setErrors(errors);
            return response;
        }
    }

    public static AjaxResponse buildSuccessResponse(String message) {
        synchronized (new Object()) {
            AjaxResponse response = new AjaxResponse();
            response.setStatus(AjaxResponse.SUCCESS);
            response.setMessage(message);
            return response;
        }
    }


    @RolesAllowed({Role.NIBSS_ADMIN})
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public ResponseEntity<JQueryDataTableResponse<Object>> organizations(HttpServletRequest request, @CurrentUser User user) {
        if (null == user)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        JQueryDataTableRequest dtRequest = JQueryDataTableRequest.create(request);

        JQueryDataTableResponse response = new JQueryDataTableResponse();
        response.setDraw(dtRequest.getDraw());

        int pageNo = dtRequest.getStart() / dtRequest.getLength();

        String search = request.getParameter("search[value]");

        Pageable pageable = new PageRequest(pageNo, dtRequest.getLength());

        List<IOrganization> organizations;

        try {

            if (null != search && !search.trim().isEmpty()) {
                String orgType = "-1";
                switch (search.toLowerCase()) {
                    case "bank":
                        orgType = OrganizationType.BANK;
                        break;
                    case "aggregator":
                        orgType = OrganizationType.AGGREGATOR;
                        break;
                    case "merchant":
                        orgType = OrganizationType.MERCHANT;
                        break;
                    case "nibss":
                        orgType = OrganizationType.NIBSS;
                        break;
                }

                int oType = Integer.parseInt(orgType);
                if (oType != -1) {
                    Page<IOrganization> oPge = organizationRepository.findByOrganizationType(oType, pageable);
                    response.setRecordsFiltered(oPge.getNumberOfElements());
                    organizations = oPge.getContent();
                } else {
                    String nameSearch = "%" + search + "%";
                    Page<IOrganization> nPg = organizationRepository.findByNameContaining(nameSearch, pageable);
                    response.setRecordsFiltered(nPg.getNumberOfElements());
                    organizations = nPg.getContent();
                }
            } else {
                Page<IOrganization> organizationPage = organizationRepository.findAllByProjection(pageable);
                response.setRecordsFiltered(organizationPage.getTotalElements());
                organizations = organizationPage.getContent();
            }
            response.setRecordsTotal(organizationRepository.count());
            response.setData(renderOrgs(organizations).toArray());

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("could not render org datatable", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private List<Object[]> renderOrgs(List<? extends Object> objects) {
        List<Object[]> records = new ArrayList<>(objects.size());

        objects.forEach(o -> {
            List<Object> row = new ArrayList<>();
           if( o instanceof IOrganization) {
               IOrganization org = (IOrganization)o;
               row.add(org.getName());
               switch (Integer.toString(org.getOrganizationType())) {
                   case OrganizationType.AGGREGATOR:
                       row.add("Aggregator");
                       break;
                   case OrganizationType.BANK:
                       row.add("Bank");
                       break;
                   default:
                   case OrganizationType.MERCHANT:
                       row.add("Merchant");
                       break;
                   case OrganizationType.NIBSS:
                       row.add("NIBSS");
                       break;
               }
               row.add("<a href='#' class='details' id='{id}'>Details</a>".replace("{id}", Integer.toString(org.getId())));
           } else if( o instanceof SearchDTO) {
               SearchDTO s = (SearchDTO)o;
               row.add(s.getId());
               row.add(s.getText());
           } else if( o instanceof  String) {
               String s = (String)o;
               row.add(s);
           }
            records.add(row.toArray());
        });

        return records;
    }



    @RolesAllowed({Role.NIBSS_ADMIN})
    @RequestMapping(value = "/merchants", method = RequestMethod.POST)
    public ResponseEntity<JQueryDataTableResponse<Object>> organizationMerchants(HttpServletRequest request, @CurrentUser User user,
                                                                                 @RequestParam("productCode") String productCode,
                                                                                 @RequestParam("organizationId") int organizationId) {
        if (null == user)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        JQueryDataTableRequest dtRequest = JQueryDataTableRequest.create(request);

        JQueryDataTableResponse response = new JQueryDataTableResponse();
        response.setDraw(dtRequest.getDraw());
        response.setRecordsFiltered(0);
        response.setRecordsTotal(0);

        int pageNo = dtRequest.getStart() / dtRequest.getLength();

        String search = request.getParameter("search[value]");

        Pageable pageable = new PageRequest(pageNo, dtRequest.getLength());

        // TODO: 7/3/2017 modify sections below to use search parameter up there
        try {
            switch(productCode.toUpperCase()) {
                case Product.CENTRALPAY:
                    Collection<String> cpayCodes = organizationRepository.findCentralPayMerchantCodesForOrganization(organizationId);
                    if( null != cpayCodes && !cpayCodes.isEmpty()) {
                        log.trace("about getting cpay dtos");
                        Page<SearchDTO> dtos = cpayMerchantRepository.findBasicByMerchantCodes(cpayCodes,pageable);
                        log.trace("done getting cpay dtos");
                        if( null != dtos) {
                            response.setRecordsFiltered(dtos.getTotalElements());
                            response.setRecordsTotal(dtos.getTotalElements());
                            response.setData(renderOrgs(dtos.getContent()).toArray());
                        }
                    }
                    break;
                case Product.EBILLSPAY:
                    Collection<Integer> billerIds = organizationRepository.findEbillsPayBillersForOrganization(organizationId);
                    if( null != billerIds && !billerIds.isEmpty()) {
                        log.trace("about getting ebills billers");
                        Page<String> billerNames = billerRepository.findAllByIds(billerIds, pageable);
                        if( null != billerNames) {
                            response.setRecordsFiltered(billerNames.getTotalElements());
                            response.setRecordsTotal(billerNames.getTotalElements());
                            response.setData( renderOrgs(billerNames.getContent()).toArray());
                        }
                    }
                    break;
                case Product.USSD_MERCHANT_PAYMENT:
                    Collection<Number> merchantIds = organizationRepository.findMerchantIdsByOrganization(organizationId);
                    if( null != merchantIds && !merchantIds.isEmpty()) {
                        Collection<Long> mIds = merchantIds.stream().map( i -> i.longValue()).collect(Collectors.toList());
                        Page<SearchDTO> dtos = mpMerchantRepository.findAllByIds(mIds, pageable);
                        if( null != dtos) {
                            response.setRecordsFiltered(dtos.getTotalElements());
                            response.setRecordsTotal(dtos.getTotalElements());
                            response.setData(renderOrgs(dtos.getContent()).toArray());
                        }
                    }
                    break;
                case Product.USSD_BILL_PAYMENT:
                    Collection<String> ussdCodes = organizationRepository.findUssdBillerCodesForOrganization(organizationId);
                    if( null != ussdCodes && !ussdCodes.isEmpty()) {
                        Page<SearchDTO> dtos = ussdBillerRepository.findBasicByCodes(ussdCodes, pageable);
                        if( null != dtos) {
                            response.setRecordsFiltered(dtos.getTotalElements());
                            response.setRecordsTotal(dtos.getTotalElements());
                            response.setData( renderOrgs(dtos.getContent()).toArray());
                        }
                    }
                    break;
            }

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("could not render org datatable", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

}
