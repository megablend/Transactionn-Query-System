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
public class JQueryDataTableOrder implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 3475381838193387067L;
	//
    public static final int MAX_ORDERS = 10000;
    //
    private int column;
    private String dir;

    public JQueryDataTableOrder() {
    }

    public JQueryDataTableOrder(int column, String dir) {
        this.column = column;
        this.dir = dir;
    }

    public JQueryDataTableOrder(String column, String dir) {
        this(column==null ? -1 : Integer.parseInt(column), dir);
    }

    /**
     * @return the column index
     */
    public int getColumn() {
        return column;
    }

    /**
     * @param column the column index to set
     */
    public void setColumn(int column) {
        this.column = column;
    }

    /**
     * @return the direction
     */
    public String getDir() {
        return dir;
    }

    /**
     * @param dir the direction to set
     */
    public void setDir(String dir) {
        this.dir = dir;
    }

    /*  Extracting query parameters from HttServletRequest  */
    protected static final String orderColumnFormat  = "order[%d].column";
    protected static final String orderDirFormat  = "order[%d].dir";
    //
    /**
     * 
     * @param index
     * @param request
     * @return 
     */
    public static JQueryDataTableOrder create(int index, HttpServletRequest request) {
        String column = request.getParameter(String.format(orderColumnFormat, index));
        if(column!=null){
            return new JQueryDataTableOrder(Integer.parseInt(column),
                request.getParameter(String.format(orderDirFormat, index)));
        }
        return null;
    }

    /**
     * 
     * @param request
     * @return 
     */
    public static JQueryDataTableOrder[] create(HttpServletRequest request) {
        ArrayList<JQueryDataTableOrder> list = 
                new ArrayList<JQueryDataTableOrder>(10);
        JQueryDataTableOrder dta;
        for(int i=0; i<MAX_ORDERS; i++) {
            dta = create(i, request);
            if(dta!=null){
                list.add(dta);
            } else {
                break;
            }
        }
        return list.toArray(new JQueryDataTableOrder[list.size()]);
    }
    
}
