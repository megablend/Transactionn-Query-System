package com.nibss.tqs.controllers;

import com.nibss.tqs.ajax.SearchDTO;
import com.nibss.tqs.centralpay.repositories.CpayMerchantService;
import com.nibss.tqs.config.security.CurrentUser;
import com.nibss.tqs.core.entities.Organization;
import com.nibss.tqs.core.entities.OrganizationType;
import com.nibss.tqs.core.entities.User;
import com.nibss.tqs.core.repositories.IOrganization;
import com.nibss.tqs.core.repositories.OrganizationRepository;
import com.nibss.tqs.ebillspay.repositories.EbillsBillerService;
import com.nibss.tqs.merchantpayment.McashService;
import com.nibss.tqs.ussd.repositories.UssdMerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by eoriarewo on 8/22/2016.
 */
@Controller
@RequestMapping("/search")
@Slf4j
public class SearchController {

    private  static final  List<SearchDTO> EMPTY_LIST = Collections.EMPTY_LIST;

    @Autowired
    private EbillsBillerService billerService;

    @Autowired
    private UssdMerchantService ussdMerchantService;

    @Autowired
    private CpayMerchantService cpayMerchantService;

    @Autowired
    private OrganizationRepository orgRepo;

    @Autowired
    private McashService mcashService;

    private static final List<SearchDTO> ALL_SEARCH = Arrays.asList( new SearchDTO("","ALL"));

    private static final String ALL = "all";

    @RequestMapping(value = "/ebillspay", method = RequestMethod.GET)
    public @ResponseBody List<SearchDTO> ebillsPayBillers(@RequestParam("search") String search,
                                                          @RequestParam(value = "isBank",required = false) Integer bank, @CurrentUser User user) {

        if( null == user)
            return EMPTY_LIST;

        if( search.equalsIgnoreCase(ALL))
            return ALL_SEARCH;

        IOrganization userOrg = user.getOrganizationInterface();
        log.trace("searching for biller pattern: {}",search);
        try {
            Pageable pageable = new PageRequest(0, 20);
            String text = "%" + search + "%";
            Page<SearchDTO> billersPage = billerService.searchAll(text, pageable);
            List<SearchDTO> billers = billersPage.getContent();
            if( null == billers || billers.isEmpty()) {
                log.trace("no response provided for search");
                return EMPTY_LIST;
            }

            if( userOrg.getOrganizationType() ==  OrganizationType.AGGREGATOR_INT || (bank != null && bank == 1)
                    || userOrg.getOrganizationType() == OrganizationType.BANK_INT) {
                Collection<Integer> billerIds = orgRepo.findEbillsPayBillersForOrganization(userOrg.getId());
                billerIds =  billerIds == null ? new HashSet<>(0) : billerIds;
                Set<String> aggIds =billerIds.stream().map(i -> Integer.toString(i)).collect(Collectors.toSet());

                billers = billers.stream().filter( b -> aggIds.contains(b.getId())).collect(Collectors.toList());
            }

            log.trace("size of values gotten: {}",billers.size());
            return billers;
        } catch (Exception e) {
            log.error("could not search for ebillspay biller",e);
            return EMPTY_LIST;
        }
    }

    @RequestMapping(value = "/centralpay", method = RequestMethod.GET)
    public @ResponseBody List<SearchDTO> centralPayMerchants(@RequestParam("search") String search,
                                                             @RequestParam(value = "isBank",required = false) Integer bank,
                                                             @CurrentUser User user) {

        if( null == user)
            return EMPTY_LIST;

        if( search.equalsIgnoreCase(ALL))
            return ALL_SEARCH;

        IOrganization userOrg = user.getOrganizationInterface();
        try {
            Pageable pageable = new PageRequest(0, 50);

            Page<SearchDTO> merchantsPage = cpayMerchantService.searchAll("%"+search+"%",pageable);
            List<SearchDTO> merchants = merchantsPage.getContent();
            if( null == merchants)
                return EMPTY_LIST;
//            merchants.get(0);

            if( userOrg.getOrganizationType() ==  Integer.parseInt(OrganizationType.AGGREGATOR) || (bank != null && bank == 1)
                    || userOrg.getOrganizationType() == OrganizationType.BANK_INT) {
                Collection<String> cpayCodes = orgRepo.findCentralPayMerchantCodesForOrganization(userOrg.getId());
                merchants = merchants.stream().filter( b -> cpayCodes.contains(b.getId())).collect(Collectors.toList());
            }
            return  merchants;
        } catch (Exception e) {
            log.error("could not search for centralpay merchants",e);
            return EMPTY_LIST;
        }
    }

