package comp.examplef1.iovisvikis.f1story.MyAdapters;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by iovisvikis on 14/4/2017.
 */

public class CalendarRace{


    private String circuitUrl, circuitName, circuitId, countryName, locality, date, time;

    public String getCircuitUrl() {
        return circuitUrl;
    }

    public String getCircuitName() {
        return circuitName;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getLocality() {
        return locality;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getCircuitId() {
        return circuitId;
    }

    public CalendarRace(JSONObject raceObject){

        try{
            this.date = raceObject.getString("date");
            this.time = raceObject.getString("time");

            JSONObject circuitObject = raceObject.getJSONObject("Circuit");
            this.circuitUrl = circuitObject.getString("url");
            this.circuitName = circuitObject.getString("circuitName");
            this.circuitId = circuitObject.getString("circuitId");

            JSONObject location = circuitObject.getJSONObject("Location");
            this.locality = location.getString("locality");
            this.countryName = location.getString("country");
        }
        catch (JSONException e){
            Log.e("CALENDARACE", e.getMessage());
        }


    }


}//CalendarRace



