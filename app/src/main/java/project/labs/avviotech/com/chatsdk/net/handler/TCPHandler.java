package project.labs.avviotech.com.chatsdk.net.handler;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;

import project.labs.avviotech.com.chatsdk.nearby.NearByUtil;
import project.labs.avviotech.com.chatsdk.net.socket.TCPClientSocket;
import project.labs.avviotech.com.chatsdk.net.socket.TCPServerSocket;
import project.labs.avviotech.com.chatsdk.util.Util;


/**
 * Created by jinhy on 2016-12-04.
 */

public class TCPHandler extends ConnectionHandler {
    private static final int DEFAULT_PORT = 8888;

    public TCPHandler(HandleConnection handleConnection, HandleProtocol handleProtocol) {
        super(handleConnection, handleProtocol);
    }

    @Override
    protected void connect() {
        /*String endpoint = connectionParameter.getRoomId();

        Matcher matcher = Util.IP_PATTERN.matcher(endpoint);
        if (!matcher.matches()) {
            return;
        }

        String ip = matcher.group(1);
        String portStr = matcher.group(matcher.groupCount());
        int port;

        if (portStr != null) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                return;
            }
        } else {
            port = DEFAULT_PORT;
        }

        InetAddress address;
        try {
            address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            onError("Invalid IP address.");
            return;
        }

//        if(socket != null) {
//            socket.disconnect();
//        }

        if (address.isAnyLocalAddress()) {
            socket = new TCPServerSocket(address, port, this);
        } else {
            socket = new TCPClientSocket(address, port, this);
        }*/
        Log.i("TCPHandler","Connect");
        super.connect();
    }
}
