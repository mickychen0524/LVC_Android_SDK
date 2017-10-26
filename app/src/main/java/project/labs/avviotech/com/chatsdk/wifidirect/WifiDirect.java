package project.labs.avviotech.com.chatsdk.wifidirect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.labs.avviotech.com.chatsdk.CallActivity;
import project.labs.avviotech.com.chatsdk.ChatApplication;
import project.labs.avviotech.com.chatsdk.net.model.DeviceModel;
import project.labs.avviotech.com.chatsdk.net.protocol.WiFiP2PProtocol;
import project.labs.avviotech.com.chatsdk.util.Util;

public class WifiDirect {

    private static WifiP2pManager mManager;
    private static WifiP2pManager.Channel mChannel;
    private static HashMap<String,DeviceModel> peers;
    private static HashMap<String,DeviceModel> clerkList;
    private static HashMap<String,DeviceModel> clientList;

    private static BroadcastReceiver mReceiver;
    public static IntentFilter mIntentFilter;;
    public static WiFiP2PProtocol.DiscoveryProtocol delegate;
    private static WifiDirect service;
    private static Activity activity;
    private static final String SERVER_IP = "server ip";
    private static final String IS_SERVER = "is_server";
    private static final String BITRATE = "bitrate";
    private static final String RESOLUTION = "resolution";
    private static WifiP2pDnsSdServiceRequest mWifiP2pServiceRequest;
    private static Handler mServiceBroadcastingHandler;
    private static Handler mServiceDiscoveringHandler;
    private static WifiP2pDnsSdServiceInfo mWifiP2pServiceInfo;

    public static boolean discover = false;

    private static boolean isGroupOwner = false;
    private static String type;

    public static IntentFilter getIntentFilter(){
        return mIntentFilter;
    }

    public static BroadcastReceiver getmReceiver()
    {
        return mReceiver;
    }
    public static WifiDirect getInstance()
    {

        if(mServiceDiscoveringHandler == null)
            mServiceDiscoveringHandler = new Handler(Looper.myLooper());

        if(mServiceBroadcastingHandler == null)
            mServiceBroadcastingHandler = new Handler(Looper.myLooper());

        if(peers == null)
            peers = new HashMap<>();

        if(clerkList == null)
            clerkList = new HashMap<>();

        if(clientList == null)
            clientList = new HashMap<>();


        if(service == null)
            service = new WifiDirect();

        return service;
    }

