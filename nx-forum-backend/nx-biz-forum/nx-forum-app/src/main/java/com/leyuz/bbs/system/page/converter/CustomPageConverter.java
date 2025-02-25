package com.leyuz.bbs.system.page.converter;

import com.leyuz.bbs.system.page.CustomPagePO;
import com.leyuz.bbs.system.page.dto.CustomPageCmd;
import com.leyuz.bbs.system.page.dto.CustomPageVO;
import org.springframework.beans.BeanUtils;

public class CustomPageConverter {
    public static CustomPageVO toVO(CustomPagePO po) {
        CustomPageVO vo = new CustomPageVO();
        BeanUtils.copyProperties(po, vo);
        return vo;
    }

    public static CustomPagePO toPO(CustomPageCmd cmd) {
        CustomPagePO po = new CustomPagePO();
        BeanUtils.copyProperties(cmd, po);
        return po;
    }

} 