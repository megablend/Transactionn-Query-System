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
public class JQueryDataTableRequest implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -5048693508253921358L;
	private int draw;
    private int start;
    private int length;
    private JQueryDataTableSearch search;
    private JQueryDataTableOrder order[];
    private JQueryDataTableColumn columns[];

    public JQueryDataTableRequest() {
    }
 
    public JQueryDataTableRequest(int draw, int start, int length) {
        this.draw = draw;
        this.start = start;
        this.length = length;
    }
 
    public JQueryDataTableRequest(String draw, String start, String length) {
        this(Integer.parseInt(draw), start==null ? -1 : Integer.parseInt(start),
            length==null ? -1 : Integer.parseInt(length));
    }
 
    public JQueryDataTableRequest(int draw, int start, int length,
            JQueryDataTableSearch search, JQueryDataTableOrder []order,
            JQueryDataTableColumn []columns) {
        this.draw = draw;
        this.start = start;
        this.length = length;
        this.order = order;
        this.search = search;
        this.columns = columns;
    }

    /**
     * @return the draw
     */
    public int getDraw() {
        return draw;
    }

    /**
     * @param draw the draw to set
     */
    public void setDraw(int draw) {
        this.draw = draw;
    }

    /**
     * @return the start
     */
    public int getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(int length) {
        this.length = length;
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

    /**
     * @return the order
     */
    public JQueryDataTableOrder[] getOrder() {
        return order;
    }

    /**
     * @param order the order to set
     */
    public void setOrder(JQueryDataTableOrder[] order) {
        this.order = order;
    }

    /**
     * @return the columns
     */
    public JQueryDataTableColumn[] getColumns() {
        return columns;
    }

    /**
     * @param columns the columns to set
     */
    public void setColumns(JQueryDataTableColumn[] columns) {
        this.columns = columns;
    }
    
    
    /*  Extracting query parameters from HttServletRequest  */
    protected static final String drawParamName  = "draw";
    protected static final String startParamName  = "start";
    protected static final String lengthParamName  = "length";
    //
    /**
     * 
     * @param request
     * @return 
     */
    public static JQueryDataTableRequest create(HttpServletRequest request){
        String draw = request.getParameter(drawParamName);
        if(draw!=null){
            JQueryDataTableRequest req = new JQueryDataTableRequest(draw, 
                    request.getParameter(startParamName), request.getParameter(lengthParamName));
            req.search = JQueryDataTableSearch.create(request);
            req.order = JQueryDataTableOrder.create(request);
            req.columns = JQueryDataTableColumn.create(request);
            return req;
        }
        return null;
    }
}
