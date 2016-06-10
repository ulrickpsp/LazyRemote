package com.example.aaldridge.myclient.Actions.Soccer;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aaldridge.myclient.Actions.Commands;
import com.example.aaldridge.myclient.Connection.TCPClient;
import com.example.aaldridge.myclient.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class SoccerActivity extends AppCompatActivity implements ExpandableListView.OnChildClickListener{


    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader, listDataContent;
    HashMap<String, List<String>> listDataChild;
    ProgressDialog progress;
    TCPClient tcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soccer);

        tcpClient = TCPClient.getInstance();

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.expandableListView);
        expListView.setOnChildClickListener(this);

        progress = new ProgressDialog(this);
        progress.setMessage("Cargando lista de partidos.. :)");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(true);
        progress.show();

        // preparing list data
        getMatchesInformation();
    }

    private void getMatchesInformation() {

        MiTareaAsincrona tarea2 = new MiTareaAsincrona();
        tarea2.execute();
    }


    private class MiTareaAsincrona extends AsyncTask<Void, Void, ArrayList<SoccerClass.SportEvent>> {
        @Override
        protected ArrayList<SoccerClass.SportEvent> doInBackground(Void... params) {

            SoccerClass soccerActivity = new SoccerClass();
            ArrayList<SoccerClass.SportEvent> parsedEvents = soccerActivity.GetListOfMatches();

            return parsedEvents;
        }


        @Override
        protected void onPostExecute(ArrayList<SoccerClass.SportEvent> result) {

            listDataHeader = new ArrayList<String>();
            listDataContent = new ArrayList<String>();
            listDataChild = new HashMap<String, List<String>>();

            progress.dismiss();

            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
            String strDate = sdf.format(date);

            for (int i = 0; i < result.size(); i++){

                if(result.get(i).Date.equals(strDate) && result.get(i).Sport.equals("FUTBOL")) {

                    listDataHeader.add((result.get(i).Time) + ": " + result.get(i).Competition);
                    listDataContent.add(result.get(i).MatchInformation);
                    listDataChild.put(listDataHeader.get(listDataHeader.size() - 1), result.get(i).Channels);

                }
            }

            listAdapter = new com.example.aaldridge.myclient.Actions.Soccer.ExpandableListAdapter(getApplicationContext(), listDataHeader, listDataContent, listDataChild);

            // setting list adapter
            expListView.setAdapter(listAdapter);
        }


    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
                                int groupPosition, int childPosition, long id) {
        Toast.makeText(getApplicationContext(), "Abriendo canal en PC...", Toast.LENGTH_LONG).show();


        //Initialize some values
        TCPClient tcpClient = TCPClient.getInstance();
        TextView tv= (TextView) v.findViewById(R.id.lblListItem);
        String data= tv.getText().toString();
        tcpClient.sendDataToServer("Soccer_" + data);
        return true;
    }



}