    @RequestMapping(value = "/ussd", method = RequestMethod.GET)
    public @ResponseBody List<SearchDTO> ussdBillers(@RequestParam("search") String search,
                                                     @RequestParam(value = "isBank",required = false) Integer bank,
                                                     @CurrentUser User user) {

        if( null == user)
            return EMPTY_LIST;

        if( search.equalsIgnoreCase(ALL))
            return ALL_SEARCH;

        IOrganization userOrg = user.getOrganizationInterface();
        try {
            Pageable pageable = new PageRequest(0, 30);
            Page<SearchDTO>  page = ussdMerchantService.searchAll("%" + search + "%",pageable);
            List<SearchDTO> ussdBillers = page.getContent();
            if( null == ussdBillers)
                return EMPTY_LIST;
           // ussdBillers.get(0);

            if( userOrg.getOrganizationType() ==  Integer.parseInt(OrganizationType.AGGREGATOR) || (bank != null && bank == 1)
                    || userOrg.getOrganizationType() == OrganizationType.BANK_INT) {
                Collection<String> ussdCodes = orgRepo.findUssdBillerCodesForOrganization(userOrg.getId());
                ussdBillers = ussdBillers.stream().filter( b -> ussdCodes.contains(b.getId())).collect(Collectors.toList());
            }
            return  ussdBillers;
        } catch (Exception e) {
            log.error("could not search for centralpay merchants",e);
            return EMPTY_LIST;
        }
    }

    @RequestMapping(value = "/merchantpay", method = RequestMethod.GET)
    public @ResponseBody List<SearchDTO> merchantPay(@RequestParam("search") String search,
                                                     @RequestParam(value = "isBank",required = false) Integer bank,
                                                     @CurrentUser User user) {


        if( null == user)
            return EMPTY_LIST;

        if( search.equalsIgnoreCase(ALL))
            return ALL_SEARCH;

        IOrganization userOrg = user.getOrganizationInterface();
        try {
            Pageable pageable = new PageRequest(0, 50);
            String text = "%" + search + "%";
            Page<SearchDTO> merchantsPage = mcashService.searchAll(text, pageable);
            List<SearchDTO> merchants = merchantsPage.getContent();
            if( null == merchants || merchants.isEmpty())
                return EMPTY_LIST;
//            merchants.get(0);
            Collection<Number> mIds = orgRepo.findMerchantIdsByOrganization(userOrg.getId());
            mIds = mIds == null ? new HashSet<>(0) : mIds;
            if( userOrg.getOrganizationType() ==  OrganizationType.AGGREGATOR_INT || (bank != null && bank == 1) ||
                    userOrg.getOrganizationType() ==  OrganizationType.MERCHANT_INT || userOrg.getOrganizationType() == OrganizationType.BANK_INT) {

                Set<String> aggIds = mIds.stream().map(i -> Long.toString(i.longValue())).collect(Collectors.toSet());
                merchants = merchants.stream().filter( b -> aggIds.contains(b.getId())).collect(Collectors.toList());
            } else if( userOrg.getOrganizationType() ==  Integer.parseInt(OrganizationType.BANK)) {
                Set<String> aggIds = mIds.stream().map(i -> Long.toString(i.longValue())).collect(Collectors.toSet());
                merchants = merchants.stream().filter( b -> aggIds.contains(b.getId())).collect(Collectors.toList());
            }
            return  merchants;
        } catch (Exception e) {
            log.error("could not search for mcash merchants",e);
            return EMPTY_LIST;
        }
    }
}
