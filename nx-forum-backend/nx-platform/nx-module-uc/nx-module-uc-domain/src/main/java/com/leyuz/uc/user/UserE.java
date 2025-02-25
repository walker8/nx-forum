package com.leyuz.uc.user;

import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.uc.user.constant.RegexConstant;
import com.leyuz.uc.user.dataobject.UserStatusV;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.regex.Pattern;


@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserE {
    private Long userId;

    private String userName;

    private String email;

    private String phone;

    private String password;

    private String avatar;

    private UserStatusV status;

    /**
     * 最后活跃IP
     */
    private String lastActiveIp;

    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveDate;

    /**
     * 注册时间
     */
    private LocalDateTime createTime;

    /**
     * 个人介绍
     */
    private String intro;

    /**
     * 创建新用户
     */
    public void register() {
        checkUserNameNotEmpty();
        checkPasswordNotEmpty();
        checkPassword();
        checkPhone();
        checkUserName();
        checkEmail();
        lastActiveDate = LocalDateTime.now();
        lastActiveIp = HeaderUtils.getIp();
        status = UserStatusV.NORMAL;
    }

    /**
     * 更新用户
     */
    public void update() {
        checkPassword();
        checkPhone();
        checkUserName();
        checkEmail();
    }

    public void init() {

    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 禁用用户
     */
    public void disable() {
        status = UserStatusV.DISABLED;
    }

    public void checkEmail() {
        if (!StringUtils.hasLength(email)) {
            return;
        }
        if (!Pattern.matches(RegexConstant.EMAIL_REGEX, email)) {
            throw new ValidationException("邮箱格式不正确");
        }
    }

    public void checkPhone() {
        if (!StringUtils.hasLength(phone)) {
            return;
        }
        if (!Pattern.matches(RegexConstant.PHONE_REGEX, phone)) {
            throw new ValidationException("手机格式错误，请输入正确的11位手机号码！");
        }
    }

    public void checkUserNameNotEmpty() {
        if (!StringUtils.hasLength(userName)) {
            throw new ValidationException("用户名不能为空");
        }
    }

    public void checkPasswordNotEmpty() {
        if (!StringUtils.hasLength(password)) {
            throw new ValidationException("密码不能为空");
        }
    }

    public void checkPhoneNotEmpty() {
        if (!StringUtils.hasLength(phone)) {
            throw new ValidationException("手机号不能为空");
        }
    }

    public void checkEmailNotEmpty() {
        if (!StringUtils.hasLength(email)) {
            throw new ValidationException("邮箱地址不能为空");
        }
    }

    public void checkUserName() {
        if (!StringUtils.hasLength(userName)) {
            return;
        }
        if (userName.length() < 3 || userName.length() > 16) {
            throw new ValidationException("用户名长度必须在3-16个字符之间");
        }
        if (!Pattern.matches(RegexConstant.USER_NAME_REGEX, userName)) {
            throw new ValidationException("用户名只能为中文、英文字符、数字或者下划线");
        }
        if (Pattern.matches(RegexConstant.PHONE_REGEX, userName)) {
            throw new ValidationException("用户名不可以为手机号码");
        }
        // 用户名不能是纯数字
        if (Pattern.matches(RegexConstant.NUMBER_REGEX, userName)) {
            throw new ValidationException("用户名不能是纯数字");
        }

    }

    public void checkPassword() {
        if (!StringUtils.hasLength(password)) {
            return;
        }
        if (!Pattern.matches(RegexConstant.PASSWORD_REGEX, password)) {
            throw new ValidationException("密码只能为英文字符、数字或者特殊字符");
        }
    }

}
