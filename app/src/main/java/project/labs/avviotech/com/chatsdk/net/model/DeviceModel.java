package project.labs.avviotech.com.chatsdk.net.model;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by swayamagrawal on 02/10/17.
 */
public class DeviceModel {

    private String address;
    private String name;
    private String type;
    private WifiP2pDevice device;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public WifiP2pDevice getDevice() {
        return device;
    }

    public void setDevice(WifiP2pDevice device) {
        this.device = device;
    }
}

