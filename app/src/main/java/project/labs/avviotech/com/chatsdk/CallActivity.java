package project.labs.avviotech.com.chatsdk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;


import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import project.labs.avviotech.com.chatsdk.model.Global;
import project.labs.avviotech.com.chatsdk.model.User;
import project.labs.avviotech.com.chatsdk.net.client.Client;
import project.labs.avviotech.com.chatsdk.util.Util;


public class CallActivity extends AppCompatActivity {
    private User mAppUser;
    private ScheduledExecutorService executor;
    private Client client;
    private static final String SERVER_IP = "server ip";
    private static final String IS_SERVER = "is_server";
    private ImageButton audio,video,end;
    private final int RECORD_AUDIO = 101;
    // layouts
    private SurfaceViewRenderer remoteRenderer;
    private SurfaceViewRenderer localRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_call);


        audio = (ImageButton)findViewById(R.id.button_call_toggle_mic);
        video = (ImageButton)findViewById(R.id.button_call_switch_camera);
        end = (ImageButton)findViewById(R.id.button_call_disconnect);

        click();
        // Layout Setup
        remoteRenderer = (SurfaceViewRenderer) findViewById(R.id.remote_video_renderer);
        localRenderer = (SurfaceViewRenderer) findViewById(R.id.local_video_renderer);

        // Default Data
        String address = Util.getMacAddr();
        mAppUser = new User(address);
        executor = Executors.newSingleThreadScheduledExecutor();

        // Layout Renderer Setup
        remoteRenderer.init(Global.getInstance().getRootEglBase().getEglBaseContext(), null);
        localRenderer.init(Global.getInstance().getRootEglBase().getEglBaseContext(), null);
        localRenderer.setZOrderOnTop(true);
        updateVideoView();

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO);
        }



    }

    @Override
    protected void onResume() {
        super.onResume();
        if(client != null) client.startVideo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(client != null) client.stopVideo();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(client != null) {
            client.stopVideo();
            client.disconnect();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        client.disconnect();
        if (localRenderer != null) {
            localRenderer.release();
            localRenderer = null;
        }
        if (remoteRenderer != null) {
            remoteRenderer.release();
            remoteRenderer = null;
        }
//        stopService(new Intent(this, Server.class));
        super.onDestroy();
    }

    private void updateVideoView() {
        remoteRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        remoteRenderer.setMirror(false);

        localRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        localRenderer.setMirror(true);

        localRenderer.requestLayout();
        remoteRenderer.requestLayout();
    }

    // CallFragment.OnCallEvents interface implementation.

    public void click()
    {
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(client != null)
                    client.disconnect();

                finish();
            }
        });

        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(client != null)
                    client.onToggleMic();
            }
        });

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(client != null)
                    client.onCameraSwitch();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = getIntent();
                    String ip = intent.getStringExtra(SERVER_IP);
                    boolean isServer = intent.getBooleanExtra(IS_SERVER, false);
                    if (isServer) {
                        client = new Client(CallActivity.this, intent, "0.0.0.0", remoteRenderer, localRenderer);
                    } else {
                        client = new Client(CallActivity.this, intent, ip, remoteRenderer, localRenderer);
                    }

                    executor.schedule(new Runnable() {
                        @Override
                        public void run() {
                            client.connect();
                        }
                    }, 1, TimeUnit.SECONDS);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
