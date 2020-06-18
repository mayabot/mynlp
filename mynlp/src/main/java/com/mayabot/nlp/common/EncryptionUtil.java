package com.mayabot.nlp.common;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 使用Java自带的MessageDigest类
 *
 * @author xiaokui
 */
public class EncryptionUtil {

    // 用来将字节转换成 16 进制表示的字符
    static char hexDigits[] =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static void main(String[] args) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
//        md.update(new byte[]{1, 2});
//        md.update(new byte[]{2, 2});
        byte[] encryptStr = md.digest();
        System.out.println(byte2String(encryptStr));
    }

    public static String md5(String source) {
        try {
            return getHash(source, "MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String md5(InputStream source) {

        BufferedInputStream input = new BufferedInputStream(source);

        try {
            MessageDigest md = MessageDigest.getInstance("Md5");

            byte[] buffer = new byte[512];

            while (input.read(buffer) != -1) {
                md.update(buffer);
            }

            byte[] encryptStr = md.digest(); // 获得密文完成哈希计算,产生128 位的长整数

            return byte2String(encryptStr);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String sha(String source) {
        try {
            return getHash(source, "SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 由于MD5 与SHA-1均是从MD4 发展而来，它们的结构和强度等特性有很多相似之处
     * SHA-1与MD5 的最大区别在于其摘要比MD5 摘要长 32 比特（1byte=8bit，相当于长4byte，转换16进制后比MD5多8个字符）。
     * 对于强行攻击，：MD5 是2128 数量级的操作，SHA-1 是2160数量级的操作。
     * 对于相同摘要的两个报文的难度：MD5是 264 是数量级的操作，SHA-1 是280 数量级的操作。
     * 因而，SHA-1 对强行攻击的强度更大。 但由于SHA-1 的循环步骤比MD5 多（80:64）且要处理的缓存大（160 比特:128 比特），SHA-1 的运行速度比MD5 慢。
     *
     * @param source   需要加密的字符串
     * @param hashType 加密类型 （MD5 和 SHA）
     * @return
     */
    private static String getHash(String source, String hashType) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance(hashType);
        md.update(source.getBytes()); // 通过使用 update 方法处理数据,使指定的 byte数组更新摘要   (为什么需要先使用update方法   有的md5方法中怎么不使用？)
        byte[] encryptStr = md.digest(); // 获得密文完成哈希计算,产生128 位的长整数

        return byte2String(encryptStr);

    }


    private static String byte2String(byte[] encryptStr) {
        char str[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符
        int k = 0; // 表示转换结果中对应的字符位置

        for (int i = 0; i < 16; i++) { // 从第一个字节开始，对每一个字节,转换成 16 进制字符的转换
            byte byte0 = encryptStr[i]; // 取第 i 个字节
            str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换, >>> 为逻辑右移，将符号位一起右移
            str[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
        }

        return new String(str); // 换后的结果转换为字符串
    }

}