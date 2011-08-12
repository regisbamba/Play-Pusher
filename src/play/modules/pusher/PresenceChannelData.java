package play.modules.pusher;

import com.google.gson.annotations.SerializedName;

public class PresenceChannelData {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("user_info")
    private Object userInfo;

    public PresenceChannelData(String userId, Object userInfo) {
        this.setUserId(userId);
        this.setUserInfo(userInfo);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Object getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(Object userInfo) {
        this.userInfo = userInfo;
    }
    
}
