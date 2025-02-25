package com.leyuz.common.ip;

import lombok.Data;

@Data
public class Address {
    /**
     * 国家
     */
    private String country;
    /**
     * 区域
     */
    private String district;
    /**
     * 省份或州
     */
    private String region;
    /**
     * 城市
     */
    private String city;
    /**
     * 运营商
     */
    private String isp;
}
