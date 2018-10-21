package common;

public class Message {
    public static final String CTRL = "CTRL";
    public static final String STOP = "STOP";
    public static final String REFU = "REFUSE";
    public static final String TEXT = "TEXT";
    private String type;
    private String toUser;
    private String toHost;
    private String fromUser;
    private String fromHost;
    private String text;
    private int length;

    public Message() { }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getToHost() {
        return toHost;
    }

    public void setToHost(String toHost) {
        this.toHost = toHost;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getFromHost() {
        return fromHost;
    }

    public void setFromHost(String fromHost) {
        this.fromHost = fromHost;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "Message{" +
                "toUser='" + toUser + '\'' +
                ", toHost='" + toHost + '\'' +
                ", fromUser='" + fromUser + '\'' +
                ", fromHost='" + fromHost + '\'' +
                ", text='" + text + '\'' +
                ", length='" + length + '\'' +
                '}';
    }
}
