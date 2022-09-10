package cn.wildfirechat.app.pojo;

public class PojoApplicationEntity {
    public String targetId;
    public String secret;
    public String name;
    public String description;
    public String portraitUrl;
    // 应用时有效
    public String mobileUrl;
    // 应用时有效
    public String desktopUrl;
    public String serverUrl;
    // 应用时，表示是否是全局应用；频道是，表示是否是广播号
    public boolean global;
    // 0， 创建应用；1，创建频道；2，创建机器人
    public int type;
    public long updateDt;
    public long createDt;
}
