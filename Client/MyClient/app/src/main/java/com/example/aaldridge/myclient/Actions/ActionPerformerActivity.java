package com.example.aaldridge.myclient.Actions;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;

import com.example.aaldridge.myclient.Actions.Soccer.SoccerActivity;
import com.example.aaldridge.myclient.Actions.Soccer.SoccerClass;
import com.example.aaldridge.myclient.Connection.TCPClient;
import com.example.aaldridge.myclient.R;

import java.util.List;

public class ActionPerformerActivity extends AppCompatActivity implements View.OnClickListener {

    Button bVolume, bOpenBrowser;
    Button bShutdown, bCancelShutdown, bShutdown_0s, bShutdown_3600s, bShutdown_5400s, bShutdown_7200s, bShutDown_sendOrder;
    Button bSoccer, bMitele, bYoutube;
    ImageView mouseSpace;
    EditText etShutDown;
    TCPClient tcpClient;
    Context context;
    ClipboardManager clipboard;
    String last_uri = "";
    Dialog DialogVolume, DialogShutdown;
    SeekBar sbVolume;
    int currentX, currentY, lastX, lastY;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actions);

        //Assign java buttons to UI elements
        bVolume = (Button) findViewById(R.id.bVolume);
        bShutdown = (Button) findViewById(R.id.bShutDown);
        bOpenBrowser = (Button) findViewById(R.id.b_openBrowser);
        bMitele = (Button) findViewById(R.id.bMitele);
        bYoutube = (Button) findViewById(R.id.bYoutube);
        mouseSpace = (ImageView) findViewById(R.id.mouseSpace);


        //Register buttons to clickListener
        bVolume.setOnClickListener(this);
        bShutdown.setOnClickListener(this);
        bOpenBrowser.setOnClickListener(this);
        bMitele.setOnClickListener(this);
        bYoutube.setOnClickListener(this);
        gestureDetector = new GestureDetector(this, new SingleTapConfirm());

        //Initialize some values
        tcpClient = TCPClient.getInstance();
        context = this;
        clipboard = null;

        //Volume layout dialog
        DialogVolume = new Dialog(this);
        LayoutInflater inflaterVolume = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutVolume = inflaterVolume.inflate(R.layout.dialog_volume, (ViewGroup) findViewById(R.id.myVolumeDialog));
        AlertDialog.Builder builderVolume = new AlertDialog.Builder(this).setView(layoutVolume);
        DialogVolume = builderVolume.create();
        sbVolume = (SeekBar) layoutVolume.findViewById(R.id.your_dialog_seekbar);
        sbVolume.setMax(100);
        sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onStopTrackingTouch(SeekBar seekBar) {
                tcpClient.sendDataToServer(new Commands().VOLUME + "_" + sbVolume.getProgress());
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
        });

        //Shutdown layout dialog
        DialogShutdown = new Dialog(this);
        LayoutInflater inflaterShutdown = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutShutdown = inflaterShutdown.inflate(R.layout.dialog_shutdown, (ViewGroup) findViewById(R.id.myShutDownDialog));
        AlertDialog.Builder builderShutdown = new AlertDialog.Builder(this).setView(layoutShutdown);
        DialogShutdown = builderShutdown.create();

        bCancelShutdown = (Button) layoutShutdown.findViewById(R.id.bCancelShutdown);
        bShutdown_0s = (Button) layoutShutdown.findViewById(R.id.b0s);
        bShutdown_3600s = (Button) layoutShutdown.findViewById(R.id.b3600s);
        bShutdown_5400s = (Button) layoutShutdown.findViewById(R.id.b5400s);
        bShutdown_7200s = (Button) layoutShutdown.findViewById(R.id.b7200s);
        bShutDown_sendOrder = (Button) layoutShutdown.findViewById(R.id.bConfirmShutdown);
        etShutDown = (EditText) layoutShutdown.findViewById(R.id.et_shutdown);

        bCancelShutdown.setOnClickListener(this);
        bShutdown_0s.setOnClickListener(this);
        bShutdown_3600s.setOnClickListener(this);
        bShutdown_5400s.setOnClickListener(this);
        bShutdown_7200s.setOnClickListener(this);
        bShutDown_sendOrder.setOnClickListener(this);

        //Soccer Dialog
        bSoccer = (Button) findViewById(R.id.bsoccer);
        bSoccer.setOnClickListener(this);


        //This event is fired each time that there's a change in the clipboard (user copies the desired link)
        clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            public void onPrimaryClipChanged() {

                //Get the link copied by the user
                String uri = clipboard.getText().toString();

                //addPrimaryClipChangedListener seems to be buggy. Sometimes it's called more than once. That's why there's a filter. If last uri equals current uri, don't do anything, just ignore it.
                if (!uri.equals(last_uri)) {

                    //If strings are different, make a copy of current uri in last_uri and go to ActionPerformerActivity main screen.
                    last_uri = uri;

                    //Try to send the link of the user
                    tcpClient.sendDataToServer(uri);

                    Intent openMainActivity = new Intent(ActionPerformerActivity.this, ActionPerformerActivity.class);
                    openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(openMainActivity);
                }
            }
        });

        mouseSpace.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {


                if (gestureDetector.onTouchEvent(event)) {



                } else {

                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN:

                            lastX = (int) event.getX();
                            lastY = (int) event.getY();
                            break;

                        case MotionEvent.ACTION_MOVE:
                            currentX = (int) event.getX();
                            currentY = (int) event.getY();
                            tcpClient.sendDataToServer(new Commands().MOUSE + "_" + (currentX - lastX) + "?" + (currentY - lastY) + "|");
                            lastX = currentX;
                            lastY = currentY;
                            break;
                    }
                }
                return true;
            }
        });
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            tcpClient.sendDataToServer(new Commands().MOUSE + "_" + "LEFTCLICK");
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            tcpClient.sendDataToServer(new Commands().MOUSE + "_" + "LEFTCLICK");
            tcpClient.sendDataToServer(new Commands().MOUSE + "_" + "LEFTCLICK");
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            tcpClient.sendDataToServer(new Commands().MOUSE + "_" + "RIGHTCLICK");
        }
    }

    //Button click handler. This code is executed each time a button is clicked (except exit button)
    @Override
    public void onClick(View v) {

        String uri = "";
        Commands commands = new Commands();

        //Open Browser
        if (bOpenBrowser.getId() == v.getId()) {

            uri = "http://www.google.es";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(browserIntent);
        }

        //Youtube
        else if (bYoutube.getId() == v.getId()) {

            uri = "http://www.youtube.es";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(browserIntent);
        }

        //Mitele
        else if (bMitele.getId() == v.getId()) {

            uri = "http://www.mitele.es";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(browserIntent);
        } else if (bSoccer.getId() == v.getId()) {

            Intent intent = new Intent(ActionPerformerActivity.this, SoccerActivity.class);
            startActivity(intent);
        }

        //Volume Control
        else if (bVolume.getId() == v.getId()) {

            tcpClient.sendDataToServer(new Commands().VOLUME);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String volume = tcpClient.receiveDataFromServer();
                    sbVolume.setProgress(Integer.parseInt(volume));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogVolume.show();
                        }
                    });
                }
            });
            thread.start();
        }


        //Shutdown PC
        else if (bShutdown.getId() == v.getId()) {
            DialogShutdown.show();
        } else if (bShutdown_0s.getId() == v.getId()) {
            etShutDown.setText("0");
        } else if (bShutdown_3600s.getId() == v.getId()) {
            etShutDown.setText("3600");
        } else if (bShutdown_5400s.getId() == v.getId()) {
            etShutDown.setText("5400");
        } else if (bShutdown_7200s.getId() == v.getId()) {
            etShutDown.setText("7200");
        } else if (bShutDown_sendOrder.getId() == v.getId()) {

            if (!etShutDown.getText().equals(""))
                tcpClient.sendDataToServer(new Commands().SHUTDOWN + "_" + etShutDown.getText());

        } else if (bCancelShutdown.getId() == v.getId()) {

            tcpClient.sendDataToServer(new Commands().SHUTDOWN + "_" + "CANCEL");
        }
    }


}
