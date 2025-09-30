package ir.naja.pushsdk.android;

import java.io.UnsupportedEncodingException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class AES {
    private static final String AES_MODE = "AES/CBC/PKCS5Padding";
    private static final String CHARSET = "UTF-8";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static String password;
    private static final byte[] ivBytes = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    public static void setKey(String key) {
        password = key;
    }

    private static SecretKeySpec generateKey(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }

    public static String encrypt(String message) throws GeneralSecurityException {
        if (password == null) {
            throw new GeneralSecurityException("Key is null");
        } else {
            try {
                SecretKeySpec key = generateKey(password);
                byte[] cipherText = encrypt(key, ivBytes, message.getBytes("UTF-8"));
                return Base64.encodeToString(cipherText, 2);
            } catch (Exception var3) {
                Exception e = var3;
                throw new GeneralSecurityException(e);
            }
        }
    }

    private static byte[] encrypt(SecretKeySpec key, byte[] iv, byte[] message) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(1, key, ivSpec);
        return cipher.doFinal(message);
    }

    public static String decrypt(String base64EncodedCipherText) throws GeneralSecurityException {
        if (password == null) {
            throw new GeneralSecurityException("Key is null");
        } else {
            try {
                SecretKeySpec key = generateKey(password);
                byte[] decodedCipherText = Base64.decode(base64EncodedCipherText, 2);
                byte[] decryptedBytes = decrypt(key, ivBytes, decodedCipherText);
                return new String(decryptedBytes, "UTF-8");
            } catch (Exception var4) {
                Exception e = var4;
                throw new GeneralSecurityException(e);
            }
        }
    }

    private static byte[] decrypt(SecretKeySpec key, byte[] iv, byte[] decodedCipherText) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(2, key, ivSpec);
        return cipher.doFinal(decodedCipherText);
    }

    private AES() {
    }
}