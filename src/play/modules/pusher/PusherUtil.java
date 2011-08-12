package play.modules.pusher;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class PusherUtil {

    public static String byteArrayToString(byte[] data){
        BigInteger bigInteger = new BigInteger(1,data);
    	String hash = bigInteger.toString(16);

    	while(hash.length() < 32 ){
    	  hash = "0" + hash;
    	}

    	return hash;
    }

    public static String md5(String string) {
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

    public static String sha256(String string, String secret) {
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
