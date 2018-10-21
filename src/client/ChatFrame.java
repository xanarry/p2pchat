package client;

import com.google.gson.Gson;
import common.Message;
import utils.Tools;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatFrame extends JFrame {
    private JButton sendBt;
    private JTextField inputField;
    private JTextPane chatContent;

    private Socket socket;

    private String toUser;//对方用户名
    private String toHost;//对方主机地址
    private int    toHostPort;//对方主机监听端口号

    private String fromUser;//本地用户


    /**
     * 初始GUI, 需要在其他其他函数调用之前调用
     */
    private void initGUI() {
        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        chatContent = new JTextPane(); // 创建一个文本域
        chatContent.setEditable(false);    // 设置文本域不可编辑
        chatContent.setContentType("text/html");


        // 创建一个滚动面板，将文本域作为其显示组件
        JScrollPane showPanel = new JScrollPane(chatContent);

        JPanel inputPanel = new JPanel(); // 创建一个JPanel面板
        inputField = new JTextField(40);  // 创建一个文本框

        sendBt = new JButton("发送"); // 创建一个发送按钮

        Label label = new Label("input:"); // 创建一个标签
        inputPanel.add(label);  // 将标签添加到JPanel面板
        inputPanel.add(inputField); // 将文本框添加到JPanel面板
        inputPanel.add(sendBt);  // 将按钮添加到JPanel面板
        //inputPanel.add(exitBt);


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Message message = new Message();

                message.setType(Message.CTRL);
                message.setFromUser(fromUser);
                message.setFromHost(Tools.getLocalHostLanIP());

                message.setToUser(toUser);
                message.setToHost(toHost);

                message.setText(Message.STOP);
                message.setLength(Message.STOP.length());
                try {
                    if (socket != null) {
                        Tools.writeToSocketStream(socket.getOutputStream(), new Gson().toJson(message));
                        if (!socket.isClosed()) {
                            socket.close();
                        }
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                } finally {
                    dispose();
                }
            }
        });

        // 将滚动面板和JPanel面板添加到JFrame窗口
        this.add(showPanel, BorderLayout.CENTER);
        this.add(inputPanel, BorderLayout.SOUTH);
        this.setTitle("聊天窗口");
        this.setSize(600, 600);
        this.setVisible(true);
    }


    /**
     * 被动聊天, 将建立好的套接字传递过来
     * @param fromUser 本地用户名
     * @param chatSocket 建立好的套接字
     */
    public ChatFrame(String fromUser, String toUser, String toHost, Socket chatSocket) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.toHost = toHost;
        socket = chatSocket;
        initGUI();
        startChat(socket);
    }

    /**
     * 主动发起聊天
     * @param fromUser 本地用户名
     * @param toUser 对方用户名
     * @param toHost 对方IP地址
     * @param toHostPort 对方开放端口
     */
    public ChatFrame(String fromUser, String toUser, String toHost, int toHostPort) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.toHost = toHost;
        this.toHostPort = toHostPort;
        initGUI();
        constructSocketConnection();
    }


    /**
     * 主动向目标主机发起聊天请求
     */
    private void constructSocketConnection() {
        socket = new Socket();
        try {
            System.out.println("connect to: " + toHost + ":" + toHostPort);
            socket.connect(new InetSocketAddress(toHost, toHostPort));
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            Message message = new Message();
            message.setType(Message.CTRL);//发出控制信息
            message.setFromUser(fromUser);
            message.setFromHost(Tools.getLocalHostLanIP());
            message.setToUser(toUser);
            message.setToHost(toHost);
            message.setText(fromUser);
            message.setLength(fromUser.length());

            Tools.writeToSocketStream(out, new Gson().toJson(message));
            String msg = Tools.readSocketStream(in);
            System.out.println("request return: " + msg);
            Message retMessage = new Gson().fromJson(msg.trim(), Message.class);
            //检查对方是否同意连接
            if (retMessage.getType().equals(Message.CTRL) && retMessage.getText().equals(message.getText())) {
                //正式开始聊天
                startChat(socket);
            } else if (retMessage.getText().equals(Message.REFU)) {
                dispose();
                JOptionPane.showMessageDialog(null, toUser + "对方拒绝了聊天请求", "提示", JOptionPane.ERROR_MESSAGE);
                in.close();
                out.close();
                socket.close();
            } else {
                dispose();
                in.close();
                out.close();
                socket.close();
            }
        } catch (IOException e) {
            dispose();
            JOptionPane.showMessageDialog(null, toUser + "建立连接失败: " + e.getMessage(), "提示", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * 开始正式聊天
     * @param socket
     */
    private void startChat(Socket socket) {
        //消息发送线程(当前线程)
        setTitle("正在与" + toUser + "聊天");
        this.sendBt.addActionListener(new ActionListener() {// 为按钮添加一个监听事件
            public void actionPerformed(ActionEvent e) {// 重写actionPerformed方法
                String content = inputField.getText().trim();// 获取输入的文本信息
                // 判断输入的信息是否为空
                if (!content.equals("")) {//trim见注释
                    // 如果不为空，将输入的文本追加到到聊天窗口
                    Message message = new Message();

                    message.setType(Message.TEXT);
                    message.setFromUser(fromUser);
                    message.setFromHost(Tools.getLocalHostLanIP());

                    message.setToUser(toUser);
                    message.setToHost(toHost);

                    message.setText(content);
                    message.setLength(content.length());

                    try {
                        Tools.writeToSocketStream(socket.getOutputStream(), new Gson().toJson(message));
                        updateContent(message);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                }
                inputField.setText("");// 将输入的文本域内容置为空
            }
        });

        //消息接收线程
        Thread recvThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String msg = Tools.readSocketStream(socket.getInputStream());
                        Message message = new Gson().fromJson(msg.trim(), Message.class);
                        if (message.getType().equals(Message.TEXT)) {
                            updateContent(message);
                        } else {
                            dispose();
                            JOptionPane.showMessageDialog(null, toUser + "结束了此次聊天", "提示", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        recvThread.start();
    }

    private synchronized void updateContent(Message message) {
        String content = "";
        System.out.println("updateContent: " + message + " " + fromUser);
        long time = new Date().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (message.getToUser().equals(this.fromUser)) { //接收的消息
            content = String.format("<b style=\"color:red; font-size:1em\">%s(%s) %s</b><br><b style=\"font-size:1.2em\">%s</b><br>", message.getFromUser(), message.getFromHost(), sdf.format(time), message.getText());
        } else { //发出去的消息
            content = String.format("<b style=\"color:green; font-size:1em\">%s(%s) %s</b><br><b style=\"font-size:1.2em\">%s</b><br>", message.getFromUser(), message.getFromHost(), sdf.format(time), message.getText());
        }

        HTMLDocument doc = (HTMLDocument) chatContent.getStyledDocument();
        try {
            doc.insertAfterEnd(doc.getCharacterElement(doc.getLength()), content);
            //设置总是显示最底部消息
            chatContent.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

