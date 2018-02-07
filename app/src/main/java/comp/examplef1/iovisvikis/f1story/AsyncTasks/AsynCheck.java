package comp.examplef1.iovisvikis.f1story.AsyncTasks;

import android.app.FragmentManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import comp.examplef1.iovisvikis.f1story.APICommunicator;
import comp.examplef1.iovisvikis.f1story.Communication;
import comp.examplef1.iovisvikis.f1story.MainActivity;
import comp.examplef1.iovisvikis.f1story.MyDialogs.CheckDialog;
import comp.examplef1.iovisvikis.f1story.MyDialogs.UpdatesDialog;
import comp.examplef1.iovisvikis.f1story.R;


/**
 * Created by giannis on 8/12/2017.
 */
public class AsynCheck extends AsyncTask<Object, Object, UpdatesDialog>{

    private CheckFragment checkFragment;



    @Override
    protected UpdatesDialog doInBackground(Object... integers) {

        APICommunicator api = new APICommunicator();
        HashMap<String, AsyncTask> tasks = new HashMap<>(); //store the tasks here to access them later and apply get

        int dataDrivers = (int) integers[0];
        int dataConstructors = (int) integers[1];
        int dataCircuits = (int) integers[2];
        int dataSeasons = (int) integers[3];
        //determines whether to show a checking dialog or not. If there is no database and the API delays to respond, the user may choose to navigate to a super
        //fragment, thus creating a query to a non existing yet table and throw an sqlite exception. If tableNumbers is greater than 2, then no such exception is
        //likely to be thrown.
        int tablesNumber = (int) integers[4];

        checkFragment = (CheckFragment) integers[5];

        ((Communication) checkFragment.getActivity() ).blockOrientationChanges();
        ((Communication) checkFragment.getActivity() ).setSearchedForUpdates(true);

        int currentProgress = 0;

        CheckDialog checkDialog = new CheckDialog();
        UpdatesDialog dialog = new UpdatesDialog();

        if(tablesNumber < 3){

            //Log.e("CHECK_DIALOG", "Showing check dialog");
            publishProgress(new Object[]{0, checkDialog, dialog});
        }

        String[] driversFinalInfoString = api.getFinalRequestString(MainActivity.BASIC_URI + "drivers.json");
        String[] constructorsFinalInfoString = api.getFinalRequestString(MainActivity.BASIC_URI + "constructors.json");
        String[] circuitsFinalInfoString = api.getFinalRequestString(MainActivity.BASIC_URI + "circuits.json");
        String[] seasonsFinalInfoString = api.getFinalRequestString(MainActivity.BASIC_URI + "seasons.json");

        //Log.e("PARSEINFO", driversFinalInfoString[0]);

        int totalDrivers = Integer.parseInt(driversFinalInfoString[0]);
        int totalConstructors = Integer.parseInt(constructorsFinalInfoString[0]);
        int totalCircuits = Integer.parseInt(circuitsFinalInfoString[0]);
        int totalSeasons = Integer.parseInt(seasonsFinalInfoString[0]);

        String resultString = "";

        if(totalDrivers != dataDrivers)
            resultString += "drivers,";

        if(totalConstructors != dataConstructors)
            resultString += "constructors,";

        if(totalCircuits != dataCircuits)
            resultString += "circuits,";

        if(totalSeasons != dataSeasons){
            resultString += "seasons";
        }

        String[] results = resultString.split(",");

        if(results.length > 0 && !resultString.equals("")){//either, drivers, constructors, circuits or seasons number has changed

            publishProgress(new Object[]{1, checkDialog, dialog});

            int downloadMax = 0;

            downloadMax += results.length * totalSeasons; //workload for classification tasks

            //workload of allChoices task
            downloadMax += totalDrivers;
            downloadMax += totalConstructors;
            downloadMax += totalCircuits;

            String allSeasonsInfo = api.getInfo(seasonsFinalInfoString[1]);

            try{
                JSONObject mrData = new JSONObject(allSeasonsInfo);

                checkFragment.setSeasons(api.getData(mrData, "Seasons"));
            }
            catch (JSONException je){
                Log.e("AsynCheckInBack", je.getMessage());
            }

            for(int i=0; i<results.length; i++){

                String key, nameKey, familyName, idString, finalRequestString;

                ArrayList<String> list;

                HashMap<String, ArrayList<String>> erasMap;
                HashMap<String, String[]> idsMap;

                if(results[i].equalsIgnoreCase("drivers")) {

                    key = "Drivers";
                    nameKey = "givenName";
                    familyName = "familyName";
                    idString = "driverId";
                    erasMap = ( (Communication) checkFragment.getActivity() ).getDriversEras();
                    idsMap = ( (Communication) checkFragment.getActivity() ).getDriversIDsURLS();


                    finalRequestString = driversFinalInfoString[1];
                    list = ( (Communication) checkFragment.getActivity() ).getAllDrivers();
                }
                else if(results[i].equalsIgnoreCase("constructors")){

                    key = "Constructors";
                    nameKey = "name";
                    familyName = null;
                    idString = "constructorId";
                    erasMap = ( (Communication) checkFragment.getActivity() ).getConstructorsEras();
                    idsMap = ( (Communication) checkFragment.getActivity() ).getConstructorsIDsURLs();

                    finalRequestString = constructorsFinalInfoString[1];
                    list = ( (Communication) checkFragment.getActivity() ).getAllConstructors();
                }
                else if(results[i].equalsIgnoreCase("circuits")){
                    key = "Circuits";
                    nameKey = "circuitName";
                    familyName = null;
                    idString = "circuitId";
                    erasMap = ( (Communication) checkFragment.getActivity() ).getCircuitsEras();
                    idsMap = ( (Communication) checkFragment.getActivity() ).getCircuitsIDsURLs();

                    finalRequestString = circuitsFinalInfoString[1];
                    list = ( (Communication) checkFragment.getActivity() ).getAllCircuits();
                }
                else {
                    finalRequestString = null;
                    key = "Seasons";
                    nameKey = "season";
                    familyName = null;
                    erasMap = null;
                    idString = null;
                    list = ( (Communication) checkFragment.getActivity() ).getAllSeasons();
                    idsMap = ( (Communication) checkFragment.getActivity() ).getSeasonURLs();
                }

                if(!key.equalsIgnoreCase("seasons")) { //seasons do not need classification

                    ClassificationTask classify = new ClassificationTask();
                    Object[] classificationParams = {checkFragment.getSeasons(), key, nameKey, familyName, erasMap, checkFragment, dialog, downloadMax};

                    //will contain the task and it's parameters for execution in the main thread
                    Object[] classificationTaskPackage = new Object[]{classify,  classificationParams};
                    tasks.put("class " + key, classify);
                    publishProgress(classificationTaskPackage);
                }

                AllChoices allChoices = new AllChoices();
                Object[] allChoicesParams = {finalRequestString, key, nameKey, familyName, idString, list, idsMap, checkFragment, dialog, downloadMax};
                tasks.put("all " + key, allChoices);

                Object[] allChoicesPack = new Object[]{allChoices, allChoicesParams};
                publishProgress(allChoicesPack);

            }

            //wait for all to execute
            try {
                for (String key : tasks.keySet())
                    tasks.get(key).get();
            }
            catch (InterruptedException ie){
                Log.e("AsynCheckDoInBackGet", ie.getMessage());
            }
            catch (ExecutionException ee) {
                Log.e("AsynCheckDoInBackGet", ee.getMessage());
            }


            // ===== UPDATE THE DATABASE ======
            //setUp the update database processes
            //update the dialog title
            publishProgress(new Object[]{2, checkDialog, dialog});

            /*
            check the tasks HashMap if "all " + (Drivers - Seasons - Constructors - Circuits) tags and if not null
            then there is an update to be done. Initiate the corresponding task
            */

            UpdateData driversDataTask = null;
            UpdateData constructorsDataTask = null;
            UpdateData circuitsDataTask = null;
            UpdateData seasonsDataTask = null;

            //if some of these change, then the corresponding updating task will be triggered
            Object[] driversDataPack = new Object[]{null};
            Object[] constructorsDataPack = new Object[]{null};
            Object[] circuitsDataPack = new Object[]{null};
            Object[] seasonsDataPack = new Object[]{null};

            if(tasks.get("all Drivers") != null) { //Drivers need update
                driversDataTask = new UpdateData();
                Object[] driversDataParams = {checkFragment.getActivity(), "DRIVER_ID", "DRIVER_NAME"};
                driversDataPack = new Object[]{driversDataTask, driversDataParams};
            }

            if(tasks.get("all Constructors") != null) {
                constructorsDataTask = new UpdateData();
                Object[] constructorsDataParams = {checkFragment.getActivity(), "CONSTRUCTOR_ID", "CONSTRUCTOR_NAME"};
                constructorsDataPack = new Object[]{constructorsDataTask, constructorsDataParams};
            }

            if(tasks.get("all Circuits") != null) {
                circuitsDataTask = new UpdateData();
                Object[] circuitsDataParams = {checkFragment.getActivity(), "CIRCUIT_ID", "CIRCUIT_NAME"};
                circuitsDataPack = new Object[]{circuitsDataTask, circuitsDataParams};
            }

            if(tasks.get("all Seasons") != null) {
                seasonsDataTask = new UpdateData();
                Object[] seasonsDataParams = {checkFragment.getActivity(), null, "SEASON_NAME"};
                seasonsDataPack = new Object[]{seasonsDataTask, seasonsDataParams};
            }

            //fire up the tasks in the UI thread
            Object [] allTasksPackages = new Object[]{driversDataPack, constructorsDataPack, circuitsDataPack, seasonsDataPack};
            publishProgress(allTasksPackages);


            //wait for execution
            try {
                if(driversDataTask != null)
                    driversDataTask.get();

                if(constructorsDataTask != null)
                    constructorsDataTask.get();

                if(circuitsDataTask != null)
                    circuitsDataTask.get();

                if(seasonsDataTask != null)
                    seasonsDataTask.get();

            }
            catch (InterruptedException ie){
                Log.e("AsynCheckOnPost", ie.getMessage());
            }
            catch (ExecutionException ee){
                Log.e("AsynCheckOnPost", ee.getMessage());
            }

            while (!dialog.isVideoOver()){

                /*
                Wait for the video to complete before ending the task. Waiting in onPostExecute  instead of right here causes a bug.
                onPostExecute runs on UI thread like the video view in the dialog. In case the task executes
                before the video is done, the (in that case) infinite loop in the UI thread would not allow the on completion
                listener to run and the dialog would never end. Running it in here allows both treads to complete perfectly.
                 */

            }

        }


        return dialog;
    }



