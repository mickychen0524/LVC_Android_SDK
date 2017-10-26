package project.labs.avviotech.com.chatsdk.net.protocol;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import project.labs.avviotech.com.chatsdk.model.User;
import project.labs.avviotech.com.chatsdk.net.model.DeviceModel;

/**
 * Created by jinhy on 2016-12-05.
 */

public class WiFiP2PProtocol {

    public JSONObject getRequestUserInfo(User user) {
        JSONObject json = new JSONObject();
        jsonPut(json, "name", user.getName());
        jsonPut(json, "ip", user.getIp());
        jsonPut(json, "key", user.getKey());
        jsonPut(json, "type", "p2p-request");
        return json;
    }

    public JSONObject getIncomingData(User user) {
        JSONObject json = new JSONObject();
        jsonPut(json, "data","incomingCall");
        jsonPut(json, "type", "incomingCall");
        return json;
    }

    public JSONObject getAnswerUserInfo(User user) {
        JSONObject json = new JSONObject();
        jsonPut(json, "name", user.getName());
        jsonPut(json, "ip", user.getIp());
        jsonPut(json, "key", user.getKey());
        jsonPut(json, "type", "p2p-answer");
        return json;
    }

    private static void jsonPut(JSONObject json, String key, Object value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Created by swayamagrawal on 27/09/17.
     */
    public static interface DiscoveryProtocol {
        public void onPeersFound(HashMap<String,DeviceModel> devices);
        public void onDisconnect();
    }
}
