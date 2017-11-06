package project.labs.avviotech.com.chatsdk.nearby;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.Connections;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import project.labs.avviotech.com.chatsdk.CallActivity;
import project.labs.avviotech.com.chatsdk.R;
import project.labs.avviotech.com.chatsdk.net.model.DeviceModel;
import project.labs.avviotech.com.chatsdk.net.protocol.NearByProtocol;
import project.labs.avviotech.com.chatsdk.util.Util;

/**
 * Created by swayamagrawal on 04/10/17.
 */
public class NearByUtilCopy implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
    {
    private static MediaPlayer mediaPlayer;
    private static NearByUtilCopy instance;
    private static GoogleApiClient mGoogleApiClient;
    private static String SERVICE_ID = "project.labs.avviotech.com.chatsdk.new";
    private static String CLIENT_SERVICE_ID = "project.labs.avviotech.com.chatsdk.client";
    private static String CLERK_SERVICE_ID = "project.labs.avviotech.com.chatsdk.clerk";
    private static String name="";
    private static final String SERVER_IP = "server ip";
    private static final String IS_SERVER = "is_server";
    private static String serverIp = "";

    private HashMap<String,DeviceModel> clerkList;
    private HashMap<String,DeviceModel> clientList;
    private HashMap<String,DeviceModel> peerList;
    private static boolean isGroupOwner = false;
    public  static NearByProtocol.DiscoveryProtocol delegate;
    public HashMap<String,DeviceModel> getClientList()
    {
        return clientList;
    }
    public HashMap<String,DeviceModel> getClerkList()
    {
        return clerkList;
    }
    public HashMap<String,DeviceModel> getPeerList()
    {
        return peerList;
    }
    private static String type;
    private static Activity activity;
    private static Message mActiveMessage;

    public static NearByUtilCopy getInstance()
    {
        if(instance == null)
            instance = new NearByUtilCopy();

        return  instance;
    }

    private static String TAG = "NearBy";
    public static void setActivity(Activity a)
    {
        activity = a;
    }

    public void init(AppCompatActivity activity, String n, String t)
    {
        name = n;
        type = t;
        name = type + "-" + name;
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(activity,this)
                .build();

        clerkList = new HashMap<>();
        clientList = new HashMap<>();
        peerList = new HashMap<>();



    }


    public static void start()
    {
        mGoogleApiClient.connect();
    }

    public static void stop()
    {
        Log.i("ChatSDK","Google client Stopped");
        unpublish();
        unsubscribe();
        mGoogleApiClient.disconnect();

    }

    public static void startAdvertising()
    {
        if(mGoogleApiClient.isConnected())
        {
            Nearby.Connections.startAdvertising(
                    mGoogleApiClient,
                    name,
                    SERVICE_ID,
                    mConnectionLifecycleCallback,
                    new AdvertisingOptions(Strategy.P2P_STAR))
                    .setResultCallback(
                            new ResultCallback<Connections.StartAdvertisingResult>() {
                                @Override
                                public void onResult(@NonNull Connections.StartAdvertisingResult result) {
                                    if (result.getStatus().isSuccess()) {
                                        Log.i("NearBy", "Device Advertising Successful  - " + result.getLocalEndpointName());
                                    } else {
                                        Log.i("NearBy", "Device Advertising failed ");
                                    }
                                }
                            });
        }

    }

    public static void stopAdvertising()
    {
        if(mGoogleApiClient.isConnected()) {
            Log.i("ChatSDK","Advertising Stopped");
            Nearby.Connections.stopAdvertising(
                    mGoogleApiClient);
        }
    }
    public static void stopDiscovery()
    {
        if(mGoogleApiClient.isConnected()) {
            Log.i("ChatSDK","Discovery Stopped");
            Nearby.Connections.stopDiscovery(
                    mGoogleApiClient);
        }
    }

    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(
                        String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
                    Log.i("NearBy", "Device Found - " + endpointId + " - " + discoveredEndpointInfo.getEndpointName());

                    DeviceModel model = new DeviceModel();
                    model.setName(getFilterName(discoveredEndpointInfo.getEndpointName()));
                    model.setAddress(endpointId);
                    peerList.put(endpointId, model);

                    if(!isClient(discoveredEndpointInfo.getEndpointName()))
                        clerkList.put(endpointId, model);
                    else
                        clientList.put(endpointId, model);

                    delegate.onPeersFound(peerList);

                }

                @Override
                public void onEndpointLost(String endpointId) {
                    Log.i("NearBy", "onEndpointLost- " + endpointId);
                    if(peerList.get(endpointId) != null)
                    {
                        DeviceModel m = peerList.get(endpointId);
                        if(m.getType().equalsIgnoreCase("clerk"))
                            clerkList.remove(endpointId);
                        if(m.getType().equalsIgnoreCase("client"))
                            clientList.remove(endpointId);

                        peerList.remove(endpointId);

                        delegate.onPeersFound(peerList);
                    }


                }
            };

    public void startDiscovery() {
        if(mGoogleApiClient.isConnected())
        {
            Nearby.Connections.startDiscovery(
                    mGoogleApiClient,
                    SERVICE_ID,
                    mEndpointDiscoveryCallback,
                    new DiscoveryOptions(Strategy.P2P_STAR))
                    .setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(@NonNull Status status) {
                                    if (status.isSuccess()) {
                                        Log.i("NearBy","Device Discovery Successful  - " + status.getStatusMessage());
                                    } else {
                                        // We were unable to start discovering.
                                        Log.i("NearBy","Device Discovery Failed  - " + status.getStatusMessage());
                                    }
                                }
                            });
        }

    }

    public void connect(
            final String endpointId) {
        clientList.remove(endpointId);
        delegate.onPeersFound(peerList);
        Nearby.Connections.requestConnection(
                mGoogleApiClient,
                name,
                endpointId,
                mConnectionLifecycleCallback)
                .setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if (status.isSuccess()) {
                                    Log.i("NearBy","requestConnection");
                                } else {
                                    Log.i("NearBy","requestConnection failed");
                                }
                            }
                        });
    }

    public void call(
            final String endpointId) {
        isGroupOwner = true;

        connect(endpointId);


    }

    private static PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String s, Payload payload) {
            Log.i("ChatSDK", "onPayloadReceived" + s + " - " + new String(payload.asBytes()));
            if(!isGroupOwner)
                serverIp = new String(payload.asBytes());

            startCall();
        }

        @Override
        public void onPayloadTransferUpdate(String s, PayloadTransferUpdate payloadTransferUpdate) {

        }
    };
    private static final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {

                @Override
                public void onConnectionInitiated(
                        final String endpointId, ConnectionInfo connectionInfo) {
                    // Automatically accept the connection on both sides.

                    if(true)
                    {
                        //try
                        //{Thread.sleep(200);}catch (Exception e){e.printStackTrace();}

                        new CountDownTimer(100, 1000) { //40000 milli seconds is total time, 1000 milli seconds is time interval

                            public void onTick(long millisUntilFinished) {
                            }
                            public void onFinish() {
                                Nearby.Connections.acceptConnection(
                                        mGoogleApiClient, endpointId, mPayloadCallback);
                            }
                        }.start();


                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("ChatSDK","Accept Condition");

                            }
                        },80);

                    }
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            // We're connected! Can now start sending and receiving data.
                            Log.i("NearBy", "Connected");
                            if(isGroupOwner)
                            {
                                Payload p = Payload.fromBytes(Util.getIPAddress(true).getBytes());
                                Nearby.Connections.sendPayload(mGoogleApiClient, endpointId, p);
                                Log.i("ChatSDK","Sending Payload with ip address to client from " + type);
                                if(isGroupOwner)
                                    startCall();
                            }

                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // The connection was rejected by one or both sides.
                            Log.i("NearBy","Rejected");
                            break;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                    Log.i("NearBy","DisConnected");
                    delegate.onDisconnect();

                }
            };


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
            stopSound();
            Intent launchServer = new Intent(activity, CallActivity.class);
            launchServer.putExtra(SERVER_IP, ip);
            launchServer.putExtra(IS_SERVER, true);
            launchServer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(launchServer);
        }else
        {
            stopSound();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent launchClient = new Intent(activity, CallActivity.class);
                    launchClient.putExtra(SERVER_IP, serverIp);
                    launchClient.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(launchClient);
                }
            }, 3000);


        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("ChatSDK","google app client started");
        //Nearby.Connections.stopAllEndpoints(mGoogleApiClient);
        //startDiscovery();
        //if("clerk".equalsIgnoreCase(type))
        //{
          //  startAdvertising();
        //}

        publish(name);
        subscribe();


    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("ChatSDK","Google Client onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("ChatSDK","Google Client onConnectionFailed");
    }

    public boolean isClient(String name)
    {
        if(name.indexOf("client") != -1)
            return true;

        return false;
    }

    public static void disconnect()
    {
        if("client".equalsIgnoreCase(type))
            stopAdvertising();

        delegate.onDisconnect();
    }

    public String getFilterName(String n)
    {
        if(n.indexOf("-") != -1)
            return n.substring(n.indexOf("-") + 1);

        return "";
    }

    public static void playSound()
    {
        if(mediaPlayer == null)
        {
            mediaPlayer = MediaPlayer.create(activity, R.raw.ringtone);
            mediaPlayer.setLooping(true);
        }

        mediaPlayer.start();
    }
    public static void stopSound()
    {
        if(mediaPlayer != null && mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
        }


    }



    private static void publish(String message) {
        Log.i(TAG, "Publishing message: " + message);
        mActiveMessage = new Message(message.getBytes());
        Nearby.Messages.publish(mGoogleApiClient, mActiveMessage);
    }

    private static void unpublish() {
        Log.i(TAG, "Unpublishing.");
        if (mActiveMessage != null) {
            Nearby.Messages.unpublish(mGoogleApiClient, mActiveMessage);
            mActiveMessage = null;
        }
    }

    private static void subscribe() {
        Log.i(TAG, "Subscribing.");
        SubscribeOptions options = new SubscribeOptions.Builder()
               .build();

        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, options);
    }

    private static void unsubscribe() {
        Log.i(TAG, "Unsubscribing.");
        Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
    }

    private static MessageListener mMessageListener = new MessageListener() {
        @Override
        public void onFound(Message message) {
            String messageAsString = new String(message.getContent());
            Log.d(TAG, "Found message: " + messageAsString);
        }

        @Override
        public void onLost(Message message) {
            String messageAsString = new String(message.getContent());
            Log.d(TAG, "Lost sight of message: " + messageAsString);
        }
    };


}