    @Override
    protected void onProgressUpdate(Object... values) {

        try{

            int progressCode = (int) values[0];

            CheckDialog checkDialog = (CheckDialog) values[1];

            if(progressCode == 0){

                FragmentManager fragmentManager = checkFragment.getActivity().getFragmentManager();
                checkDialog.show(fragmentManager, "CHECK_DIALOG");
                fragmentManager.executePendingTransactions();
            }
            else if(progressCode == 1) {

                UpdatesDialog dialog = (UpdatesDialog) values[2];

                if(checkDialog!= null && checkDialog.isVisible()){ // always initialized, but not shown to user if database not empty
                    checkDialog.dismiss();
                }

                //keep the screen awake for the download
                checkFragment.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                if(dialog != null){
                    FragmentManager fm = checkFragment.getActivity().getFragmentManager();
                    dialog.show(fm, "DIALOG_FRAG");
                    //force to show now, otherwise we might get a null pointer
                    fm.executePendingTransactions();
                }

            }
            else {

                UpdatesDialog dialog = (UpdatesDialog) values[2];

                if (dialog != null) {
                    dialog.updateMessage(checkFragment.getResources().getString(R.string.updating_database));
                }
            }

        }
        catch (ClassCastException cce) { //it is an object array containing [task, arguments for task] pairs

            if (values.length == 4) {

                //{null} or pair like
                //kindDataTask = new UpdateData();
                //Object[] kindDataParams = {checkFragment.getActivity(), null, "SEASON_NAME"};
                //kindDataPack = new Object[]{seasonsDataTask, seasonsDataParams};

                for(int index=0; index < values.length; index++){

                    Object[] possiblePair = (Object[]) values[index];

                    if(possiblePair.length > 1){ //it is not a {null}, it is {task, params} Object[]

                        //ex driversDataPack containing the Update task for the drivers and the params as an object[]
                        Object[] kindPack = (Object[]) values[index];

                        UpdateData kindDataTask = (UpdateData) kindPack[0];
                        Object[] kindDataParams = (Object[]) kindPack[1];
                        kindDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, kindDataParams);
                    }

                }

            }
            else {

                Object[] taskPack = values;

                Object[] taskParams = (Object[]) taskPack[1];

                if (taskParams.length == 8) { //classification task
                    ClassificationTask clasTask = (ClassificationTask) taskPack[0];
                    clasTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, taskParams);
                }
                else { //all choices task

                    AllChoices allTask = (AllChoices) taskPack[0];
                    allTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, taskParams);
                }

            }

        }

    }



    @Override
    protected void onPostExecute(UpdatesDialog dialog) {
        super.onPostExecute(dialog);

        if (dialog.isVisible()) { //is null if database needs no update

            dialog.dismiss();

            checkFragment.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            Toast.makeText(checkFragment.getActivity(), checkFragment.getResources().getString(R.string.database_ready), Toast.LENGTH_SHORT).show();
        }

        ( (Communication) checkFragment.getActivity()).allowOrientationChanges();

    }


}//AsynCheck

