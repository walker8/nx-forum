package com.leyuz.common.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 敏感配置值加解密工具（AES-GCM）
 *
 * <p>用于对 common_configs 等存储中的敏感字段（API Key、密码、AccessKeySecret）做静态加密：</p>
 * <ul>
 *     <li>加密值带 {@link #PREFIX} 前缀标识，形如 {@code enc:v1:base64(iv+密文+tag)}，可识别、可跳过重复加密；</li>
 *     <li>密钥由配置项 {@code nx.config.secret-key} 经 SHA-256 派生为 256 位，密钥务必通过环境变量/配置注入，
 *         切勿与密文同库存放（同库仅等价于混淆）；</li>
 *     <li>未加密的历史明文值可被 {@link #decrypt} 原样透传，保证平滑升级。</li>
 * </ul>
 *
 * @author Walker
 */
@Slf4j
public final class SecretCrypto {

    private SecretCrypto() {
    }

    /**
     * 加密值前缀标识
     */
    public static final String PREFIX = "enc:v1:";

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private static final int TAG_BITS = 128;

    private static final int IV_LENGTH = 12;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 判断值是否为加密格式
     */
    public static boolean isEncrypted(String value) {
        return StringUtils.isNotBlank(value) && value.startsWith(PREFIX);
    }

    /**
     * 加密明文
     *
     * <p>{@code secretKey} 为空时仅记录告警并原样返回明文，便于未配置密钥的环境继续写入
     * （仅等价于混淆，不构成机密性保护，运维需在配置中心补齐密钥）。</p>
     */
    public static String encrypt(String plain, String secretKey) {
        if (StringUtils.isBlank(plain)) {
            return null;
        }
        if (StringUtils.isBlank(secretKey)) {
            log.warn("nx.config.secret-key 未配置，敏感配置将以明文存储（仅混淆，请尽快在配置中心补齐密钥）");
            return plain;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, buildKey(secretKey), new GCMParameterSpec(TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            // 结构：iv + 密文，Base64 编码后加前缀
            byte[] combined = ByteBuffer.allocate(iv.length + ciphertext.length)
                    .put(iv)
                    .put(ciphertext)
                    .array();
            return PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("敏感配置加密失败：" + e.getMessage(), e);
        }
    }

    /**
     * 解密（非加密格式原样透传，兼容历史明文数据）
     *
     * <p>{@code secretKey} 为空时：未加密的历史明文透传，密文因无法解密亦原样返回（带告警），
     * 与加密侧「未配置密钥则明文存储」保持对称语义。</p>
     */
    public static String decrypt(String value, String secretKey) {
        if (StringUtils.isBlank(value) || !isEncrypted(value)) {
            return value;
        }
        if (StringUtils.isBlank(secretKey)) {
            log.warn("nx.config.secret-key 未配置，密文无法解密将原样返回（请补齐密钥后重新保存以恢复明文）");
            return value;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(value.substring(PREFIX.length()));
            byte[] iv = new byte[IV_LENGTH];
            byte[] ciphertext = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, ciphertext, 0, ciphertext.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, buildKey(secretKey), new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("敏感配置解密失败（密钥是否变更？）：" + e.getMessage(), e);
        }
    }

    /**
     * 生成脱敏展示值（保留首尾，如 sk-****abcd；过短直接打码）
     */
    public static String mask(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        if (value.length() <= 8) {
            return "****";
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }

    /**
     * 判断提交值是否为「脱敏展示值」（即未修改，需保留原 Key）
     */
    public static boolean isMaskedForm(String submitted, String plain) {
        return StringUtils.isNotBlank(submitted) && submitted.equals(mask(plain));
    }

    /**
     * 由配置密钥派生 256 位 AES 密钥（SHA-256）
     */
    private static SecretKeySpec buildKey(String secretKey) {
        if (StringUtils.isBlank(secretKey)) {
            throw new IllegalStateException("未配置 nx.config.secret-key，无法对敏感配置进行加解密，请检查配置");
        }
        try {
            byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                    .digest(secretKey.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("AES 密钥派生失败：" + e.getMessage(), e);
        }
    }
}