    public static void init(Activity activity,String t)
    {


        type = t;

        Map record = new HashMap<>();
        record.put("type", type);
        mWifiP2pServiceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("_wifidemotest", "_presence._tcp", record);


        mManager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(activity,activity.getMainLooper(), null);

        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel,peerListListener,connectionInfoListenerListener);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);



    }

    private static WifiP2pManager.ConnectionInfoListener connectionInfoListenerListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            Log.i("Chat SDK", "onConnectionInfoAvailable " + wifiP2pInfo.isGroupOwner);
            if(wifiP2pInfo.isGroupOwner)
                isGroupOwner = true;
            else
                isGroupOwner = false;


        }
    };


    private static WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {


        }
    };

    public static HashMap<String,DeviceModel> getPeersList()
    {
        return peers;
    }

    public static HashMap<String,DeviceModel> getClientList()
    {
        return clientList;
    }

    public static HashMap<String,DeviceModel> getClerkList()
    {
        return clerkList;
    }


    public static void connect(final WifiP2pDevice device) {

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        discover = false;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.i("Chat SDK", "Connect success");
                isGroupOwner = device.isGroupOwner();
                peers.remove(device.deviceAddress);
                clientList.remove(device.deviceAddress);
                delegate.onPeersFound(peers);
            }

            @Override
            public void onFailure(int reason) {
                Log.i("Chat SDK", "Connect Failed");
            }
        });
    }

    public static void startCall()
    {
        boolean isserer = false;
        String ip = Util.getIPAddress(true);
        InetAddress address;
        try {
            address = InetAddress.getByName(ip);
            if(address.isAnyLocalAddress())
                isserer = true;
        } catch (UnknownHostException e) {
        }


        Log.i("ChatSDK", "IP Address - " + ip);
        Log.i("ChatSDK", "is owner - " + isGroupOwner);
        if(isGroupOwner)
        {
            Intent launchServer = new Intent(ChatApplication.getContext(), CallActivity.class);
            launchServer.putExtra(SERVER_IP, ip);
            launchServer.putExtra(IS_SERVER, true);
            launchServer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ChatApplication.getContext().startActivity(launchServer);
        }else
        {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent launchClient = new Intent(ChatApplication.getContext(), CallActivity.class);
                    launchClient.putExtra(SERVER_IP, "192.168.49.1");
                    launchClient.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ChatApplication.getContext().startActivity(launchClient);
                }
            }, 3000);


        }
    }

    public static void updateConnectionStatus(final WifiP2pDevice device)
    {
        if(device != null)
        {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String status = getDeviceStatus(device.status);
                    Log.i("Chat SDK", "Status of device - " + device.deviceName + " - " + status);
                    if (status == "Unavailable") {
                        peers.remove(device.deviceAddress);
                        clientList.remove(device.deviceAddress);
                        delegate.onPeersFound(peers);
                        isGroupOwner = false;
                    } else if (status == "Connected") {
                        peers.remove(device.deviceAddress);
                        clientList.remove(device.deviceAddress);
                        delegate.onPeersFound(peers);
                        clearLocalServices();
                        startCall();
                    }
                }
            }, 3000);
        }
    }

    private static String getDeviceStatus(int deviceStatus) {
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

    private static Runnable mServiceBroadcastingRunnable = new Runnable() {
        @Override
        public void run() {
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                    mManager.requestPeers(mChannel, peerListListener);


                }

                @Override
                public void onFailure(int error) {
                }
            });

        }
    };

    public static void clearLocalServices()
    {
        mManager.addLocalService(mChannel, mWifiP2pServiceInfo,
                new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        // service broadcasting started
                    }

                    @Override
                    public void onFailure(int error) {
                        // react to failure of adding the local service
                    }
                });
    }

    public static void startBroadcastingService(){
        if(discover)
        {
            mManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    mManager.addLocalService(mChannel, mWifiP2pServiceInfo,
                            new WifiP2pManager.ActionListener() {

                                @Override
                                public void onSuccess() {
                                    // service broadcasting started
                                    mServiceBroadcastingHandler
                                            .postDelayed(mServiceBroadcastingRunnable,
                                                    5000);
                                }

                                @Override
                                public void onFailure(int error) {
                                    // react to failure of adding the local service
                                }
                            });
                }

                @Override
                public void onFailure(int error) {
                    // react to failure of clearing the local services
                }
            });
        }

    }

    public static void prepareServiceDiscovery() {
        discover = true;
        mManager.setDnsSdResponseListeners(mChannel,
                new WifiP2pManager.DnsSdServiceResponseListener() {

                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                                                        String registrationType, WifiP2pDevice srcDevice) {
                        Log.i("ChatSDK",srcDevice.deviceName);
                    }
                }, new WifiP2pManager.DnsSdTxtRecordListener() {

                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomainName, Map<String, String> record,
                            WifiP2pDevice device) {
                        Log.i("ChatSDK", record.get("type"));
                        DeviceModel model = new DeviceModel();
                        model.setAddress(device.deviceAddress);
                        model.setName(device.deviceName);
                        model.setType(record.get("type"));
                        model.setDevice(device);

                        peers.put(device.deviceAddress, model);

                        if("clerk".equalsIgnoreCase(record.get("type")))
                            clerkList.put(device.deviceAddress,model);
                        else if("client".equalsIgnoreCase(record.get("type")))
                            clientList.put(device.deviceAddress,model);

                        delegate.onPeersFound(peers);
                    }
                });

        mWifiP2pServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();
    }

    public static void startServiceDiscovery() {
        if(discover)
        {
            mManager.removeServiceRequest(mChannel, mWifiP2pServiceRequest,
                    new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            mManager.addServiceRequest(mChannel, mWifiP2pServiceRequest,
                                    new WifiP2pManager.ActionListener() {

                                        @Override
                                        public void onSuccess() {
                                            mManager.discoverServices(mChannel,
                                                    new WifiP2pManager.ActionListener() {

                                                        @Override
                                                        public void onSuccess() {
                                                            //service discovery started

                                                            mServiceDiscoveringHandler.postDelayed(
                                                                    mServiceDiscoveringRunnable,
                                                                    5000);
                                                        }

                                                        @Override
                                                        public void onFailure(int error) {
                                                            // react to failure of starting service discovery
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onFailure(int error) {
                                            // react to failure of adding service request
                                        }
                                    });
                        }

                        @Override
                        public void onFailure(int reason) {
                            // react to failure of removing service request
                        }
                    });
        }

    }

    public static Runnable mServiceDiscoveringRunnable = new Runnable() {
        @Override
        public void run() {
            startServiceDiscovery();
        }
    };

    public static void disconnect()
    {
        Log.i("ChatSDK","disconnect called");
        delegate.onDisconnect();
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                discover = true;
                if(type.equalsIgnoreCase("clerk"))
                {

                    startBroadcastingService();
                    startServiceDiscovery();
                }
                else
                {
                    startServiceDiscovery();
                }
            }

            @Override
            public void onFailure(int i) {

            }
        });
    }

    public static void stopDiscovering()
    {
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i) {

            }
        });
    }

}
