package org.mints.masterslave.utils;

import org.mints.masterslave.logger.MsLogger;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author huadeng
 * @version 0.0.0.0
 * @docRoot
 * @date Created 17:55 2017/9/08
 * @modified By luke
 */
public class EncryptAESUtil {

    private static MsLogger logger = MsLogger.getLogger(EncryptAESUtil.class);


    /**
     * 单例设计模式（保证类的对象在内存中只有一个）
     * 1、把类的构造函数私有
     * 2、自己创建一个类的对象
     * 3、对外提供一个公共的方法，返回类的对象
     */
    private EncryptAESUtil() {
    }

    private static final EncryptAESUtil instance = new EncryptAESUtil();

    /**
     * 返回类的对象
     */
    public static EncryptAESUtil getInstance() {
        return instance;
    }

    /**
     * 加密
     *
     * @param content  需要加密的内容
     * @param password 加密密码
     * @return
     */
    public String encryptHex(String content, String password) {
        return parseByte2HexStr(encrypt(content, password));
    }

    /**
     * 解密
     *
     * @param content  待解密内容
     * @param password 解密密钥
     * @return
     */
    public String decryptHex(String content, String password) {
        return new String(decrypt(parseHexStr2Byte(content), password));
    }


    /**
     * 加密
     *
     * @param content  需要加密的内容
     * @param password 加密密码
     * @return
     */
    public byte[] encrypt(String content, String password) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(password.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(byteContent);
            return result; // 加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密
     *
     * @param content  待解密内容
     * @param password 解密密钥
     * @return
     */
    public byte[] decrypt(byte[] content, String password) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(password.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(content);
            return result; // 加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     */
    public String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    ////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////

    private static String key = DigestUtils.md5DigestAsHex("suit".getBytes()).substring(8, 24);
    private static String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding"; // optional value AES/DES/DESede
    private static String vis = "";//DigestUtils.md5DigestAsHex(plainPwd.getBytes()).substring(8, 24);//MD5.GetMD5Code("goodmoney_2020").substring(8, 24);

    public static void init(String akey, String plainPwd){
        key = DigestUtils.md5DigestAsHex(akey.getBytes()).substring(8, 24);
        vis = DigestUtils.md5DigestAsHex(plainPwd.getBytes()).substring(8, 24);
    }

    private static SecretKeySpec getKey(String strKey) throws Exception {
        byte[] arrBTmp = strKey.getBytes();
        byte[] arrB = new byte[16]; // 创建一个空的16位字节数组（默认值为0）

        for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
            arrB[i] = arrBTmp[i];
        }
        SecretKeySpec skeySpec = new SecretKeySpec(arrB, "AES");
        return skeySpec;
    }

    public static String encrypt(String message) {
        try {
            SecretKeySpec skeySpec = getKey(key);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(vis.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(message.getBytes("UTF-8"));
            return new BASE64Encoder().encode(encrypted);
        } catch (Exception e) {
        }
        return "";
    }



    public static String detrypt(String message) {
        try {
            String newkey = key;
            if (!StringUtils.isEmpty(newkey)) {
                String dMsg = message.replace(" ", "+");
                byte[] res = new BASE64Decoder().decodeBuffer(dMsg);
                SecretKeySpec skeySpec = getKey(newkey);
                Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
                IvParameterSpec iv = new IvParameterSpec(vis.getBytes());
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
                byte[] detrypted = cipher.doFinal(res);
                return new String(detrypted, "UTF-8");
            }
        } catch (Exception e) {
            logger.error("报文解密失败-msg={}, error={}", message, e.getMessage());
            throw new RuntimeException(e);
        }

        return "";
    }

    public static String detryptFailReturnSrc(String message) {
        try {
            return detrypt(message);
        } catch (Exception e) {
            //logger.error("报文解密失败-返回原文-msg={}, error={}", message, e.getMessage());
            return message;
        }
    }

    public static void main(String[] args) {
        EncryptAESUtil.init("su","mints@0419");

        System.out.println(DigestUtils.md5DigestAsHex("goodmoney_2020".getBytes()).substring(8, 24));
    }
}
