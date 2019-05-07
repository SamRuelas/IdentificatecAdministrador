package android.com.identificatecadministrador;

import java.util.Locale;

public class Converters {

    public Converters() {
    }

    public String getHexString(byte[] b, int length)
    {
        String result = "";
        Locale loc = Locale.getDefault();

        for (int i = 0; i < length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
            result += "";
        }
        return result.toUpperCase(loc);
    }

    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


    public String asciiHexFromString(String str) {
        String hexAscii = "";
        for (int i=0; i<str.length(); i++) {
            hexAscii += Integer.toHexString((int)str.charAt(i));
        }
        return hexAscii;
    }

    public String stringFromHexAscii(String ascii) {
        String str = "";
        for (int i=0; i<=ascii.length()-2; i+=2) {
            System.out.println("ascii:"+Integer.parseInt(ascii.substring(i, i+2),16));
            System.out.println("String:"+(char) Integer.parseInt(ascii.substring(i, i+2),16));
            str += Character.toString((char) Integer.parseInt(ascii.substring(i, i+2),16));
        }
        return str;
    }
}
