package com.example.aaldridge.myclient.Actions.Soccer;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaldridge on 15/04/2016.
 */
public class SoccerClass {

    public ArrayList<SportEvent> GetListOfMatches(){

        ArrayList<String> events = new ArrayList<String>();
        ArrayList<SportEvent> parsedEvents = new ArrayList<>();

        String html = getHTMLfromURL("http://arenavision.in/agenda");
        String startOfDesiredData = "@ArenaVision_in for updates</p>\n" + "<p>";
        html = html.substring(html.indexOf(startOfDesiredData) + startOfDesiredData.length());

        while(true) {
            try {
                String event = html.substring(0, html.indexOf("<br/>\n"));
                events.add(event);
                html = html.substring(html.indexOf("<br/>\n") + 6);

            }catch (Exception ex){
                Log.d(ex.toString(), ex.toString());
                break;
            }
        }

        parsedEvents = ParseEvents(events);
        return parsedEvents;
    }

    private ArrayList<SportEvent> ParseEvents(ArrayList<String> events){

        ArrayList<SportEvent> parsedEvents = new ArrayList<>();

        for (int i = 0; i < events.size(); i++){

            SportEvent SingleEvent = new SportEvent();
            String helper = null;

            SingleEvent.Date = events.get(i).substring(0, events.get(i).indexOf(" "));
            helper = events.get(i).substring(events.get(i).indexOf(" "));
            SingleEvent.Time = helper.substring(0, events.get(i).indexOf(" ") - 2);
            helper = helper.substring((helper.indexOf("CET ") + 4));
            SingleEvent.Sport = helper.substring(0, helper.indexOf(":"));
            helper = helper.substring(helper.indexOf(":") + 2);
            SingleEvent.MatchInformation = helper.substring(0, helper.indexOf("("));
            helper = helper.substring(helper.indexOf("(") + 1);
            SingleEvent.Competition = helper.substring(0, helper.indexOf(")"));
            helper = helper.substring(helper.indexOf(")") + 2);

            int channelNumber = 0;

            while(true){

                try {
                    SingleEvent.Channels.add(helper.substring(helper.indexOf("AV") + 2, helper.indexOf("/")));
                    channelNumber++;
                    helper = helper.substring(helper.indexOf("/") + 1);
                }catch (Exception ex){
                    SingleEvent.Channels.add(helper.substring(2));
                    Log.d(ex.toString(), ex.toString());
                    break;
                }

            }
            parsedEvents.add(SingleEvent);
        }
        return parsedEvents;

    }

    private static String getHTMLfromURL(String theURL){

        URL url = null;
        StringBuffer buffer = null;

        try {
            url = new URL(theURL);
            InputStream is = url.openStream();
            int ptr = 0;
            buffer = new StringBuffer();
            while ((ptr = is.read()) != -1) {
                buffer.append((char)ptr);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer.toString();
    }

    public class SportEvent{

        public String Date;
        public String Time;
        public String Sport;
        public String MatchInformation;
        public String Competition;
        public List<String>  Channels = new ArrayList<String>();
    }
}
