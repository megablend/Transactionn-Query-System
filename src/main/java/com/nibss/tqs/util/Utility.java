package com.nibss.tqs.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;

/**
 * Created by eoriarewo on 9/14/2016.
 */
public class Utility {

    public static synchronized boolean isEmptyBigDecimal(BigDecimal decimal) {
        return null == decimal || decimal.compareTo(BigDecimal.ZERO) <= 0;
    }

    public static synchronized BigDecimal getShare(BigDecimal partyShare, BigDecimal fee, boolean isPercentage) {
        if (isEmptyBigDecimal(partyShare) || isEmptyBigDecimal(fee)) {
            return BigDecimal.ZERO;
        }

        if (isPercentage) {
            return partyShare.multiply(fee);
        } else {
            return partyShare;
        }
    }

    public synchronized static String buildPaymentReceipt(String sessionId) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?> ");
        buffer.append("<PaymentChannelReceipt>");
        buffer.append(String.format("<SessionID>%s</SessionID>", sessionId));
        buffer.append("<EchoData>xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</EchoData>");
        buffer.append("<HashValue>xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</HashValue>");
        buffer.append("</PaymentChannelReceipt>");
        return buffer.toString();
    }

    public static synchronized String encryptAES(String plainText, String aesKey) throws RuntimeException {
        SecretKeySpec sks = new SecretKeySpec(hexStringToByteArray(aesKey), "AES");
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, sks, cipher.getParameters());
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return byteArrayToHexString(encrypted);
        } catch (Exception ex) {
            throw new RuntimeException("could not do aes encryption", ex);
        }
    }

    private static String byteArrayToHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase();
    }

    private static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }
}
