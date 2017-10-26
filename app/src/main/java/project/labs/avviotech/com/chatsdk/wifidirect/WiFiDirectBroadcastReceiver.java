/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package project.labs.avviotech.com.chatsdk.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

import java.util.HashMap;

import project.labs.avviotech.com.chatsdk.ChatApplication;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private WifiP2pManager.PeerListListener peerListListener;
    private WifiDirect wifiDirect;
    private HashMap<String,String> statusMap;
    private WifiP2pManager.ConnectionInfoListener connectionInfoListenerListener;
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,WifiP2pManager.PeerListListener peerListListener,WifiP2pManager.ConnectionInfoListener connectionInfoListenerListener) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.peerListListener = peerListListener;
        statusMap = new HashMap<>();
        this.connectionInfoListenerListener = connectionInfoListenerListener;

    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (manager != null) {
                manager.requestPeers(channel, peerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            manager.requestConnectionInfo(channel, connectionInfoListenerListener);
            Log.i("ChatSDK", "WIFI_P2P_CONNECTION_CHANGED_ACTION Status - ");
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            WifiP2pDevice device = (WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

            Log.i("ChatSDK","Device Status - "+ device.deviceName + " - " + getDeviceStatus(device.status));
            manager.requestConnectionInfo(channel, connectionInfoListenerListener);

            if(statusMap.get(device.deviceAddress) == null || getDeviceStatus(device.status) == "Available")
                statusMap.put(device.deviceAddress,getDeviceStatus(device.status));

            if(getDeviceStatus(device.status) == "Connected" && statusMap.get(device.deviceAddress).equalsIgnoreCase("Available"))
                wifiDirect.getInstance().updateConnectionStatus((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }
    private String getDeviceStatus(int deviceStatus) {
        Log.d("WiFiDirectActivity.TAG", "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }
}
