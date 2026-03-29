package com.leyuz.common.ip;

import cn.hutool.core.net.NetUtil;
import cn.hutool.http.HtmlUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 获取地址类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressUtils {

    // 未知地址
    public static final String UNKNOWN = "XX XX";

    /**
     * 根据ip获取省份或州，不存在则返回国家
     *
     * @param ip ip地址
     * @return 返回地址
     */
    public static String getRegionByIP(String ip) {
        if (StringUtils.isBlank(ip)) {
            return UNKNOWN;
        }
        // 内网不查询
        ip = StringUtils.contains(ip, "0:0:0:0:0:0:0:1") ? "127.0.0.1" : HtmlUtil.cleanHtmlTag(ip);
        if (NetUtil.isInnerIP(ip)) {
            return "内网IP";
        }
        Address address = RegionUtils.getAddress(ip);
        if (StringUtils.isNotBlank(address.getRegion())) {
            return address.getRegion();
        }
        return address.getCountry();
    }

    /**
     * 根据ip获取城市（支持 IPv4 和 IPv6）
     *
     * @param ip ip地址
     * @return 返回地址
     */
    public static String getCityByIP(String ip) {
        if (StringUtils.isBlank(ip)) {
            return UNKNOWN;
        }
        // 内网不查询
        ip = StringUtils.contains(ip, "0:0:0:0:0:0:0:1") ? "127.0.0.1" : HtmlUtil.cleanHtmlTag(ip);
        if (NetUtil.isInnerIP(ip)) {
            return "内网IP";
        }
        Address address = RegionUtils.getAddress(ip);
        if (StringUtils.isNotBlank(address.getCity())) {
            return address.getRegion() + address.getCity();
        }
        if (StringUtils.isNotBlank(address.getRegion())) {
            return address.getRegion();
        }
        return address.getCountry();
    }

}
