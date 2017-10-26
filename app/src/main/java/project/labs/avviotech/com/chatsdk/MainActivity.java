package project.labs.avviotech.com.chatsdk;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.nearby.Nearby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import project.labs.avviotech.com.chatsdk.nearby.NearByUtil;
import project.labs.avviotech.com.chatsdk.net.model.DeviceModel;
import project.labs.avviotech.com.chatsdk.net.protocol.NearByProtocol;
import project.labs.avviotech.com.chatsdk.wifidirect.WifiDirect;


public class MainActivity extends ActionBarActivity implements NearByProtocol.DiscoveryProtocol{

    private NearByUtil nearby;
    private ListView listView;
    private ArrayList<HashMap<String,String>> arrayList;
    private SimpleAdapter simpleAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);

        nearby = NearByUtil.getInstance();
        nearby.init(this, Build.MANUFACTURER,"client");
        nearby.delegate = this;
        String[] from={"name"};//string array
        int[] to={project.labs.avviotech.com.chatsdk.R.id.textView};//int array of views id's

        final WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        listView = (ListView)findViewById(R.id.simpleListView);
        arrayList=new ArrayList<>();
        simpleAdapter = new SimpleAdapter(this,arrayList, project.labs.avviotech.com.chatsdk.R.layout.list_view_items,from,to);
        listView.setAdapter(simpleAdapter);
        simpleAdapter.notifyDataSetChanged();

        populateData();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Object[] keys = nearby.getPeerList().keySet().toArray();
                        String d = nearby.getPeerList().get(keys[i]).getAddress();
                        nearby.call(d);
                    }
                });

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        nearby.start();


    }

    @Override
    protected void onStop() {
        super.onStop();
        nearby.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void populateData()
    {

        HashMap<String,DeviceModel> clientList = nearby.getClientList();
        Log.i("Clerk", "populateData" + " - " + clientList.size());
        arrayList.clear();
        Iterator it = clientList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            DeviceModel value = (DeviceModel)pair.getValue();
            HashMap<String,String> map = new HashMap<>();
            map.put("name", value.getName());
            arrayList.add(map);
        }
        simpleAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPeersFound(HashMap<String, DeviceModel> devices) {
        populateData();
    }

    @Override
    public void onDisconnect() {

    }
}
