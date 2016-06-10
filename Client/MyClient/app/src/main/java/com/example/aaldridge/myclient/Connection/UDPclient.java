package com.example.aaldridge.myclient.Connection;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Created by aaldridge on 12/04/2016.
 */
public class UDPclient {

    private static UDPclient _instance;

    private UDPclient()
    {
    }

    public static UDPclient getInstance()
    {
        if (_instance == null)
        {
            _instance = new UDPclient();
        }
        return _instance;
    }

    public String intToIp(int i) {

        return ( i & 0xFF)  + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ((i >> 24 ) & 0xFF );
    }

    public byte[] UDPdiscoverDevice(Context mContext){

        InetAddress address = getBroadcastAddress(mContext);
        DatagramSocket ds = null;
        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();

        try {
            ds = new DatagramSocket();
            int ip=dhcp.ipAddress;
            String myip = intToIp(ip);
            String send = "LazyRemote_" + myip;
            DatagramPacket dp = new DatagramPacket(send.getBytes(), send.length(), address, 56874);
            ds.setBroadcast(true);
            ds.send(dp);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] lMsg = new byte[30];
        DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
        try {
            ds = new DatagramSocket(56874);
            ds.setSoTimeout(5000);
            ds.receive(dp);
        } catch (SocketTimeoutException  e) {
            lMsg = null;
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lMsg;
    }

    InetAddress getBroadcastAddress(Context mContext) {

        InetAddress address;
        Enumeration<NetworkInterface> en = null;
        try {
            en = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (en.hasMoreElements()) {
            NetworkInterface ni = en.nextElement();

            List<InterfaceAddress> list = ni.getInterfaceAddresses();
            Iterator<InterfaceAddress> it = list.iterator();

            while (it.hasNext()) {
                InterfaceAddress ia = it.next();
                address = ia.getBroadcast();
                if (address != null) {
                    return address;
                }
                System.out.println(" Broadcast = " + ia.getBroadcast());
            }
        }
        return null;
    }
}
