package com.example.aaldridge.myclient.ServerDiscovery;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.aaldridge.myclient.Actions.ActionPerformerActivity;
import com.example.aaldridge.myclient.Connection.TCPClient;
import com.example.aaldridge.myclient.Connection.UDPclient;

/**
 * Created by aaldridge on 15/04/2016.
 */
public class LoginScreenClass extends Activity{

    EditText _ip, _port;
    Button _connect;
    CheckBox _UDP, _TCP;
    SharedPreferences _preferences;
    Context _context;
    ProgressDialog _progress;

    public LoginScreenClass(EditText ip, EditText port, Button connect, CheckBox UDP, CheckBox TCP, ProgressDialog progress, SharedPreferences preferences, Context context){

        this._ip = ip;
        this._port = port;
        this._connect = connect;
        this._UDP = UDP;
        this._TCP = TCP;
        this._preferences = preferences;
        this._context = context;
        this._progress = progress;
    }

    public void checkboxHandler_recoverLastSession(boolean isChecked){

        if (isChecked) {

            //If user checks the checkbox, get the last connection parameters from sharedPreferences and place them in the textboxes.
            _ip.setText(_preferences.getString("ip", ""));
            _port.setText(_preferences.getString("port", ""));

            //If textbox is empty after loading preferences it's because user has never used the application. Notify him using a toast and uncheck the checkbox.
            if (_ip.getText().toString().equals((""))) {
                showToast("No previous data stored", _context);
                _TCP.setChecked(false);
            }
        } else {
            //If user unchecks the checkbox leave textboxes empty
            _ip.setText("");
            _port.setText("");
        }

    }

    public void checkboxHandler_tryUDPConnection(boolean isChecked){

        //Update UI according to selected login option

        if(isChecked) {
            _port.setEnabled(false);
            _ip.setEnabled(false);
            _TCP.setEnabled(false);
        }

        else{
            _port.setEnabled(true);
            _ip.setEnabled(true);
            _TCP.setEnabled(true);
        }

    }

    public Boolean buttonHander_connect(){

        String ip = "";
        int port = 0;
        Boolean validData = false;

        //TCP connection
        if(!_UDP.isChecked()) {

            ip = _ip.getText().toString();
            try {
                port = Integer.parseInt(_port.getText().toString());
                validData = true;
            }catch (NumberFormatException ex){
                showToast("Puerto no válido", _context);
            }
        }

        //UDP Connection
        else {

            UDPclient udpClient = UDPclient.getInstance();
            byte[] lMsg = udpClient.UDPdiscoverDevice(_context);

            if(lMsg != null) {
                try {
                    String szUT8 = new String(lMsg, "UTF-8");
                    if (szUT8.contains("ServerID_")) {

                        ip = szUT8.substring(szUT8.indexOf("_") + 1, szUT8.indexOf(":"));
                        port = Integer.parseInt(szUT8.substring(szUT8.indexOf(":") + 1, szUT8.indexOf("?")));
                        validData = true;
                    }
                } catch (Exception ex) {
                }
            }
        }

        TCPClient.ConnectionStatus connectionStatus = new TCPClient.ConnectionStatus();
        connectionStatus.status =false;

        //After getting the connection parameters, connect.
        if(validData == true){

            connectionStatus = EstablishConnection(ip, port);

            //Remove progressdialog after establishConnection finishes (fine or not).
            _progress.dismiss();

            if (connectionStatus.status == true) {

                //If connection is good, save user input in shared preferences
                SharedPreferences.Editor editor = _preferences.edit();
                editor.putString("ip", ip);
                editor.putString("port", port + "");
                editor.commit();

                //Start new activity
                Intent i = new Intent(_context, ActionPerformerActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                _context.startActivity(i);
                finish();
            }
            else{

                showToast("Imposible conectar al servidor", _context);
            }
        }

        return connectionStatus.status;

    }

    public TCPClient.ConnectionStatus EstablishConnection(String ip, int port) {

        TCPClient my_socket = TCPClient.getInstance();
        TCPClient.ConnectionStatus connectionStatus = my_socket.establishConnection(ip, port);

        return connectionStatus;
    }




    //This method is used to show 'centered-screen' Toasts from anywhere in the activity
    public static void showToast(final String text, final Context context) {
        ((Activity)context).runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }
}
