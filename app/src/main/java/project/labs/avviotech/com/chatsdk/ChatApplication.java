package project.labs.avviotech.com.chatsdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by swayamagrawal on 27/09/17.
 */
public class ChatApplication extends Application {
    public static WifiP2pManager mManager;

    public static WifiP2pManager.Channel mChannel;
    private static Context context;
    public static List<WifiP2pDevice> peers;
    public static boolean isgorupowner = false;
    public static WifiP2pDnsSdServiceInfo serviceInfo;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        peers = new ArrayList<>();
    }

    public static Context getContext()
    {
        return context;
    }



}
