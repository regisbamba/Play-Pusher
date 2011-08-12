package play.modules.pusher;

import com.google.gson.annotations.SerializedName;

public class AuthData {

    private String auth;

    @SerializedName("channel_data")
    private String channelData;

    public AuthData(String auth) {
        this.auth = auth;
    }

    public AuthData(String auth, String channelData) {
        this.auth = auth;
        this.channelData = channelData;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getChannelData() {
        return channelData;
    }

    public void setChannelData(String channelData) {
        this.channelData = channelData;
    }


}
