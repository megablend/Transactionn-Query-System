/*
 * Copyright 2014 Nuno Jacinto
 * Released under the MIT license
 *
 * Date: 2014-06-22
 */

package com.nibss.tqs.renderer.datatables;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author nuno
 */
@XmlRootElement
public class JQueryDataTableSearch implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 139080165811299492L;
	//
    private String value;
    private boolean regex;

    public JQueryDataTableSearch() {
    }

    public JQueryDataTableSearch(String value, boolean regex) {
        this.regex = regex;
        this.value = value;
    }

    public JQueryDataTableSearch(String value, String regex) {
        this(value, regex==null ? false : Boolean.parseBoolean(regex));
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the regex
     */
    public boolean getRegex() {
        return regex;
    }

    /**
     * @param regex the regex to set
     */
    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    /*  Extracting query parameters from HttServletRequest  */
    protected static final String searchValueParamName  = "search.value";
    protected static final String searchRegexParamName  = "search.regex";
    protected static final String colSearchValueFormat  = "columns[%d][search][value]";
    protected static final String colSearchRegexFormat  = "columns[%d][search][regex]";
    
    /**
     * 
     * @param request
     * @return 
     */
    public static JQueryDataTableSearch create(HttpServletRequest request) {
        String value = request.getParameter(searchValueParamName);
        if(value!=null){
            return new JQueryDataTableSearch(value,
                request.getParameter(searchRegexParamName));
        }
        return null;
    }

    /**
     * 
     * @param col
     * @param request
     * @return 
     */
    public static JQueryDataTableSearch create(int col, HttpServletRequest request) {
        String value = String.format(colSearchValueFormat, col);
        if(value!=null){
            return new JQueryDataTableSearch(request.getParameter(value),
                request.getParameter(String.format(colSearchRegexFormat, col)));
        }
        return null;
    }
}
