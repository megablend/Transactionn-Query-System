package com.nibss.tqs.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@Slf4j
public class CustomErrorController  implements  ErrorController{

    private static final String PATH ="/error";

    @Autowired
    private ErrorAttributes errorAttributes;

    @Override
    public String getErrorPath() {
        return PATH;
    }


    @RequestMapping(value = PATH, produces = "text/html")
    public String htmlError(Model model, HttpServletRequest request,HttpServletResponse response) {

        log.trace("Http Status Code: {}", response.getStatus());
        switch(response.getStatus()) {
            case 404:
                return "errors/404";
            case 403:
                return "redirect:/";
            default:
                model.addAllAttributes(getErrorAttributes(request,true));
                return "errors/default";
        }
    }


    @RequestMapping(value = PATH)
    @ResponseBody
    public ResponseEntity<Map<String,Object>> error(HttpServletRequest request,HttpServletResponse response) {
        Map<String,Object> errors = getErrorAttributes(request,true);
        return  new ResponseEntity<>(errors, HttpStatus.valueOf(response.getStatus()));

    }
    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    }
}
