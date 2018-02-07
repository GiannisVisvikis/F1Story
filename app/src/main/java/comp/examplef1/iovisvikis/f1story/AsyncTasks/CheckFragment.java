package comp.examplef1.iovisvikis.f1story.AsyncTasks;

import android.os.AsyncTask;
import android.os.Bundle;

import comp.examplef1.iovisvikis.f1story.Communication;
import comp.examplef1.iovisvikis.f1story.MyDialogs.UpdatesDialog;
import comp.examplef1.iovisvikis.f1story.R;

import org.json.JSONArray;



/**
 * Created by iovisvikis on 15/10/2016.
 */

public class CheckFragment extends android.support.v4.app.Fragment{

    private JSONArray seasons;

    private int currentProgress = 0;

    public void checkForUpdates(){

        if( ((Communication) getActivity()).apiResponds()){
            Bundle args = getArguments();

            int dataDrivers = args.getInt("TOTAL_DATABASE_DRIVERS");
            int dataConstructors = args.getInt("TOTAL_DATABASE_CONSTRUCTORS");
            int dataCircuits = args.getInt("TOTAL_DATABASE_CIRCUITS");
            int dataSeasons = args.getInt("TOTAL_DATABASE_SEASONS");
            int tableNumbers = args.getInt("TOTAL_DATABASE_TABLES");

            Object[] params = {dataDrivers, dataConstructors, dataCircuits, dataSeasons, tableNumbers, this};

            //check if remote entries are different than those stored and if so start a download and classify them
            AsynCheck check = new AsynCheck();
            check.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);

        }

    }


    public synchronized void updateDialogMessage(UpdatesDialog dialog, int downloadMax){

        String message;

        incrementCurrentProgress();
        Integer percentage = (int) ((currentProgress / (float) downloadMax) * 100);
        message = getResources().getString(R.string.downloading) + " " + percentage.toString() + "%";

        dialog.updateMessage(message);
    }


    private void incrementCurrentProgress(){currentProgress++;}

    public JSONArray getSeasons(){return seasons;}
    public void setSeasons(JSONArray seasons){this.seasons = seasons;}



}//CheckFragment


