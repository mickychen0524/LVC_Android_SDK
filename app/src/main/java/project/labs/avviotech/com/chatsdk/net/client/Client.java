package project.labs.avviotech.com.chatsdk.net.client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import org.webrtc.Camera2Enumerator;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import project.labs.avviotech.com.chatsdk.ChatApplication;
import project.labs.avviotech.com.chatsdk.model.Global;
import project.labs.avviotech.com.chatsdk.model.User;
import project.labs.avviotech.com.chatsdk.nearby.NearByUtil;
import project.labs.avviotech.com.chatsdk.net.ConnectionParameter;
import project.labs.avviotech.com.chatsdk.net.Params;
import project.labs.avviotech.com.chatsdk.net.PeerConnectionClient;
import project.labs.avviotech.com.chatsdk.net.handler.ConnectionHandler;
import project.labs.avviotech.com.chatsdk.net.handler.PeerHandler;
import project.labs.avviotech.com.chatsdk.net.handler.TCPHandler;
import project.labs.avviotech.com.chatsdk.util.Util;



/**
 * Created by jinhy on 2016-11-22.
 */

public class Client implements Params,
        ConnectionHandler.HandleConnection, PeerHandler.HandlePeerConnection {
    private ScheduledExecutorService executor;
    private boolean micEnabled = true;
    Context context;
    PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
    private PeerConnectionClient peerConnectionClient = null;
    ConnectionParameter connectionParameter;
    TCPHandler tcpHandler;
    PeerHandler peerHandler;

    private boolean iceConnected;

    private SurfaceViewRenderer remoteRenderer;
    private SurfaceViewRenderer localRenderer;

    public Client(Context context, Intent intent, String ip,
                  SurfaceViewRenderer remoteRenderer, SurfaceViewRenderer localRenderer) {
        this.context = context;
        this.remoteRenderer = remoteRenderer;
        this.localRenderer = localRenderer;
        executor = Executors.newSingleThreadScheduledExecutor();

        boolean loopback = intent.getBooleanExtra(EXTRA_LOOPBACK, false);
        boolean tracing = intent.getBooleanExtra(EXTRA_TRACING, false);

        boolean useCamera2 = Camera2Enumerator.isSupported()
                && intent.getBooleanExtra(EXTRA_CAMERA2, false);
        peerConnectionParameters = new PeerConnectionClient.PeerConnectionParameters(
                        intent.getBooleanExtra(EXTRA_VIDEO_CALL, true),
                        loopback,
                        tracing,
                        useCamera2,
                        intent.getIntExtra(EXTRA_VIDEO_WIDTH, 0),
                        intent.getIntExtra(EXTRA_VIDEO_HEIGHT, 0),
                        intent.getIntExtra(EXTRA_VIDEO_FPS, 0),
                        intent.getIntExtra(EXTRA_VIDEO_BITRATE, 0),
                        intent.getStringExtra(EXTRA_VIDEOCODEC),
                        intent.getBooleanExtra(EXTRA_HWCODEC_ENABLED, true),
                        intent.getBooleanExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, useCamera2),
                        intent.getIntExtra(EXTRA_AUDIO_BITRATE, 0),
                        intent.getStringExtra(EXTRA_AUDIOCODEC),
                        intent.getBooleanExtra(EXTRA_NOAUDIOPROCESSING_ENABLED, true),
                        intent.getBooleanExtra(EXTRA_AECDUMP_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_OPENSLES_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AEC, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AGC, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_NS, false),
                        intent.getBooleanExtra(EXTRA_ENABLE_LEVEL_CONTROL, true));

        peerConnectionClient = PeerConnectionClient.getInstance(false);
        peerHandler = new PeerHandler(peerConnectionClient, this);
        tcpHandler = new TCPHandler(this, peerHandler);


        setConnectionParameter("", ip + ":8888", loopback);

        if (loopback) {
            PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
            options.networkIgnoreMask = 0;
            peerConnectionClient.setPeerConnectionFactoryOptions(options);
        }
        peerConnectionClient.createPeerConnectionFactory(context, localRenderer, remoteRenderer,
                peerConnectionParameters, tcpHandler);

    }

    public void setConnectionParameter(String roomUrl, String roomId, boolean loopback) {
        connectionParameter = new ConnectionParameter(roomUrl, roomId, loopback);
    }

    public void startVideo() {
        if (peerConnectionClient != null) {
            peerConnectionClient.startVideoSource();
        }
    }

    public void stopVideo() {
        if (peerConnectionClient != null) {
            peerConnectionClient.stopVideoSource();
        }
    }

    public void disconnect() {
        if (tcpHandler != null) {
            tcpHandler.disconnectFromRoom();
//            tcpHandler = null;
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
//            peerConnectionClient = null;
        }

        try
        {
            ((Activity)context).finish();
            NearByUtil.getInstance().disconnect();

        }catch(Exception e)
        {
            e.printStackTrace();
        }



    }

    public void connect() {
        if (tcpHandler == null) {
            return;
        }
        // Start room connection.

//        if(peerConnectionClient != null) {
//            peerConnectionClient.close();
//        }
        tcpHandler.connectToRoom(connectionParameter);
    }

    private void setSignalingParameters(ConnectionHandler.SignalingParameters params) {
        tcpHandler.setSignalingParameters(params);
        peerHandler.setSignalingParameters(params);
    }

    @Override
    public void onConnectTCP() {
        Log.i("ChatSDK","TCP Connected");
        Global.getInstance().getUser().setIp(Util.getIPAddress(true));
        tcpHandler.requestUserInfo();
    }

    public void onMessageReceived(String message)
    {
        tcpHandler.onMessage(message);
    }

    @Override
    public void onConnectPeer() {
        Log.i("ChatSDK", "Peer Connected");
    }

    @Override
    public void onRequestUserInfo(User user) {
        if(!NearByUtil.getInstance().isGroupOwner())
            Global.getInstance().getUser().setIp(NearByUtil.getInstance().serverIp);
        else
            Global.getInstance().getUser().setIp(Util.getIPAddress(true));
        tcpHandler.answerUserInfo();
    }

    @Override
    public void onConnectedToRoom(ConnectionHandler.SignalingParameters params) {
        setSignalingParameters(params);
        peerConnectionClient.createPeerConnection(Global.getInstance().getRootEglBase().getEglBaseContext(),
                localRenderer, remoteRenderer, params);

        if (params.initiator) {
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient.createOffer();
        } else {
            if (params.offerSdp != null) {
                peerConnectionClient.setRemoteDescription(params.offerSdp);
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
            if (params.iceCandidates != null) {
                // Add remote ICE candidates from room.
                for (IceCandidate iceCandidate : params.iceCandidates) {
                    peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                }
            }
        }
    }

    @Override
    public void onChannelClose() {
        Log.i("ChatSDK","onChannelClose");
        disconnect();
        //WifiDirect.getInstance().disconnect();
    }

    @Override
    public void onChannelError(String description) {
        Log.i("ChatSDK","onChannelError - " + description);
        disconnect();
    }

    @Override
    public void onIceConnected() {
        iceConnected = true;
        if (peerConnectionClient == null) {
            return;
        }
        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
    }

    @Override
    public void onIceDisconnected() {
        iceConnected = false;
        Log.i("ChatSDK","onIceDisconnected");
        disconnect();
    }

    public void onCameraSwitch() {
        if (peerConnectionClient != null) {
            peerConnectionClient.switchCamera();
        }
    }

    public boolean onToggleMic() {
        if (peerConnectionClient != null) {
            micEnabled = !micEnabled;
            peerConnectionClient.setAudioEnabled(micEnabled);
        }
        return micEnabled;
    }


}
