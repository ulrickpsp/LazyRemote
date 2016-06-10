package com.example.aaldridge.myclient.ServerDiscovery;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.example.aaldridge.myclient.Actions.ActionPerformerActivity;
import com.example.aaldridge.myclient.R;
import com.example.aaldridge.myclient.Connection.TCPClient;


public class LoginScreenActivity extends AppCompatActivity {

    //Global variables
    EditText editTextIPAddress, editTextPort;
    CheckBox checkbox_recoverLastSession, checkbox_tryUDPConnection;
    Button buttonConnect;
    ProgressDialog progress;
    SharedPreferences preferences;
    LoginScreenClass helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Assign java buttons to UI elements
        checkbox_recoverLastSession = (CheckBox) findViewById(R.id.cb_recoverLastSession);
        checkbox_tryUDPConnection = (CheckBox) findViewById(R.id.cb_tryUDP);
        editTextIPAddress = (EditText) findViewById(R.id.addressEditText);
        editTextPort = (EditText) findViewById(R.id.portEditText);
        buttonConnect = (Button) findViewById(R.id.connectButton);

        //Initialize progress bar
        progress = new ProgressDialog(this);
        progress.setTitle("Please Wait!!");
        progress.setMessage("...Connecting...");
        progress.setCancelable(true);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        //Initialize shared preferences (used to remember last connection parameters)
        preferences = getSharedPreferences("MisPreferencias", MODE_PRIVATE);

        //Initialize helper class
        helper = new LoginScreenClass(editTextIPAddress, editTextPort, buttonConnect, checkbox_tryUDPConnection, checkbox_recoverLastSession, progress,  preferences, this );


        //This code is executed each time the checkbox is clicked.
        checkbox_recoverLastSession.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                helper.checkboxHandler_recoverLastSession(isChecked);
            }
        });

        //This code is executed each time the checkbox is clicked.
        checkbox_tryUDPConnection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                helper.checkboxHandler_tryUDPConnection(isChecked);
            }
        });

        buttonConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (mWifi.isConnected()) {

                    progress.show();
                    //This code is executed when user clicks connect button. As it could be a long running operation, it's executed in a new thread to avoid blocking UI
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            Boolean status = helper.buttonHander_connect();
                            if(status == false){

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.dismiss();
                                        Toast.makeText(LoginScreenActivity.this, "No se puede conectar", Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                        }
                    });
                    thread.start();
                }
                else{

                    Toast.makeText(getApplicationContext(), "Wifi no estaba activo. Activando..", Toast.LENGTH_LONG).show();
                    WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    wifi.setWifiEnabled(true); // true or false to activate/deactivate wifi
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();


        progress.show();
        //Try to login to last connected IP just in case it's still open to avoid wasting time
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                String ip = preferences.getString("ip", "");
                String port = preferences.getString("port", "");
                if(!ip.equals("") || !port.equals("")) {
                    try {

                        TCPClient.ConnectionStatus TCPanswer = helper.EstablishConnection(ip, Integer.parseInt(port));
                        if (TCPanswer.status == true) {

                            //Start new activity
                            Intent i = new Intent(getApplicationContext(), ActionPerformerActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            progress.dismiss();
                        }
                    } catch (Exception ex) {
                    }
                }
            }
        });
        thread.start();

        progress.dismiss();
    }
}







