package com.simon.ota.ble.util;

import android.text.TextUtils;

@SuppressWarnings("all")
public class OtaConvertUtil {
    private static final String qppHexStr = "0123456789ABCDEF";

    public static byte loUint16(short v) {
        return (byte) (v & 0xFF);
    }

    public static byte hiUint16(short v) {
        return (byte) (v >> 8 & 0xFF);
    }

    public static byte[] hexStr2Bytes(String hexString) {
        if (TextUtils.isEmpty(hexString)) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() >> 1;
        char[] hexChars = hexString.toCharArray();
        int i = 0;
        do {
            int checkChar = qppHexStr.indexOf(hexChars[i]);
            if (checkChar == -1)
                return null;
            i++;
        } while (i < hexString.length());
        byte[] dataArr = new byte[length];
        for (i = 0; i < length; i++) {
            int strPos = i * 2;
            dataArr[i] = (byte) (charToByte(hexChars[strPos]) << 4 | charToByte(hexChars[strPos + 1]));
        }
        return dataArr;
    }

    private static byte charToByte(char c) {
        return (byte) qppHexStr.indexOf(c);
    }

    /**
     * Convert byte[] to hex
     * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
//            hv = hv + " ";
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * @param iType    设备类型
     * @param iCmdcode 命令码
     * @param src      输入的参数
     * @return
     */
    public static int getChecksum(int iType, int iCmdcode, byte[] src) {
        if (src == null || src.length <= 0) {
            return (iType + iCmdcode);
        }
        int iChecksum = 0;
        for (int i = 0; i < src.length; i++) {
            iChecksum += (0x00FF & src[i]);

        }
        iChecksum += iType + iCmdcode;
        return iChecksum;
    }

    /**
     * @param src 输入的参数
     * @return
     */
    public static int getOutChecksum(int[] src) {
        if (src == null || src.length <= 0) {
            return 0;
        }
        int iChecksum = 0;
        for (int i = 0; i < src.length; i++) {
            iChecksum += src[i];
        }
        return iChecksum;
    }

    /**
     * @param iInt ,iDec分别表示整数和小数部分
     * @return
     */
    public static String byteToTemp(int iInt, int iDec) {
        String strRes = String.valueOf(iInt);
        strRes += ".";
        if (iDec >= 0 && iDec <= 9) {
            strRes += "0" + String.valueOf(iDec);
        } else {
            strRes += String.valueOf(iDec);
        }
        return strRes;
    }

    public static String byteToTempNew(String strInt, String strDec) {
        String strRes = strInt;
        strRes += ".";
        int iDec = Integer.parseInt(strDec);
        if (iDec >= 0 && iDec <= 9) {
            strRes += "0" + strDec;
        } else {
            strRes += strDec;
        }
        return strRes;
    }

    public static int bytes2Int(byte data1, byte data2) {
        return ((data1 & 0xff) << 8) + (data2 & 0xff);
    }

}

