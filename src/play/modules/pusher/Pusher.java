package play.modules.pusher;

import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.exceptions.UnexpectedException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.math.BigInteger;
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
                        + "&body_md5=" + md5(message)
                        + "&name=" + event
                        + (socketId != null ? "&socket_id=" + socketId : "");

        String signature = sha256("POST\n" + path + "\n" + query, this.secret);

        String uri = "http://" + this.host + path + "?" + query +  "&auth_signature=" + signature;

        return WS.url(uri).body(message).post();
    }


    /**
     * Generates an authentication signature for a private channel
     *
     * @param String channel
     * @param String socketId
     * @param String userId
     * @return The auth signature
     */
    public String auth(String channel, String socketId) {
        return this.socketAuth(channel, socketId, null, null);
    }


    /**
     * Generates an authentication signature for a presence channel
     *
     * @param String channel
     * @param String socketId
     * @param String userId
     * @param String userInfo
     * @return The auth signature
     */
    public String presence(String channel, String socketId, String userId, Object userInfo) {
        return this.socketAuth(channel, socketId, userId, userInfo);
    }

    /**
     * Generates a socket signature
     *
     * @param String channel
     * @param String socketId
     * @param String userId
     * @param String userInfo
     * @return The auth signature
     */
    private String socketAuth(String channel, String socketId, String userId, Object userInfo) {

        Gson gson = new Gson();

        // Sent back to the client
        String jsonChannelData = null;

        // Used to generate the auth signature
        String jsonUserData = null;

        if (userId != null) {
            HashMap<String, Object> channelData = new HashMap();
            channelData.put("user_id", userId);
            if (userInfo != null) {
                channelData.put("user_info", userInfo);
            }
           
            jsonChannelData = gson.toJson(channelData);
            jsonUserData = jsonChannelData.replace(":", "=>").replace(",", ", "); // For some reason, the auth signature is only valid when I do this.
        }

        String signature = sha256((socketId + ":"  + channel + (jsonUserData != null ? ":" + jsonUserData : "")), this.secret);

        return "{ \"auth\" : \"" + this.key + ":" + signature + "\"" + (jsonChannelData != null ? " , \"channel_data\" : " + jsonChannelData : "") + "}";
    }


    /*** Utility methods ***/


    private static String byteArrayToString(byte[] data){
        BigInteger bigInteger = new BigInteger(1,data);
    	String hash = bigInteger.toString(16);

    	while(hash.length() < 32 ){
    	  hash = "0" + hash;
    	}

    	return hash;
    }

    private static String md5(String string) {
        try {
            byte[] bytesOfMessage = string.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(bytesOfMessage);
            return byteArrayToString(digest);
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException("No HMac SHA256 algorithm");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("No UTF-8");
        }
    }

    private static String sha256(String string, String secret) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec( secret.getBytes(), "HmacSHA256");

            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            byte[] digest = mac.doFinal(string.getBytes("UTF-8"));
            digest = mac.doFinal(string.getBytes());

            BigInteger bigInteger = new BigInteger(1,digest);
            return String.format("%0" + (digest.length << 1) + "x", bigInteger);

        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException("No HMac SHA256 algorithm");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("No UTF-8");
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key exception while converting to HMac SHA256");
        }
    }
}



