package com.chirag.news.util;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
public class EncryptionUtil {
    private static final Logger LOG = LoggerFactory.getLogger(EncryptionUtil.class);
    private static final String initVector = "oX8PmA5970UywqXy";

    public EncryptionUtil() {
    }

    protected static String encrypt(String key, String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec("oX8PmA5970UywqXy".getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(1, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeBase64String(encrypted);
        } catch (Exception var6) {
            LOG.error("Error in encrypting key : {} ", value, var6);
            return null;
        }
    }

    protected static String decrypt(String key, String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec("oX8PmA5970UywqXy".getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(2, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));
            return new String(original);
        } catch (Exception var6) {
            LOG.error("Error in decrypting key : {} ", encrypted, var6);
            return null;
        }
    }
}
