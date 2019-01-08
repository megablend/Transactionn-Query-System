package com.nibss.tqs.centralpay.dto;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by eoriarewo on 9/5/2016.
 */
@Entity
@Table(name="cpay_gateway_response_code")
@Data
public class GatewayResponseCodes implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "gateway_response_code")
    private String responseCode;

    @Column(name="gateway_response_desc")
    private String responseDescription;

    @Column(name="gateway_id")
    private String gatewayId;

}
