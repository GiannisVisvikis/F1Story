package comp.examplef1.iovisvikis.f1story.AsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import comp.examplef1.iovisvikis.f1story.APICommunicator;
import comp.examplef1.iovisvikis.f1story.MyDialogs.CheckDialog;
import comp.examplef1.iovisvikis.f1story.MyDialogs.UpdatesDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by iovisvikis on 18/10/2016.
 */

public class AllChoices extends AsyncTask<Object, Object, Void> {


    @Override
    protected Void doInBackground(Object... objects) {

        String finalRequestString = (String) objects[0];
        String key = (String) objects[1]; //drivers, constructors, circuits etc
        String nameKey = (String) objects[2];
        String familyName = (String) objects[3];
        String idString = (String) objects[4];
        ArrayList<String> list = (ArrayList<String>) objects[5];
        HashMap<String, String[]> idsMap = (HashMap<String, String[]>) objects[6];
        CheckFragment checkFrag = (CheckFragment) objects[7];

        UpdatesDialog dialog = (UpdatesDialog) objects[8];
        int downloadMax = (int) objects[9];

        APICommunicator api = new APICommunicator();

        try {

            JSONArray keysArray;

            if(finalRequestString != null) {
                String mrDataString = api.getInfo(finalRequestString);
                JSONObject mrData = new JSONObject(mrDataString);

                //JSONObject keyRoot = api.getData(mrData, key, 0);
                //keysArray = keyRoot.getJSONArray(key);

                keysArray = api.getData(mrData, key);
            }
            else {
                keysArray = checkFrag.getSeasons();
            }

            //just in case
            list.clear();

            Object[] publishParams = new Object[]{checkFrag, downloadMax, dialog};

            for (int index = 0; index < keysArray.length(); index++) {

                JSONObject keyObject = keysArray.getJSONObject(index);

                String name;
                String lastName = "";
                String id = "";
                String url;


                name = keyObject.getString(nameKey);

                if (familyName != null)
                    lastName = " " + keyObject.getString(familyName);

                if(idString != null)
                    id = keyObject.getString(idString);

                //Log.d("AllChoDoInBack", "Processing " + name + lastName + " in " + key);

                list.add(name + lastName);

                url = keyObject.getString("url");

                String[] array = new String[]{id, url};

                idsMap.put(name + lastName, array);

                publishProgress(publishParams);
            }
        }
        catch(JSONException je){
            Log.e("AllChoicesTaskInBack", je.getMessage());
        }


        return null;
    }


    @Override
    protected void onProgressUpdate(Object... values) {

        int downloadMax = (int) values[1];

        UpdatesDialog dialog = (UpdatesDialog) values[2];

        ( (CheckFragment) values[0]).updateDialogMessage(dialog, downloadMax);
    }



}//Allchoices




