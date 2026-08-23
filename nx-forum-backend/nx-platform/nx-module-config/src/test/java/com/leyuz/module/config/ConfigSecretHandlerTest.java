package com.leyuz.module.config;

import com.leyuz.common.security.SecretCrypto;
import com.leyuz.module.config.app.ConfigSecretHandler;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigSecretHandlerTest {

    private ConfigSecretHandler handler;

    @Before
    public void setUp() {
        handler = new ConfigSecretHandler();
        ReflectionTestUtils.setField(handler, "secretKey", "unit-test-secret-key");
    }

    @Test
    public void testAiModelsApiKeyEncryptDecrypt() {
        String json = "{\"providers\":[{\"id\":\"p1\",\"name\":\"Minimax\",\"apiUrl\":\"https://api.minimaxi.com/v1\","
                + "\"apiKey\":\"sk-abcdef123456\",\"timeoutSeconds\":60,\"enabled\":true,"
                + "\"models\":[{\"id\":\"m1\",\"model\":\"Minimax-M3\"}]},"
                + "{\"id\":\"p2\",\"name\":\"Ollama\",\"apiUrl\":\"http://localhost:11434/v1\","
                + "\"apiKey\":\"\",\"enabled\":true,\"models\":[]}]}";

        String encrypted = handler.encryptConfigValue("ai_models", json);
        assertTrue("apiKey 应被加密", encrypted.contains(SecretCrypto.PREFIX));
        assertFalse("空 apiKey 不应被加密", encrypted.contains(SecretCrypto.PREFIX + "null"));

        String decrypted = handler.decryptConfigValue("ai_models", encrypted);
        assertTrue("解密后应还原明文 Key", decrypted.contains("sk-abcdef123456"));

        // 幂等：对已加密值再次加密不应叠加前缀
        String reEncrypted = handler.encryptConfigValue("ai_models", encrypted);
        assertEquals("重复加密应幂等", encrypted, reEncrypted);
    }

    @Test
    public void testMailAndSmsConfigEncryptDecrypt() {
        String mailJson = "{\"host\":\"smtp.aliyun.com\",\"port\":465,\"username\":\"a@b.com\","
                + "\"password\":\"mail-password\",\"defaultFrom\":\"nx-forum\",\"auth\":true}";
        String mailEncrypted = handler.encryptConfigValue("mail_smtp_config", mailJson);
        assertTrue(mailEncrypted.contains(SecretCrypto.PREFIX));
        assertTrue("解密应还原密码", handler.decryptConfigValue("mail_smtp_config", mailEncrypted).contains("mail-password"));

        String smsJson = "{\"provider\":\"ALIYUN\",\"accessKeyId\":\"LTAI4XXXXX\","
                + "\"accessKeySecret\":\"sms-secret\",\"signName\":\"nx\",\"enabled\":true}";
        String smsEncrypted = handler.encryptConfigValue("sms_config", smsJson);
        assertTrue(smsEncrypted.contains(SecretCrypto.PREFIX));
        assertTrue("解密应还原 secret", handler.decryptConfigValue("sms_config", smsEncrypted).contains("sms-secret"));
    }

    @Test
    public void testLegacyPlaintextPassthrough() {
        // 存量明文数据解密时应原样透传
        String legacy = "{\"host\":\"smtp.aliyun.com\",\"password\":\"legacy-plain\"}";
        assertEquals("历史明文应透传", legacy, handler.decryptConfigValue("mail_smtp_config", legacy));
    }

    @Test
    public void testNonSensitiveKeyUntouched() {
        String json = "{\"websiteName\":\"nx-forum\",\"seoTitle\":\"nx\"}";
        assertEquals(json, handler.encryptConfigValue("website_base_info", json));
        assertEquals(json, handler.decryptConfigValue("website_base_info", json));
    }

    @Test
    public void testInvalidJsonPassthrough() {
        assertEquals("not-json", handler.encryptConfigValue("ai_models", "not-json"));
        assertEquals("", handler.encryptConfigValue("ai_models", ""));
        assertEquals(null, handler.encryptConfigValue("ai_models", null));
    }

    @Test
    public void testSensitiveKeysRegistry() {
        assertTrue(handler.isSensitive("ai_models"));
        assertTrue(handler.isSensitive("mail_smtp_config"));
        assertTrue(handler.isSensitive("sms_config"));
        assertFalse(handler.isSensitive("website_base_info"));
    }
}
