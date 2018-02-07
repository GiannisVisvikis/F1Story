package comp.examplef1.iovisvikis.f1story.AsyncTasks;


import android.os.AsyncTask;
import android.util.Log;

import comp.examplef1.iovisvikis.f1story.APICommunicator;
import comp.examplef1.iovisvikis.f1story.MainActivity;
import comp.examplef1.iovisvikis.f1story.MyDialogs.UpdatesDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by iovisvikis on 15/10/2016.
 *
 * Used when the app is first loaded or the API is updated. It creates the app sqlite database which classifies drivers, constructors and circuits per decade
 *
 */

public class ClassificationTask extends AsyncTask<Object , Object, Void>
{

    private CheckFragment checkFrag;
    private APICommunicator api;


    @Override
    protected void onPreExecute() {
        api = new APICommunicator();
    }

    @Override
    protected Void doInBackground(Object... objects){

        JSONArray seasons = (JSONArray) objects[0]; //all seasons
        String key = (String) objects[1];
        String nameKey = (String) objects[2];
        String familyName = (String) objects[3];
        HashMap<String, ArrayList<String>> erasMap = (HashMap<String, ArrayList<String>>) objects[4];

        //useful for updating progress
        checkFrag = (CheckFragment) objects[5];
        UpdatesDialog dialog = (UpdatesDialog) objects[6];
        int downloadMax = (int) objects[7];

        HashMap<String, String> lastEntryMap = new HashMap<>();

        Object[] publishParams = new Object[]{dialog, downloadMax};

        for (int index=0; index < seasons.length(); index++) {

            try {

                String season = (seasons.getJSONObject(index)).getString("season");
                String keyInfo = api.getFinalRequestString(MainActivity.BASIC_URI + season + "/" + key + ".json")[1]; //0 gives you the total entries

                String keysJSON = api.getInfo(keyInfo);
                JSONObject keysRoot = new JSONObject(keysJSON);

                //JSONObject keysObject = api.getData(keysRoot, key, 0);
                //JSONArray keysArray = keysObject.getJSONArray(key);

                JSONArray keysArray = api.getData(keysRoot, key);

                //Log.d("ClassifDoInBack", "Processing season " + season + " in " + key);

                for (int index2=0; index2<keysArray.length(); index2++){

                    JSONObject keyObject = keysArray.getJSONObject(index2);

                    String name = keyObject.getString(nameKey);

                    String lastName = "";
                    if (familyName != null)
                        lastName += " " + keyObject.getString(familyName);

                    int seasonYear = Integer.parseInt(season);
                    int year = seasonYear % 10;

                    String eraKey = Integer.toString(seasonYear - year) + 's';

                    if (erasMap.get(eraKey) == null) {
                        erasMap.put(eraKey, new ArrayList<String>());
                    }

                    //either I've never seen him before, or he is not in added that era yet
                    if (lastEntryMap.get(name + lastName) == null || !lastEntryMap.get(name + lastName).equalsIgnoreCase(eraKey)) {
                        lastEntryMap.put(name + lastName, eraKey);
                        erasMap.get(eraKey).add(name + lastName);
                    }


                }
            } catch (JSONException e) {
                Log.e("ClassTask", e.getMessage());
            }

            publishProgress(publishParams);
        }

        return null;
    }


    @Override
    protected void onProgressUpdate(Object... values) {

        UpdatesDialog dialog = (UpdatesDialog) values[0];
        int downloadMax = (int) values[1];

        checkFrag.updateDialogMessage(dialog, downloadMax);
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        //free up resources
        checkFrag = null;
        api = null;
    }



}//ClassificationTask




