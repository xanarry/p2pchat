package common;

public class UserRegisterInfo {
    private String userName; //用户名
    private String ipAddress; //用户IP
    private int    port;// 监听端口
    private long   lastRegisterTime; //最近一次登记时间

    public UserRegisterInfo() { }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public long getLastRegisterTime() {
        return lastRegisterTime;
    }

    public void setLastRegisterTime(long lastRegisterTime) {
        this.lastRegisterTime = lastRegisterTime;
    }

    @Override
    public String toString() {
        return "UserRegisterInfo{" +
                "userName='" + userName + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                ", lastRegisterTime=" + lastRegisterTime +
                '}';
    }
}
