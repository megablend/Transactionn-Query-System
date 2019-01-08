package com.nibss.tqs.centralpay.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by eoriarewo on 8/28/2017.
 */
public enum CentralPayProcessors {

    ISW( "00001", "ISW"),
    UPSL("00002","UPSL"),
    NIBSS("00003","NIBSS"),
    UNKOWN("" , "");


    private static  Map<String,String> map = new HashMap<>();

    static  {
        map.put( "00001", "ISW");
        map.put( "00002", "UPSL");
        map.put( "00003", "NIBSS");
        map.put( "", "");
    }
     CentralPayProcessors(String code, String name) {
        this.code = code;
        this.name = name;

    }
    private String name;
    private String code;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    public synchronized static String getNameFromCode(String code) {
        if( code == null)
            return "";
        return map.getOrDefault(code,"");
    }

}
