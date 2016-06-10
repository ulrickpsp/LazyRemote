package com.example.aaldridge.myclient.Connection;

import android.provider.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by aaldridge on 05/04/2016.
 */
    public class TCPClient {

        public static Socket socket;
        byte[] receive_buffer;
        InputStream inputStream;
        String dataReceived;

        private static TCPClient _instance;

        private TCPClient()
        {
        }

        public static TCPClient getInstance()
        {
            if (_instance == null)
            {
                _instance = new TCPClient();
            }
            return _instance;
        }

        public ConnectionStatus establishConnection(String addr, int port) {

            ConnectionStatus connectionStatus = new ConnectionStatus();
            connectionStatus.status = false;
            connectionStatus.information = "";

            try {
                socket = new java.net.Socket(addr, port);
                //int bytesRead;
                //String data = receiveDataFromServer();
                //if (connectionStatus.information.contains("Connected")) {
                    connectionStatus.status = true;
                    //connectionStatus.information = "OK";
                //}

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                connectionStatus.information = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                connectionStatus.information = "IOException: " + e.toString();
            }
            return connectionStatus;
        }

        public String receiveDataFromServer() {

            receive_buffer = new byte[40];
            try {
                inputStream = socket.getInputStream();
                int size= inputStream.read(receive_buffer);
                dataReceived = new String(receive_buffer, 0, size, "UTF-8");
                return dataReceived;
            } catch (Exception e) {
                Logger logger = Logger.getAnonymousLogger();
                logger.log(Level.SEVERE, "an exception was thrown", e);
                return "error";
            }
        }

        public void sendDataToServer(String data) {
            try {
                socket.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8), 0, data.getBytes(StandardCharsets.UTF_8).length);
            } catch (IOException e) {}
        }




    public void stopTCPClient(){

        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        public static class ConnectionStatus{

            public Boolean status;
            public String information;
        }


}



