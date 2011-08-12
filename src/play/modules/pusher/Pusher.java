package play.modules.pusher;

import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.exceptions.UnexpectedException;
import com.google.gson.Gson;

/**
 * Pusher Java Library
 * 
 * @author regis.bamba@gmail.com
 */
public class Pusher {

    private final String host = "api.pusherapp.com";
    private final String appId;
    private final String key;
    private final String secret;


    public Pusher() {
        if (!Play.configuration.containsKey("pusher.appId")) {
            throw new UnexpectedException("Module Pusher requires that you specify pusher.appId in your application.conf");
        }
        if (!Play.configuration.containsKey("pusher.key")) {
            throw new UnexpectedException("Module Pusher requires that you specify pusher.key in your application.conf");
        }
        if (!Play.configuration.containsKey("pusher.secret")) {
            throw new UnexpectedException("Module Pusher requires that you specify pusher.secret in your application.conf");
        }
        this.appId = Play.configuration.getProperty("pusher.appId");
        this.key = Play.configuration.getProperty("pusher.key");
        this.secret = Play.configuration.getProperty("pusher.secret");
    }

    public Pusher(String appId, String key, String secret) {
        this.appId = appId;
        this.key = key;
        this.secret = secret;
    }

    /**
     * Triggers an event on a channel and delivers a message to all the suscribers suscribers.
     * 
     * @param String channel
     * @param String event
     * @param String message
     * @return The response from the Pusher Server
     */
    public HttpResponse trigger(String channel, String event, String message) {
        return this.trigger(channel, event, message, null);
    }

    /**
     * Triggers an event on a channel and delivers a message to all the suscribers suscribers
     * excluding the passed socketId.
     *
     * @param String channel
     * @param String event
     * @param String message
     * @param String socketId
     * @return The response from the Pusher Server
     */
    public HttpResponse trigger(String channel, String event, String message, String socketId) {

        String path = "/apps/" + this.appId + "/channels/" + channel + "/events";
        String query = "auth_key=" + this.key
                        + "&auth_timestamp=" + (System.currentTimeMillis() / 1000)
                        + "&auth_version=1.0"
                        + "&body_md5=" + PusherUtil.md5(message)
                        + "&name=" + event
                        + (socketId != null ? "&socket_id=" + socketId : "");

        String signature = PusherUtil.sha256("POST\n" + path + "\n" + query, this.secret);

        String uri = "http://" + this.host + path + "?" + query +  "&auth_signature=" + signature;

        return WS.url(uri).body(message).post();
    }


    /**
     * Generates an authentication signature for a private channel
     *
     * @param String socketId
     * @param String channel
     * @param String userId
     * @return The auth signature
     */
    public String createAuthString(String socketId, String channel) {
        Gson gson = new Gson();

        String signature = PusherUtil.sha256((socketId + ":"  + channel), this.secret);
        AuthData auth = new AuthData(this.key + ":" + signature);

        return gson.toJson(auth);
    }


    /**
     * Generates an authentication signature for a presence channel
     *
     * @param String socketId
     * @param String channel
     * @param PresenceChannelData channelData
     * @return The auth signature
     */
    public String createAuthString(String socketId, String channel, PresenceChannelData channelData) {

        Gson gson = new Gson();
        String jsonChannelData = jsonChannelData = gson.toJson(channelData);

        String signature = PusherUtil.sha256((socketId + ":"  + channel +  ":" + jsonChannelData ), this.secret);
        AuthData auth = new AuthData(this.key + ":" + signature, jsonChannelData);

        return gson.toJson(auth);
    }   
    
}



