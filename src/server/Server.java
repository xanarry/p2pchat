
package server;


import com.google.gson.Gson;
import common.UserRegisterInfo;
import utils.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {
    private static final int serverPort = 1024;
    private static List<UserRegisterInfo> userList = new ArrayList<>();

    public static void main(String[] argv) {
       /* TimerTask task = new TimerTask() {
            @Override
            public void run() {
                updateUserList(null);
            }
        };
        java.util.Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, 30000);*/


        Thread waitForRegisterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket registerServer = new ServerSocket(1024);
                    while (true) {
                        Socket socket = registerServer.accept();
                        InputStream inputStream = socket.getInputStream();
                        OutputStream outputStream = socket.getOutputStream();
                        String message = Tools.readSocketStream(inputStream);//读取用户登记信息
                        UserRegisterInfo userRegisterInfo = new Gson().fromJson(message.trim(), UserRegisterInfo.class);
                        List<UserRegisterInfo> list = updateUserList(userRegisterInfo);//更新登记信息
                        Tools.writeToSocketStream(outputStream, new Gson().toJson(list));//回发在线列表
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        waitForRegisterThread.setName("waitForRegisterThread");
        System.out.println("start to wait user registration, port:" + serverPort);
        waitForRegisterThread.start();
    }

    private static synchronized List<UserRegisterInfo> updateUserList(UserRegisterInfo userRegisterInfo) {
        long curTime = new Date().getTime();
        //删除过期登陆用户
        userList.removeIf(tmp -> curTime - tmp.getLastRegisterTime() > 30000);

        //更新当前注册用户
        if (userRegisterInfo != null) {
            userList.removeIf(tmp -> tmp.getUserName().equals(userRegisterInfo.getUserName()));
            userRegisterInfo.setLastRegisterTime(curTime);
            userList.add(userRegisterInfo);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(format.format(curTime) + " register [" + userRegisterInfo.getUserName() + " (" + userRegisterInfo.getIpAddress() + ":" + userRegisterInfo.getPort() + ")" + "]");
        }
        return userList;
    }
}


