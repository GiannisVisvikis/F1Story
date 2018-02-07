package comp.examplef1.iovisvikis.f1story.AsyncTasks;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeoutException;

import static android.content.Context.CONNECTIVITY_SERVICE;


/**
 * Created by iovisvikis on 21/2/2017.
 */

public class CheckConnection extends AsyncTask <Activity, Void, Object[]>{


    @Override
    protected Object[] doInBackground(Activity... appCompatActivities) {

        Activity context = appCompatActivities[0];

        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = conManager.getActiveNetworkInfo();

        boolean aBoolean = (netInfo != null && netInfo.isConnectedOrConnecting());

        return new Object[]{context, aBoolean};

    }



    @Override
    protected void onPostExecute(Object... objects) {

        Activity context = (Activity) objects[0];
        boolean aBoolean = (boolean) objects[1];

        if(!aBoolean){
            Toast.makeText(context, "Check internet connection", Toast.LENGTH_SHORT).show();
        }

    }


}


