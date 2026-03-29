package com.leyuz.common.ip;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.ObjectUtil;
import com.leyuz.common.exception.ServiceException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.lionsoul.ip2region.service.Config;
import org.lionsoul.ip2region.service.Ip2Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;

/**
 * 根据ip地址定位工具类，离线方式
 * 参考地址：<a href="https://gitee.com/lionsoul/ip2region/tree/master/binding/java">集成 ip2region 实现离线IP地址定位库</a>
 *
 * @author lishuyan
 */
@Component
public class RegionUtils {

    private static Ip2Region IP2REGION;

    @Value("${nx.ip.only-v4:false}")
    private boolean onlyV4;

    private static String loadXdbFile(String fileName, String desc) {
        File destFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
        ClassPathResource resource = new ClassPathResource(fileName);
        try (InputStream stream = resource.getStream()) {
            if (ObjectUtil.isEmpty(stream)) {
                throw new ServiceException("RegionUtils初始化失败，原因：" + desc + " IP地址库数据不存在！");
            }
            FileUtil.writeFromStream(stream, destFile);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("RegionUtils初始化失败，原因：" + desc + " IP地址库加载异常！" + e.getMessage());
        }
        return destFile.getPath();
    }

    private static String cleanRegionPart(String part) {
        if (part == null || "0".equals(part)) {
            return "";
        }
        return part;
    }

    @PostConstruct
    public void init() {
        String v4DbPath = loadXdbFile("/ip2region_v4.xdb", "IPv4");

        Config v4Config;
        try {
            v4Config = Config.custom()
                .setCachePolicy(Config.BufferCache)
                .setXdbPath(v4DbPath)
                .asV4();
        } catch (Exception e) {
            throw new ServiceException("RegionUtils初始化失败，原因：IPv4配置创建失败！" + e.getMessage());
        }

        if (onlyV4) {
            try {
                IP2REGION = Ip2Region.create(v4Config, null);
            } catch (Exception e) {
                throw new ServiceException("RegionUtils初始化失败，原因：" + e.getMessage());
            }
            return;
        }

        String v6DbPath = loadXdbFile("/ip2region_v6.xdb", "IPv6");

        Config v6Config;
        try {
            v6Config = Config.custom()
                .setCachePolicy(Config.BufferCache)
                .setXdbPath(v6DbPath)
                .asV6();
        } catch (Exception e) {
            throw new ServiceException("RegionUtils初始化失败，原因：IPv6配置创建失败！" + e.getMessage());
        }

        try {
            IP2REGION = Ip2Region.create(v4Config, v6Config);
        } catch (Exception e) {
            throw new ServiceException("RegionUtils初始化失败，原因：" + e.getMessage());
        }
    }

    @PreDestroy
    public void destroy() {
        if (IP2REGION != null) {
            try {
                IP2REGION.close();
            } catch (Exception e) {
                // 忽略关闭异常
            }
        }
    }

    /**
     * 根据IP地址离线获取城市（同时支持 IPv4 和 IPv6）
     * <p>
     * ip2region 3.x 输出格式：Country|Province|City|ISP|CountryCode
     */
    public static Address getAddress(String ip) {
        Address address = new Address();
        if (ip == null) {
            address.setCountry("未知");
            return address;
        }
        try {
            ip = ip.trim();
            String region = IP2REGION.search(ip);
            if (region == null || region.isEmpty()) {
                address.setCountry("未知");
                return address;
            }
            String[] regions = region.split("\\|", 5);
            if (regions.length >= 5) {
                address.setCountry(cleanRegionPart(regions[0]));
                address.setRegion(cleanRegionPart(regions[1]));
                address.setCity(cleanRegionPart(regions[2]));
                address.setIsp(cleanRegionPart(regions[3]));
            } else {
                address.setCountry("未知");
            }
        } catch (Exception e) {
            address.setCountry("未知");
        }
        return address;
    }

}
