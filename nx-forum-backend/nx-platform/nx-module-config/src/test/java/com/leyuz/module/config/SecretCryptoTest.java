package com.leyuz.module.config;

import com.leyuz.common.security.SecretCrypto;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SecretCryptoTest {

    private static final String SECRET_KEY = "unit-test-secret-key";

    @Test
    public void testEncryptDecryptRoundTrip() {
        String plain = "sk-test-api-key-123456";
        String encrypted = SecretCrypto.encrypt(plain, SECRET_KEY);
        assertTrue("应带加密前缀", SecretCrypto.isEncrypted(encrypted));
        assertEquals("解密应还原原文", plain, SecretCrypto.decrypt(encrypted, SECRET_KEY));
    }

    @Test
    public void testEncryptRandomIv() {
        // 相同明文两次加密应得到不同密文（随机 IV），但都能解密回原文
        String plain = "same-plain-text";
        String e1 = SecretCrypto.encrypt(plain, SECRET_KEY);
        String e2 = SecretCrypto.encrypt(plain, SECRET_KEY);
        assertNotEquals("随机 IV 应产生不同密文", e1, e2);
        assertEquals(plain, SecretCrypto.decrypt(e1, SECRET_KEY));
        assertEquals(plain, SecretCrypto.decrypt(e2, SECRET_KEY));
    }

    @Test
    public void testDecryptPlaintextPassthrough() {
        // 历史明文数据应原样透传，保证平滑升级
        assertEquals("legacy-plain", SecretCrypto.decrypt("legacy-plain", SECRET_KEY));
        assertNull(SecretCrypto.decrypt(null, SECRET_KEY));
        assertEquals("", SecretCrypto.decrypt("", SECRET_KEY));
    }

    @Test
    public void testEncryptBlank() {
        assertNull(SecretCrypto.encrypt(null, SECRET_KEY));
        assertNull(SecretCrypto.encrypt("", SECRET_KEY));
        assertNull(SecretCrypto.encrypt("  ", SECRET_KEY));
    }

    @Test(expected = IllegalStateException.class)
    public void testEncryptWithoutSecretKey() {
        SecretCrypto.encrypt("secret", null);
    }

    @Test(expected = IllegalStateException.class)
    public void testDecryptWithWrongKey() {
        String encrypted = SecretCrypto.encrypt("secret", SECRET_KEY);
        SecretCrypto.decrypt(encrypted, "another-key");
    }

    @Test
    public void testMask() {
        assertEquals("sk-a****3456", SecretCrypto.mask("sk-abcdef123456"));
        assertEquals("****", SecretCrypto.mask("short"));
        assertEquals("", SecretCrypto.mask(""));
        assertEquals("", SecretCrypto.mask(null));
    }

    @Test
    public void testIsMaskedForm() {
        String plain = "sk-abcdef123456";
        assertTrue(SecretCrypto.isMaskedForm("sk-a****3456", plain));
        assertFalse(SecretCrypto.isMaskedForm("", plain));
        assertFalse(SecretCrypto.isMaskedForm("sk-a****9999", plain));
        assertFalse(SecretCrypto.isMaskedForm(plain, plain));
    }
}
