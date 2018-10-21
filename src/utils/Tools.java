package utils;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

public class Tools {
    public static String decToHex(int dec) {
        String hex = "";
        while (dec > 0) {
            hex = "0123456789ABCDEF".charAt(dec % 16) + hex;
            dec /= 16;
        }
        return (new String[] {"", "0", "00"})[hex.length() > 3 ? 0 : 3 - hex.length()] + hex; //在前面补零保持3的长度
    }

    public static int hexToDec(String hex) {
        int dec = 0;
        for (int i = 0; i < hex.length() && hex.charAt(i) != '\0'; i++) {
            dec = (dec * 16) + "0123456789ABCDEF".indexOf(hex.charAt(i));
        }
        return dec;
    }

    public static String readSocketStream(InputStream in) throws IOException {
        int bufLen = 4100;
        byte[] buff = new byte[3]; //3字节长度(0xFFF)描述 + 4095字节消息
        in.read(buff, 0, 3);//先读取前三个字节描述的此条消息长度
        int msgLen = Tools.hexToDec(new String(buff, StandardCharsets.UTF_8));

        buff = new byte[bufLen];
        in.read(buff, 0, msgLen); //读取消息实体
        return new String(buff, StandardCharsets.UTF_8);
    }

    public static void writeToSocketStream(OutputStream out, String message) throws IOException {
        int len = message.getBytes().length;
        if (len > 0xFFF) {
            throw new IOException("message is too long to send");
        }
        String msg = (Tools.decToHex(len) + message);
        out.write(msg.getBytes());
        out.flush();
    }


    public static String getLocalHostLanIP() {
        //获取本机在局域网中的IP
        Enumeration<?> allNetInterfaces;
        InetAddress IP = null;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                //System.out.println(netInterface.getName());
                if (netInterface.isLoopback() || netInterface.isPointToPoint()) {
                    continue;
                }

                Enumeration<?> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress tempIP = (InetAddress) addresses.nextElement();
                    if (tempIP != null && tempIP instanceof Inet4Address) {
                        //System.out.println("本机的IP=" + tempIP.getHostAddress());
                        IP = tempIP;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        if (IP != null) {
            return IP.toString().replaceAll("/", "").trim();
            //return "127.0.0.1";
        } else {
            return "";
        }
    }
}
