package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.Message;
import common.UserRegisterInfo;
import utils.Tools;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static client.Client.serverPort;

public class IndexFrame extends JFrame {
    private DefaultListModel model = new DefaultListModel(); //更新列表数据的model
    private JList jListOfUser; //列表

    private String fromUser; //本用户用户名
    private String serverAddress; //注册管理服务器
    private ServerSocket serverSocket;
    private int    listeningPort = 0; //0时系统动态分配端口号

    /**
     * @param fromUser 本用户
     * @param serverAddress 注册管理服务器地址
     */
    public IndexFrame(String fromUser, String serverAddress) {
        this.fromUser = fromUser;
        this.serverAddress = serverAddress;

        init(); //初始化界面
        startThreadToWaitChatConnection(); //等待其他peer发起的连接
        startThreadToUpdateState(); //向注册服务器更新自己的状态
    }

    private void init() {
        setLayout(null);
        jListOfUser = new JList(model);
        setTitle("正在加载好友...");

        jListOfUser.setBounds(0, 0, 200, 500);
        jListOfUser.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jListOfUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JList list = (JList) e.getSource();
                if (e.getClickCount() == 2) {
                    // Double-click detected
                    int index = list.locationToIndex(e.getPoint());
                    //model中保存字符串: user.getUserName() + " " + user.getIpAddress() + " " + user.getPort()
                    String[] userInfo = model.get(index).toString().split(" ");
                    //打开新的聊天窗口
                    new ChatFrame(fromUser, userInfo[0], userInfo[1], Integer.parseInt(userInfo[2]));
                }
            }
        });


        JScrollPane scrollPane = new JScrollPane(
                jListOfUser,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        setContentPane(scrollPane);

        setSize(300, 500);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

        });
    }

    private void startThreadToWaitChatConnection() {
        //监听建立聊天的请求
        Thread waitingForConnectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(0);//系统随机生成端口号
                    listeningPort = serverSocket.getLocalPort();
                    while (true) {
                        System.out.println("waiting for chat connection at:" + listeningPort);
                        //接收peer发出的控制数据, 根据不同的状态选择不同的操作
                        Socket newChatSocket = serverSocket.accept();
                        String msg = Tools.readSocketStream(newChatSocket.getInputStream());
                        Message message = new Gson().fromJson(msg.trim(), Message.class);
                        if (message.getType().equals(Message.CTRL) && message.getText().length() > 0) {
                            int choice = JOptionPane.showConfirmDialog(null, message.getText() + " 发起了新的聊天,是否接受", "新的聊天", JOptionPane.OK_CANCEL_OPTION);
                            if (choice == JOptionPane.OK_OPTION) {
                                //创建新的聊天窗口
                                Tools.writeToSocketStream(newChatSocket.getOutputStream(), new Gson().toJson(message));
                                new ChatFrame(fromUser, message.getFromUser(), message.getFromHost(), newChatSocket);
                            } else {
                                //发送拒绝连接消息
                                message.setText(Message.REFU);
                                Tools.writeToSocketStream(newChatSocket.getOutputStream(), new Gson().toJson(message));
                                //关闭套接字
                                newChatSocket.close();
                            }
                        } else {
                            newChatSocket.close();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //开启等待线程
        waitingForConnectionThread.start();
    }


    private void startThreadToUpdateState() {
        //使用定时器定期检查用户列表
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Socket socket = new Socket();
                try {
                    socket.connect(new InetSocketAddress(serverAddress, serverPort), 10000);
                    //创建用户信息
                    UserRegisterInfo userRegisterInfo = new UserRegisterInfo();
                    userRegisterInfo.setUserName(fromUser);
                    userRegisterInfo.setPort(listeningPort);
                    userRegisterInfo.setIpAddress(Tools.getLocalHostLanIP());

                    //发送给服务器登记
                    Tools.writeToSocketStream(socket.getOutputStream(), new Gson().toJson(userRegisterInfo));
                    String message = Tools.readSocketStream(socket.getInputStream());
                    List<UserRegisterInfo> onlineUserList = new Gson().fromJson(message.trim(), new TypeToken<List<UserRegisterInfo>>() {}.getType());

                    //更新在线列表
                    System.out.println(onlineUserList);
                    updateUserList(onlineUserList);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        };
        java.util.Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, 15000);//每隔15秒登记一次
    }


    private void updateUserList(List<UserRegisterInfo> userList) {
        this.model.clear();
        for (UserRegisterInfo user : userList) {
            if (!user.getUserName().equals(fromUser)) {
                model.addElement(user.getUserName() + " " + user.getIpAddress() + " " + user.getPort());
            }
        }
        setTitle("[" + fromUser + "]" + model.getSize() + "个好友在线");
    }
}
