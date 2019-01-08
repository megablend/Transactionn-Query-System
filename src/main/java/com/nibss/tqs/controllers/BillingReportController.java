package com.nibss.tqs.controllers;

import com.nibss.tqs.config.ApplicationSettings;
import com.nibss.tqs.core.entities.*;
import com.nibss.tqs.core.repositories.AggregatorRepository;
import com.nibss.tqs.core.repositories.BankRepository;
import com.nibss.tqs.core.repositories.IOrganization;
import com.nibss.tqs.report.BaseReportHelper;
import com.nibss.tqs.report.BillingFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Emor on 9/27/16.
 */
@Controller
@RequestMapping("/billingreport")
@Slf4j
public class BillingReportController {

    private final static Comparator<BillingFile> BILLING_FILE_COMPARATOR = (a, b) -> a.getDateCreated().compareTo(b.getDateCreated());

    @Autowired
    private ApplicationSettings appSettings;

    @Autowired
    private AggregatorRepository aggregatorRepository;

    @Autowired
    private BankRepository bankRepository;

    @RequestMapping(value = "/{product}", method = RequestMethod.GET)
    public String index(@PathVariable("product") String product, Model model, Authentication auth) {

        User user = (User) auth.getPrincipal();
        String userCode = getOrganizationCode(user.getOrganizationInterface());

        List<BillingFile> files = new ArrayList<>(0);
        if (userCode != null) {

            String productCode = getProductCode(product, model);
            try {
                Path folderPath = Paths.get(appSettings.billingReportFolder(), productCode, userCode);

                if (Files.exists(folderPath)) {
                    files = getBillingFiles(folderPath);
                    files = files.stream().sorted(BILLING_FILE_COMPARATOR.reversed()).collect(Collectors.toList());
                }
            } catch (InvalidPathException e) {
                log.error("Invalid folder path: {}, {}, {}", appSettings.billingReportFolder(), productCode, userCode);
            }
        }

        model.addAttribute("productType", product);
        model.addAttribute("reportFiles", files);

        return "billing/reports";
    }


    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public ResponseEntity<byte[]> download(Authentication auth, @RequestParam("name") String fileName,
                                           @RequestParam("product") String product, Model model) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        if (auth.getPrincipal() == null)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        User user = (User) auth.getPrincipal();
        String userCode = getOrganizationCode(user.getOrganizationInterface());
        if (null == userCode)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        Path theFile;

        try {
            theFile =  Paths.get(appSettings.billingReportFolder(), getProductCode(product, model), userCode, fileName);
        } catch (InvalidPathException e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        if (!Files.exists(theFile))
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        String contentType = "application/pdf";
        if (fileName.toLowerCase().endsWith("csv"))
            contentType = "text/csv";


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(theFile))) {
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1)
                outputStream.write(buffer);
        }

        headers.set("Content-Type", contentType);
        headers.set("Content-Disposition", String.format("inline;filename=\"%s\"", fileName));
        return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);

    }


    private String getProductCode(String type, Model model) {
        switch (type.toLowerCase()) {
            case "cpay":
                model.addAttribute("product", "CentralPay Account Billing Reports");
                return Product.CENTRALPAY;
            case "mpay":
                model.addAttribute("product", "USSD Merchant Payment Billing Reports");
                return Product.USSD_MERCHANT_PAYMENT;
            case "bpay":
                model.addAttribute("product", "USSD Bill Payment Billing Reports");
                return Product.USSD_BILL_PAYMENT;
            case "ebills":
            default:
                model.addAttribute("product", "e-BillsPay Billing Reports");
                return Product.EBILLSPAY;
        }
    }

    private String getOrganizationCode(IOrganization org) {
        if (org.getOrganizationType() == OrganizationType.NIBSS_INT)
            return BaseReportHelper.NIBSS_CODE;
        else if (org.getOrganizationType() == OrganizationType.BANK_INT) {
            return bankRepository.findCbnCodeByBank(org.getId());
        } else if (org.getOrganizationType() == OrganizationType.AGGREGATOR_INT) {
            return aggregatorRepository.findCodeForAggregator(org.getId());
        }

        return null;
    }

    private List<BillingFile> getBillingFiles(Path folderPath) {
        List<BillingFile> files = new ArrayList<>();
        if (!Files.exists(folderPath))
            return new ArrayList<>(0);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath, "*.{csv,pdf}")) {

            Iterator<Path> itr = stream.iterator();
            while (itr.hasNext()) {
                Path file = itr.next();
                BillingFile temp = new BillingFile();
                temp.setFileName(file.getFileName().toString());
                BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
                FileTime creationTime = attr.creationTime();
                Date createDate = Date.from(creationTime.toInstant());
                temp.setDateCreated(createDate);

                String name = file.getFileName().toString();
                if (name.toLowerCase().endsWith("csv"))
                    temp.setFileType("CSV");
                else
                    temp.setFileType("PDF");

                files.add(temp);

            }

        } catch (IOException e) {
            log.error("could not get files for organization", e);
            return files;
        }

        return files;
    }
}
