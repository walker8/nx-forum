package com.leyuz.uc.auth.token;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author walker
 * @since 2024-04-22
 */
@TableName("uc_user_login_tokens")
@Data
public class UserLoginTokenPO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 2908366221052737226L;
    /**
     * 自增id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 设备id
     */
    private String deviceId;

    /**
     * 令牌
     */
    private String token;

    /**
     * 令牌失效时间
     */
    private LocalDateTime tokenExpiresAt;

    /**
     * 客户端useragent
     */
    private String userAgent;

    /**
     * 登录IP
     */
    private String loginIp;

}
