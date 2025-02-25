package com.leyuz.bbs.page.converter;

import com.leyuz.bbs.page.CustomPagePO;
import com.leyuz.bbs.page.dto.CustomPageCmd;
import com.leyuz.bbs.page.dto.CustomPageVO;
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