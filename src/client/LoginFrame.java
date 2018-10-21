package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.UserRegisterInfo;
import utils.Tools;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static client.Client.serverPort;

public class LoginFrame extends JFrame {
    private JTextField serverAddressField = new JTextField(10);
    private JTextField userNameField = new JTextField(10);
    private JLabel serverLabel = new JLabel("服务器");//创建标签
    private JLabel userNameLabel = new JLabel("用户名");
    private JButton registerBtn = new JButton("确认");//创建按钮
    private JButton exitBtn = new JButton("退出");
    private JPanel jp = new JPanel();

    public LoginFrame() {
        setTitle("登陆");
        jp.setLayout(null);

        serverLabel.setBounds(30, 20, 80, 30);       //设置各个控件的位置，及关系
        jp.add(serverLabel);

        userNameLabel.setBounds(30, 70, 80, 30);
        jp.add(userNameLabel);

        serverAddressField.setBounds(80, 20, 180, 30);
        jp.add(serverAddressField);

        userNameField.setBounds(80, 70, 180, 30);
        jp.add(userNameField);

        registerBtn.setBounds(80, 120, 80, 30);
        exitBtn.setBounds(180, 120, 80, 30);
        jp.add(registerBtn);
        jp.add(exitBtn);

        registerBtn.addActionListener(new ActionListener() {//为确定按钮添加时间处理
            public void actionPerformed(ActionEvent e) {
                //提取IP地址
                String serverAddress = serverAddressField.getText();
                //验证IP地址
                final String regExp = "\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}";
                Matcher matcher = Pattern.compile(regExp).matcher(serverAddress);

                //提取登陆名
                String fromName      = userNameField.getText();

                if (matcher.matches() && fromName.trim().length() > 0) {
                    register(serverAddress, fromName);
                } else {
                    JOptionPane.showMessageDialog(null, "对不起, 服务器地址或用户名输入不正确");
                }
            }
        });


        exitBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        this.add(jp);
        this.setBounds(550, 350, 320, 180);//设置窗口大小
        this.setVisible(true);

    }


    private void register(String serverAddress, String fromName) {
        Socket socket = new Socket();
        try {
            //注册服务器为固定端口号1024, 设置超时10秒
            socket.connect(new InetSocketAddress(serverAddress, serverPort), 10000);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            //构造当前用户的的登记信息, 包括用户名, IP地址,
            UserRegisterInfo userRegisterInfo = new UserRegisterInfo();
            userRegisterInfo.setUserName(fromName);
            userRegisterInfo.setIpAddress(Tools.getLocalHostLanIP());

            //发送登记信息
            Tools.writeToSocketStream(out, new Gson().toJson(userRegisterInfo));
            String message = Tools.readSocketStream(in);

            //接收注册服务的响应消息, 并判断是否注册成功
            List<UserRegisterInfo> onlineUserList = new Gson().fromJson(message.trim(), new TypeToken<List<UserRegisterInfo>>(){}.getType());
            if (onlineUserList != null) {
                this.dispose();
                new IndexFrame(fromName, serverAddress);
            }

        } catch (IOException e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(this, e1.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

}
