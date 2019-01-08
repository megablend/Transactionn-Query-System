/*
 * Copyright 2014 Nuno Jacinto
 * Released under the MIT license
 *
 * Date: 2014-06-22
 */

package com.nibss.tqs.renderer.datatables;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nuno
 * @param <E>
 */
@XmlRootElement
public class JQueryDataTableResponse<E> implements Serializable {
    /**
	 * 
	 */

    public JQueryDataTableResponse() {
        this.extras = new HashMap<>(5);
    }
	private static final long serialVersionUID = 3034664684679649053L;
	private int draw; 
    // total number of records
    private long recordsTotal;
    // total number of records after applying the filtering conditions
    private long recordsFiltered;
    // configuration, the default is 'data'
    private E []data;

    private Map<String, Object> extras;
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
     * @return the recordsTotal
     */
    public long getRecordsTotal() {
        return recordsTotal;
    }

    /**
     * @param recordsTotal the recordsTotal to set
     */
    public void setRecordsTotal(long recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    /**
     * @return the recordsFiltered
     */
    public long getRecordsFiltered() {
        return recordsFiltered;
    }

    /**
     * @param recordsFiltered the recordsFiltered to set
     */
    public void setRecordsFiltered(long recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }
    
    public Map<String, Object> getExtras() {
    	return extras;
    }
    
    public void setExtras(final Map<String,Object> extras) {
    	this.extras = extras;
    }

    /**
     * @return the data
     */
    public E[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(E[] data) {
        this.data = data;
    }
    
    /*  */
    protected static final int MIN_BUFFER_SIZE = 65+9+9+9;
    //
    /**
     * 
     * @param draw
     * @param recordsTotal
     * @param recordsFiltered
     * @param jsonData
     * @return 
     */
    public static String createJson(int draw, long recordsTotal,
                                    long recordsFiltered, String jsonData){
        StringBuilder buff = new StringBuilder(MIN_BUFFER_SIZE + jsonData.length());
        return buff.append("{ \"draw\": ").append(draw).append(", \"recordsTotal\": ")
                .append(recordsTotal).append(", \"recordsFiltered\": ").append(recordsFiltered)
                .append(", \"data\": ").append(jsonData).append(" }").toString();
    }
}
