package project.labs.avviotech.com.chatsdk.net.protocol;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import project.labs.avviotech.com.chatsdk.model.User;
import project.labs.avviotech.com.chatsdk.net.model.DeviceModel;

/**
 * Created by jinhy on 2016-12-05.
 */

public class NearByProtocol {
    public static interface DiscoveryProtocol {
        public void onPeersFound(HashMap<String, DeviceModel> devices);
        public void onDisconnect();
    }
}
