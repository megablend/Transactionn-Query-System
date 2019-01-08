/*
 * Copyright 2014 Nuno Jacinto
 * Released under the MIT license
 *
 * Date: 2014-06-22
 */

package com.nibss.tqs.renderer.datatables;

import java.io.Serializable;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlRootElement;
/**
 *
 * @author nuno
 */
@XmlRootElement
public class JQueryDataTableColumn implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6557886073386682526L;
	//
    public static final int MAX_COLUMNS = 10000;
    //
    private String colData;
    private String colName;
    private boolean colSearchable;
    private boolean colOrderable;
    private JQueryDataTableSearch search;

    public JQueryDataTableColumn() {
    }

    public JQueryDataTableColumn(String colData, String colName, boolean colSearchable,
            boolean colOrderable, JQueryDataTableSearch search) {
        this.colData = colData;
        this.colName = colName;
        this.colOrderable = colOrderable;
        this.colSearchable = colSearchable;
        this.search = search;
    }

    public JQueryDataTableColumn(String colData, String colName, String colSearchable,
            String colOrderable, JQueryDataTableSearch search) {
        this(colData, colName, colSearchable==null ? false : Boolean.parseBoolean(colSearchable),
                colOrderable==null ? false : Boolean.parseBoolean(colOrderable), search);
    }

    public JQueryDataTableColumn(String colData, String colName, boolean colSearchable,
            boolean colOrderable, String colSearchValue, boolean colSearchRegex) {
        this(colData, colName, colSearchable, colOrderable, 
                new JQueryDataTableSearch(colSearchValue, colSearchRegex));
    }

    public JQueryDataTableColumn(String colData, String colName, String colSearchable,
            String colOrderable, String colSearchValue, String colSearchRegex) {
        this(colData, colName, colSearchable, colOrderable,
                new JQueryDataTableSearch(colSearchValue, colSearchRegex));
    }

    /**
     * @return the colData
     */
    public String getData() {
        return colData;
    }

    /**
     * @param colData the colData to set
     */
    public void setData(String colData) {
        this.colData = colData;
    }

    /**
     * @return the colName
     */
    public String getName() {
        return colName;
    }

    /**
     * @param colName the colName to set
     */
    public void setName(String colName) {
        this.colName = colName;
    }

    /**
     * @return the colSearchable
     */
    public boolean getSearchable() {
        return colSearchable;
    }

    /**
     * @param colSearchable the colSearchable to set
     */
    public void setSearchable(boolean colSearchable) {
        this.colSearchable = colSearchable;
    }

    /**
     * @return the colOrderable
     */
    public boolean getOrderable() {
        return colOrderable;
    }

    /**
     * @param colOrderable the colOrderable to set
     */
    public void setOrderable(boolean colOrderable) {
        this.colOrderable = colOrderable;
    }

    /**
     * @return the search
     */
    public JQueryDataTableSearch getSearch() {
        return search;
    }

    /**
     * @param search the search to set
     */
    public void setSearch(JQueryDataTableSearch search) {
        this.search = search;
    }

    /*  Extracting query parameters from HttServletRequest  */
    protected static final String colDataFormat  = "columns[%d][data]";
    protected static final String colNameFormat  = "columns[%d][name]";
    protected static final String colSearchableFormat  = "columns[%d][searchable]";
    protected static final String colOrderableFormat  = "columns[%d][orderable]";
    //
    /**
     * 
     * @param col
     * @param request
     * @return 
     */
    public static JQueryDataTableColumn create(int col, HttpServletRequest request) {
        String colData = request.getParameter(String.format(colDataFormat, col));
        if(colData!=null){
            return new JQueryDataTableColumn(colData, 
                    request.getParameter(String.format(colNameFormat, col)), 
                    request.getParameter(String.format(colOrderableFormat, col)), 
                    request.getParameter(String.format(colSearchableFormat, col)), 
                    JQueryDataTableSearch.create(col, request));
        }
        return null;
    }

    /**
     * 
     * @param request
     * @return 
     */
    public static JQueryDataTableColumn[] create(HttpServletRequest request) {
        ArrayList<JQueryDataTableColumn> list = 
                new ArrayList<JQueryDataTableColumn>(10);
        JQueryDataTableColumn dta;
        for(int i=0; i<MAX_COLUMNS; i++) {
            dta = create(i, request);
            if(dta!=null){
                list.add(dta);
            } else {
                break;
            }
        }
        return list.toArray(new JQueryDataTableColumn[list.size()]);
    }
    
}
