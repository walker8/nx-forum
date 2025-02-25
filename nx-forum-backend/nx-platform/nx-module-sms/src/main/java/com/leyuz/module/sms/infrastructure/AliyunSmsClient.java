package com.leyuz.module.sms.infrastructure;

import com.alibaba.cola.exception.BizException;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.leyuz.module.sms.dto.SmsConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AliyunSmsClient {

    private static final String ENDPOINT = "dysmsapi.aliyuncs.com";

    public void sendSms(String phone, String code, SmsConfigDTO config) {
        try {
            // 创建配置
            Config aliConfig = new Config()
                    .setAccessKeyId(config.getAccessKeyId())
                    .setAccessKeySecret(config.getAccessKeySecret())
                    .setEndpoint(ENDPOINT);

            // 创建客户端
            Client client = new Client(aliConfig);

            // 创建发送短信请求
            SendSmsRequest sendSmsRequest = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(config.getSignName())
                    .setTemplateCode(config.getVerifyCodeTemplateId())
                    .setTemplateParam("{\"code\":\"" + code + "\"}");

            // 发送短信
            SendSmsResponse response = client.sendSms(sendSmsRequest);

            // 处理响应
            if (!"OK".equals(response.getBody().getCode())) {
                log.error("短信发送失败: phone={}, code={}, message={}",
                        phone, response.getBody().getCode(), response.getBody().getMessage());
                throw new BizException("短信发送失败: " + response.getBody().getMessage());
            }

            log.info("短信发送成功: phone={}, requestId={}", phone, response.getBody().getRequestId());

        } catch (Exception e) {
            log.error("短信发送异常", e);
            throw new BizException("短信发送失败: " + e.getMessage());
        }
    }
} 