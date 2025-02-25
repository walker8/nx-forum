package com.leyuz.uc.user.verify;

import cn.hutool.core.util.RandomUtil;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.module.cache.GenericCache;
import com.leyuz.module.mail.application.MailApplication;
import com.leyuz.module.sms.application.SmsApplication;
import com.leyuz.uc.user.dataobject.AccountTypeV;
import com.leyuz.uc.user.verify.dto.VerifyType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerifyCodeService {
    private final GenericCache<String, String> smsCache;
    private final GenericCache<String, String> mailCache;
    private final MailApplication mailApplication;
    private final SmsApplication smsApplication;

    public void sendEmailVerifyCode(String email, String userName, VerifyType verifyType) {
        if (StringUtils.isBlank(email)) {
            throw new ValidationException("请输入邮箱地址");
        }
        // 使用String.format()方法格式化输出，确保总是得到6位数
        String code = String.format("%06d", RandomUtil.randomInt(100000, 1000000));
        Map<String, String> variables = new HashMap<>();
        variables.put("code", code);
        variables.put("expireMinutes", "20");
        switch (verifyType) {
            case REGISTER:
                variables.put("userName", "注册用户");
                mailApplication.sendTemplateEmail(MailApplication.MailTemplateType.VERIFY_CODE, email, variables);
                break;
            case LOGIN, CHANGE_EMAIL:
                variables.put("userName", userName);
                mailApplication.sendTemplateEmail(MailApplication.MailTemplateType.VERIFY_CODE, email, variables);
                break;
            case RESET_PASSWORD:
                variables.put("userName", userName);
                mailApplication.sendTemplateEmail(MailApplication.MailTemplateType.RESET_PASSWORD, email, variables);
                break;
        }
        // 缓存验证码
        mailCache.put(email, code);
    }

    public void sendPhoneVerifyCode(String phone, VerifyType verifyType) {
        if (StringUtils.isBlank(phone)) {
            throw new ValidationException("请输入手机号码");
        }
        // 使用String.format()方法格式化输出，确保总是得到6位数
        String code = String.format("%06d", RandomUtil.randomInt(100000, 1000000));

        smsApplication.sendVerifyCode(phone);
        // 缓存验证码
        smsCache.put(phone, code);
    }

    public void verifyCode(String account, String code, AccountTypeV accountTypeV, VerifyType verifyType) {
        String savedCode = AccountTypeV.PHONE.equals(accountTypeV) ? smsCache.get(account) : mailCache.get(account);
        if (code == null || !code.equals(savedCode)) {
            throw new ValidationException("验证码错误");
        }
    }

    public void deleteCache(String account, AccountTypeV accountTypeV, VerifyType verifyType) {
        // 验证成功后删除验证码
        if (AccountTypeV.PHONE.equals(accountTypeV)) {
            smsCache.remove(account);
        } else {
            mailCache.remove(account);
        }
    }

}
