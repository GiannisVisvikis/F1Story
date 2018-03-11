package comp.examplef1.iovisvikis.f1story;

import comp.examplef1.iovisvikis.f1story.AsyncTasks.ApiAnswers;
import comp.examplef1.iovisvikis.f1story.AsyncTasks.CheckConnection;
import comp.examplef1.iovisvikis.f1story.AsyncTasks.CheckFragment;
import comp.examplef1.iovisvikis.f1story.AsyncTasks.DownloadFragment;
import comp.examplef1.iovisvikis.f1story.MyAdapters.CalendarRaceAdapter;
import comp.examplef1.iovisvikis.f1story.MyAdapters.NewsAdapter;
import comp.examplef1.iovisvikis.f1story.MyAdapters.NewsSitesAdapter;
import comp.examplef1.iovisvikis.f1story.MyAdapters.QualifyingResultsAdapter;
import comp.examplef1.iovisvikis.f1story.MyAdapters.UIResultAdapter;
import comp.examplef1.iovisvikis.f1story.MyDialogs.MultipleSelectionDialog;
import comp.examplef1.iovisvikis.f1story.MyDialogs.NotificationsDialog;
import comp.examplef1.iovisvikis.f1story.MyDialogs.SingleSelectionDialog;
import comp.examplef1.iovisvikis.f1story.MyDialogs.VideoDialog;

import com.google.android.gms.ads.MobileAds;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

import android.content.res.Configuration;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import android.net.Uri;

import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements Communication{

    // Remove the below line after defining your own ad unit ID.
    //private static final String TOAST_TEXT = "Test ads are being shown. "
            //+ "To show live ads, replace the ad unit ID in res/values/strings.xml with your own ad unit ID.";


    public static final String BASIC_URI = "https://ergast.com/api/f1/";
    public static final String DATABASE_NAME = "F1_STORY.db";
    public static final String NEWS_TABLES_DATABASE = "NEWS_SITES.db";
    public static final String SHARED_PREFERENCES_TAG = "com_example_visvikis_f1storypreferences";
    private final String LAST_ENTRY_TAG = "LAST_ENTRY";

    //fragments tags
    private final String CHECK_FRAGMENT_TAG = "CHECK_FRAG";
    private final String DOWNLOAD_FRAGMENT_TAG = "DOWN_FRAG";
    private final String IN_UI_TAG = "IN_UI_FRAG";
    private final String IN_RES_TAG = "IN_RES_FRAG";
    private final String SOUND_FRAGMENT_TAG = "SOUND_FRAG";
    private final String RESULT_FRAGMENT_TAG = "RESULT_FRAG";
    private final String BACK_STACK_KEY = "THE_BACK_STACK_KEY";

    public static final String DRIVER_FRAGMENT_TAG = "DRIVER_FRAG";
    public static final String CONSTRUCTOR_FRAGMENT_TAG = "CONSTRUCTOR_FRAG";
    public static final String CIRCUIT_FRAGMENT_TAG = "CIRCUIT_FRAG";

    private final String SOUND_PREFERENCE_KEY = "SOUND_PREFERENCE";
    public final String NOTIFICATIONS_PREFERENCE_KEY = "NOTIFICATIONS_PREFERENCE";

    private static ArrayList<Object> myBackStack;

    //====Only needed for downloading and updating the sqlite database when necessary
    //Maps used if database is empty or needs to update
    private HashMap<String, ArrayList<String>> driverEras;
    private HashMap<String, ArrayList<String>> constructorEras;
    private HashMap<String, ArrayList<String>> circuitEras;

    //driver names --> [ID,URL] will be stored here
    private HashMap<String, String[]> driverIDsURLs;
    //constructor names --> [ID,URL] will be stored here
    private HashMap<String, String[]> constructorIDsURLs;
    //circuit names --> [IDs,URLs] will be stored here
    private HashMap<String, String[]> circuitIDsURLs;
    private HashMap<String, String[]> seasonURLs;

    private ArrayList<String> allDrivers;
    private ArrayList<String> allConstructors;
    private ArrayList<String> allCircuits;
    private ArrayList<String> allSeasons;


    private boolean searchedForUpdates = false;

    private boolean soundsOn, notificationsOn;
    private boolean playStartupSound = true;

    private Menu activityMenu; //reference for updating the icons

    private SQLiteDatabase f1DataBase;

    private ResultFragment resFrag;
    private CheckFragment checkFrag;
    private DownloadFragment downloadFragment;
    private SoundFragment soundFrag;

    private SuperFragment driversFragment, constructorFragment, circuitFragment;

    private android.support.v4.app.Fragment inUInow;
    private ResultFragment inRESnow;

    private String inUiPlaceTag = null; //keeps track of which fragment is currently added in the container
    private String inResPlaceTag = null;
    private String lastUIEntry = "";


    private Toolbar theAppBar;
    private DrawerLayout theDrawerLayout;
    private NavigationScrollViewFragment drawerFragment;
    private View drawerView;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //block orientation changes and check if there are updates available
        blockOrientationChanges();

        //SET UP THE INITIAL UI

        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();

        //check for prior states
        if(savedInstanceState == null){

            //initialize the fragments
            downloadFragment = new DownloadFragment();
            soundFrag = new SoundFragment();
            checkFrag = new CheckFragment();

            myBackStack = new ArrayList<>();

            ft.add(downloadFragment, DOWNLOAD_FRAGMENT_TAG);
            ft.add(checkFrag, CHECK_FRAGMENT_TAG);
            ft.add(soundFrag, SOUND_FRAGMENT_TAG);

        }
        else{ //get them

            downloadFragment = (DownloadFragment) fm.findFragmentByTag(DOWNLOAD_FRAGMENT_TAG);

            soundFrag = (SoundFragment) fm.findFragmentByTag(SOUND_FRAGMENT_TAG);

            driversFragment = (SuperFragment) fm.findFragmentByTag(DRIVER_FRAGMENT_TAG);
            constructorFragment = (SuperFragment) fm.findFragmentByTag(CONSTRUCTOR_FRAGMENT_TAG);
            circuitFragment = (SuperFragment) fm.findFragmentByTag(CIRCUIT_FRAGMENT_TAG);

            inUiPlaceTag = savedInstanceState.getString(IN_UI_TAG);
            inUInow = fm.findFragmentByTag(inUiPlaceTag);

            inResPlaceTag = savedInstanceState.getString(IN_RES_TAG);
            inRESnow = (ResultFragment) fm.findFragmentByTag(inResPlaceTag);

            lastUIEntry = savedInstanceState.getString(LAST_ENTRY_TAG);

            searchedForUpdates = savedInstanceState.getBoolean("SEARCHED_FOR_UPDATES");
            setPlayStartupSound(savedInstanceState.getBoolean("PLAY_SOUND"));

            //check fragment no longer retained
            if(!searchedForUpdates) {
                //not likely
                checkFrag = new CheckFragment();
            }
        }



        //set the toolbar
        theAppBar = findViewById(R.id.the_toolbar);
        setSupportActionBar(theAppBar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        theDrawerLayout = findViewById(R.id.the_drawer_layout);

        //get the drawer fragment instance
        drawerFragment = (NavigationScrollViewFragment) getSupportFragmentManager().findFragmentById(R.id.drawerFragment);
        //get it's view
        drawerView = findViewById(R.id.drawerFragment);

        drawerFragment.setTheDrawerFragment(drawerView, theDrawerLayout, theAppBar);


        //access the database
        f1DataBase = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);

        //retrieve user settings regarding sounds and notidications options from previous use if any stored in the database
        this.soundsOn = getFromPreferences(SOUND_PREFERENCE_KEY, true);
        this.notificationsOn = getFromPreferences(NOTIFICATIONS_PREFERENCE_KEY, false);



        //look for any updates
        //setup the search for updates and the checkFragment if not already checked
        if(!searchedForUpdates){

            Bundle args = new Bundle();

            //Not efficient to use a rawQuery and a cursor for 1x1 results. Use DatabaseUtils.longForQuery or .longForString
            long tablesNumber = DatabaseUtils.longForQuery(f1DataBase, "select count(name) from sqlite_master where type = ?", new String[]{"table"});

            //Log.d("TOTAL_TABLES", tablesNumber.toString());

            if(tablesNumber > 2){ //there's already an sqlite database in store with always at least one (android_metadata)
                // + the one we might have created for the notifications data

                int totalDataDrivers = (int) DatabaseUtils.longForQuery(f1DataBase, "SELECT COUNT(*) FROM ALL_DRIVERS;", null);
                int totalDataConstructors = (int) DatabaseUtils.longForQuery(f1DataBase, "SELECT COUNT(*) FROM ALL_CONSTRUCTORS;", null);
                int totalDataCircuits = (int) DatabaseUtils.longForQuery(f1DataBase, "SELECT COUNT(*) FROM ALL_CIRCUITS;", null);
                int totalDataSeasons = (int) DatabaseUtils.longForQuery(f1DataBase, "SELECT COUNT(*) FROM ALL_SEASONS;", null);

                //set up the check fragment to look for any updates
                args.putInt("TOTAL_DATABASE_DRIVERS", totalDataDrivers);
                args.putInt("TOTAL_DATABASE_CONSTRUCTORS", totalDataConstructors);
                args.putInt("TOTAL_DATABASE_CIRCUITS", totalDataCircuits);
                args.putInt("TOTAL_DATABASE_SEASONS", totalDataSeasons);
            }
            else { //set empty values, create the sqlite database
                args.putInt("TOTAL_DATABASE_DRIVERS", 1);
                args.putInt("TOTAL_DATABASE_CONSTRUCTORS", 1);
                args.putInt("TOTAL_DATABASE_CIRCUITS", 1);
                args.putInt("TOTAL_DATABASE_SEASONS", 1);

            }

            args.putInt("TOTAL_DATABASE_TABLES", (int) tablesNumber);

            checkFrag.setArguments(args);
        }


        //Place fragments according to whether it is a large screen or not and orientation changes

        if(inUiPlaceTag != null){ //there was a fragment in UI place before

            inUInow = fm.findFragmentByTag(inUiPlaceTag);

            if(!inUInow.isAdded())
                ft.add(R.id.uiFragmentPlace, inUInow, IN_UI_TAG);
        }

        if(!inSmallScreenMode()){//there is a place for result fragment

            if(inResPlaceTag != null) {

                resFrag = (ResultFragment) fm.findFragmentByTag(inResPlaceTag);

                if (!resFrag.isAdded()) {
                    ft.add(R.id.resultFragmentPlace, resFrag, IN_RES_TAG);
                    inRESnow = resFrag;
                    inResPlaceTag = IN_RES_TAG;
                }
            }
        }

        ft.commit();
        fm.executePendingTransactions();

        //TODO uncomment, replace app_id and banner_id strings and change version code in the app.build before release
        //MobileAds.initialize(this, getResources().getString(R.string.addMob_app_id));

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        // Toasts the test ad message on the screen. Remove this after defining your own ad unit ID.
        //Toast.makeText(this, TOAST_TEXT, Toast.LENGTH_LONG).show();

    }//onCreate



    @Override
    protected void onStart(){
        super.onStart();

        //Check for Internet Connection
        if (!hasInternetConnection()) {
            Intent noInternetIntent = new Intent(MainActivity.this, NoConnectionActivity.class);
            startActivity(noInternetIntent);
            finish();
        }
        else if (!searchedForUpdates) {
            driverEras = new HashMap<>();
            constructorEras = new HashMap<>();
            circuitEras = new HashMap<>();

            allDrivers = new ArrayList<>();
            allConstructors = new ArrayList<>();
            allCircuits = new ArrayList<>();
            allSeasons = new ArrayList<>();

            driverIDsURLs = new HashMap<>();
            constructorIDsURLs = new HashMap<>();
            circuitIDsURLs = new HashMap<>();
            seasonURLs = new HashMap<>();

            checkFrag.checkForUpdates();
            searchedForUpdates = true;
        }
        else {
            //don't need the check fragment any more, orientation changes were block inside onCreate for checking
            allowOrientationChanges();
        }

        //don't need it anymore, make it  available to GC, free up resources
        checkFrag = null;
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (isPlayStartupSound()){
            //make engine sound
            getSoundFragment().playSound("sounds/app_start.mp3");
            setPlayStartupSound(false);
        }

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("SEARCHED_FOR_UPDATES", searchedForUpdates);
        outState.putBoolean("PLAY_SOUND", isPlayStartupSound());

        //remember which fragment was in UIFragPlace
        outState.putString(IN_UI_TAG, inUiPlaceTag);
        outState.putString(IN_RES_TAG, inResPlaceTag);
        outState.putString(LAST_ENTRY_TAG, lastUIEntry);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_options_menu, menu);

        activityMenu = menu;

        //restore past selections recovered from shared preferences in onCreate method to menu icons
        MenuItem soundItem = activityMenu.findItem(R.id.soundsMenu);
        MenuItem notificationsItem = activityMenu.findItem(R.id.notificationsMenu);

        if(soundsOn)
            soundItem.setIcon(R.mipmap.ic_volume_up_black_24dp);
        else
            soundItem.setIcon(R.mipmap.ic_volume_off_black_24dp);

        if(notificationsOn)
            notificationsItem.setIcon(R.mipmap.ic_notifications_black_24dp);
        else
            notificationsItem.setIcon(R.mipmap.ic_notifications_off_black_24dp);

        onPrepareOptionsMenu(activityMenu);

        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.soundsOn:
                setSoundsOn(true);
                return true;

            case R.id.soundsOff:
                getSoundFragment().stopSound();
                setSoundsOn(false);
                return  true;

            case R.id.notificationsOn:
                //create the notifications table if it not yet exists
                try{
                    String notifTableCreate = "create table if not exists notifications_table (notif_id integer not null, event text not null, time_to_event text not null);";
                    f1DataBase.execSQL(notifTableCreate);
                }
                catch (SQLiteException sql){
                    Log.e("NotifDialog/212", sql.getLocalizedMessage());
                }

                NotificationsDialog notifydialog = new NotificationsDialog();
                notifydialog.show(getSupportFragmentManager(), "NOTIFICATION_DIALOG");
                return true;

            case R.id.notificationsOff:
                cancelAllNotifications();
                return true;

            case R.id.williamSupreme:
                getSoundFragment().stopSound();
                Intent supremeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/ULTtWUZhD9c"));
                startActivity(supremeIntent);
                return true;

            case R.id.harrisonFaster:
                getSoundFragment().stopSound(); //in case something plays in the background
                Intent fasterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/tSN04eFX2to"));
                startActivity(fasterIntent);
                return true;

            case R.id.replayIntro:
                VideoDialog videoDialog = new VideoDialog();
                videoDialog.show(getFragmentManager(), "VIDEO_DIALOG");
                return true;

            case R.id.blown_exhausts:
                getSoundFragment().playSound("sounds/blown_exhausts.mp3");
                return true;

            case R.id.downshifting:
                getSoundFragment().playSound("sounds/downshifting.mp3");
                return true;

            case R.id.multiple_pass:
                getSoundFragment().playSound("sounds/multiple_pass.mp3");
                return true;

            case R.id.v8_sound:
                getSoundFragment().playSound("sounds/bmw_v8.mp3");
                return true;

            case R.id.v12_sound:
                getSoundFragment().playSound("sounds/ferrari_v12.mp3");
                return true;

            case R.id.matraFord1969:
                getSoundFragment().playSound("sounds/ford_matra_1969.mp3");
                return true;

            case R.id.ferrari312:
                getSoundFragment().playSound("sounds/ferrari312b.mp3");
                return true;

            default:
                return false;
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        f1DataBase.close();

    }



    /**
     * I have to create another pending intent with the same request code and cancel it in order to cancel an existing alarm
     */
    @Override
    public void cancelAllNotifications() {

        try {

            String query = "select notif_id, event, time_to_event from notifications_table;";

            Cursor notificationsIds = f1DataBase.rawQuery(query, null);

            if(notificationsIds.moveToFirst()){

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                do {

                    //create a new alarm with the same exact pending intent and cancel it
                    int requestCode = notificationsIds.getInt(0);

                    Intent notiFyIntent = new Intent(MainActivity.this, NotificationBroadcast.class);

                    PendingIntent mAlarmPendingIntent = PendingIntent.getBroadcast(this, requestCode, notiFyIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    alarmManager.cancel(mAlarmPendingIntent);

                    //clear the notifications_table in the database
                    try{

                        f1DataBase.execSQL("delete from notifications_table where notif_id = " + requestCode + ";");

                    }catch (SQLiteException sql){
                        Log.e("MainAct/clearNotifs", sql.getMessage());
                    }

                }
                while (notificationsIds.moveToNext());

                //update the preferences file
                setNotificationsOn(false);
            }

        }
        catch (SQLiteException sqlite){ //Notifications off selected before any notifications were set, no table yet exists
            Log.e("CANCEL_NOTIFS", sqlite.getMessage());
        }

    }


    @Override
    public void setSearchedForUpdates(boolean searchedForUpdates) {
        this.searchedForUpdates = searchedForUpdates;
    }


    private boolean isPlayStartupSound() {
        return playStartupSound;
    }

    private void setPlayStartupSound(boolean playStartupSound) {
        this.playStartupSound = playStartupSound;
    }



    public void setSoundsOn(boolean on){
        this.soundsOn = on;

        writeToPreferences(SOUND_PREFERENCE_KEY, on);

        if(on)
            activityMenu.findItem(R.id.soundsMenu).setIcon(R.mipmap.ic_volume_up_black_24dp);
        else
            activityMenu.findItem(R.id.soundsMenu).setIcon(R.mipmap.ic_volume_off_black_24dp);

        onPrepareOptionsMenu(activityMenu); //refresh the menu, icons have changed
    }


    @Override
    public  void setNotificationsOn(boolean on){
        this.notificationsOn = on;

        writeToPreferences(NOTIFICATIONS_PREFERENCE_KEY, on);

        if(on)
            activityMenu.findItem(R.id.notificationsMenu).setIcon(R.mipmap.ic_notifications_black_24dp);
        else
            activityMenu.findItem(R.id.notificationsMenu).setIcon(R.mipmap.ic_notifications_off_black_24dp);

        onPrepareOptionsMenu(activityMenu); //refresh the menu, icons have changed
    }




    @Override
    public boolean isSoundsOn() {
        return this.soundsOn;
    }


    @Override
    public DownloadFragment getDownloadFragment(){
        return this.downloadFragment;
    }

    @Override
    public HashMap<String, ArrayList<String>> getDriversEras() {
        return driverEras;
    }

    @Override
    public HashMap<String, ArrayList<String>> getConstructorsEras() {
        return constructorEras;
    }

    @Override
    public HashMap<String, ArrayList<String>> getCircuitsEras() {
        return circuitEras;
    }

    @Override
    public ArrayList<String> getAllSeasons() {
        return allSeasons;
    }

    @Override
    public ArrayList<String> getAllDrivers() {
        return allDrivers;
    }

    @Override
    public ArrayList<String> getAllConstructors() {
        return allConstructors;
    }

    @Override
    public ArrayList<String> getAllCircuits() {
        return allCircuits;
    }

    @Override
    public HashMap<String, String[]> getDriversIDsURLS() {
        return driverIDsURLs;
    }

    @Override
    public HashMap<String, String[]> getConstructorsIDsURLs() {
        return constructorIDsURLs;
    }

    @Override
    public HashMap<String, String[]> getCircuitsIDsURLs() {
        return circuitIDsURLs;
    }

    @Override
    public HashMap<String, String[]> getSeasonURLs() {
        return seasonURLs;
    }


    @Override
    public SQLiteDatabase getAppDatabase() {
        return this.f1DataBase;
    }

    public View getDrawerView(){return this.drawerView;}

    public boolean hasInternetConnection(){

        boolean result = false;

        CheckConnection conTask = new CheckConnection();

        Activity[] params = new Activity[]{this};

        try {

            Object[] taskResults = conTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params).get();
            result = (boolean) taskResults[1];
        }
        catch (InterruptedException e1){
            Log.e("hasInternetConn", e1.getMessage());
        }
        catch (ExecutionException e2){
            Log.e("hasInternetConn", e2.getMessage());
        }

        return result;
    }



    @Override
    public boolean apiResponds() {

        boolean result = false;

        ApiAnswers apiTask = new ApiAnswers();

        try {
            result = apiTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
        } catch (InterruptedException e1) {
            Log.e("apiResponds", e1.getMessage());
        } catch (ExecutionException e2){
            Log.e("apiResponds", e2.getMessage());
        }

        //if Api does not respond ask user to try again later
        if(result == false){
            startActivity(new Intent(this, NoResponseActivity.class));
            this.finish();
        }

        return result;
    }



    @Override
    public SoundFragment getSoundFragment() {
        return soundFrag;
    }



    @Override
    public void onDialogPositiveClick(String userInput, String key){

        String finalQuery = BASIC_URI + userInput;

        Object[] params;

        if(key.contains("/1")) {
            String newKey = deSlash(key);
            params = new Object[]{finalQuery, newKey, getDownloadFragment(), getResources().getString(R.string.getting_data), new Bundle()};
        }
        else
            params = new Object[]{finalQuery, key, getDownloadFragment(), getResources().getString(R.string.getting_data)};

        downloadFragment.startListAdapterTask(params);

    }



    @Override
    public void launchSingleSelectionDialog(Bundle args){

        SingleSelectionDialog dialog = (SingleSelectionDialog) getSupportFragmentManager().findFragmentByTag("SINGLE_DIALOG");

        if (dialog == null)
            dialog = new SingleSelectionDialog();

        dialog.setArguments(args);

        dialog.show(getSupportFragmentManager(), "SINGLE_DIALOG");

    }



    @Override
    public void launchMultipleSelectionDialog(Bundle args){

        MultipleSelectionDialog dialog = (MultipleSelectionDialog) getSupportFragmentManager().findFragmentByTag("MULTIPLE_DIALOG");

        if(dialog == null)
            dialog = new MultipleSelectionDialog();

        dialog.setArguments(args);

        dialog.show(getSupportFragmentManager(), "MULTIPLE_DIALOG");

        getSupportFragmentManager().executePendingTransactions();
    }



    @Override
    public boolean inSmallScreenMode(){

        return (findViewById(R.id.resultFragmentPlace) == null);

    }



    //if in small screen mode the result frag will disable orientation changes
    @Override
    public void onBackPressed() {

        if(myBackStack.size() > 0){

            int index = myBackStack.size() - 1;

            Object backStackEntry = myBackStack.get(index);

            if( inSmallScreenMode() && (backStackEntry instanceof String) ){

                //then it is a tag of a fragment that needs to be put back in the ui
                String fragTag = (String) backStackEntry;

                SuperFragment toRestore = (SuperFragment) findOrCreateNewFragment(fragTag);
                //toRestore.restoreLastEntry(lastUIEntry);
                addFragment(toRestore);
                lastUIEntry = "";
            }
            else {
                RecyclerView.Adapter adapterStored = (RecyclerView.Adapter) myBackStack.get(index); //get new last insertion

                ResultFragment frag = (ResultFragment) getSupportFragmentManager().findFragmentByTag(RESULT_FRAGMENT_TAG);

                if (frag == null)
                    frag = (ResultFragment) findOrCreateNewFragment(RESULT_FRAGMENT_TAG);

                addFragment(frag);

                if(adapterStored != null){

                    frag.setResultAdapter(adapterStored);
                }
            }

            myBackStack.remove(index); //remove last insertion

            getSoundFragment().playSound("sounds/gears_down.mp3");

        }
        else {

            allowOrientationChanges();

            if(theDrawerLayout.isDrawerOpen(drawerView)){

                if(isSoundsOn()){

                    getSoundFragment().playSound("sounds/app_closed.mp3");

                    while (getSoundFragment().isExitSoundPlaying()){
                        //wait for the exit sound to stop before exiting the app
                    }
                }

                super.onBackPressed();
            }
            else
                theDrawerLayout.openDrawer(drawerView);
        }

    }



    @Override
    public void blockOrientationChanges(){

        //find out what the orientation is and keep it until releasing it again
        int config = getResources().getConfiguration().orientation;

        //set this as permanent until informed otherwise

          if(config == Configuration.ORIENTATION_LANDSCAPE)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }



    @Override
    public void allowOrientationChanges(){
        //enable orientation changes
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }



    //will set up the Adapter on result fragment's list view upon download end
    @Override
    public void setResultFragment(RecyclerView.Adapter adapterToUpload) {

        if(adapterToUpload != null){

            ResultFragment result = (ResultFragment) getSupportFragmentManager().findFragmentByTag(RESULT_FRAGMENT_TAG);

            if(result == null)
                result = (ResultFragment) findOrCreateNewFragment(RESULT_FRAGMENT_TAG);

            //store the tag and the adapter of the fragment to leave foreground
            storeToBackStack(adapterToUpload);

            //add the new result fragment to place
            addFragment(result);
            result.setResultAdapter(adapterToUpload);

            //play results in sound
            if(isSoundsOn())
                getSoundFragment().playSound("sounds/results_in.mp3");


        }
        else
            Toast.makeText(this, "No results for this selection", Toast.LENGTH_SHORT).show();

    }



    @Override
    public void addFragment(Fragment toAdd){

        if(!toAdd.isAdded()){

            String tag = toAdd.getArguments().getString("TAG");

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            if(toAdd.getClass() == ResultFragment.class){

                if(!inSmallScreenMode()) {

                    ft.replace(R.id.resultFragmentPlace, toAdd, tag);
                    inRESnow = (ResultFragment) toAdd;
                    inResPlaceTag = tag;
                }
                else {

                    ft.replace(R.id.uiFragmentPlace, toAdd, tag);
                    inUInow = toAdd;
                    inUiPlaceTag = tag;
                }

            }
            else{

                ft.replace(R.id.uiFragmentPlace, toAdd, tag);
                inUInow = toAdd;
                inUiPlaceTag = tag;
            }

            ft.commit();
            getSupportFragmentManager().executePendingTransactions();

        }
    }




    @Override
    public Fragment findOrCreateNewFragment(String tag) {

        Fragment result = getSupportFragmentManager().findFragmentByTag(tag);

        if (result == null){

            Bundle args = new Bundle();
            args.putString("TAG", tag);

            switch (tag){
                case DRIVER_FRAGMENT_TAG:
                    result = new DriversFragment();
                    break;

                case CONSTRUCTOR_FRAGMENT_TAG:
                    result = new ConstructorsFragment();
                    break;

                case CIRCUIT_FRAGMENT_TAG:
                    result = new CircuitFragment();
                    break;

                default:
                    result = new ResultFragment();   //qualifying fragment
                    break;
            }

            result.setArguments(args);
        }

        return result;
    }


    /**
     *  If a new Result Fragment is created, store it's adapter to my backstack
     */
    private void storeToBackStack(RecyclerView.Adapter adapterToUpload){

        RecyclerView.Adapter adapterToStore = null;
        String fragTag = "";

        if(inSmallScreenMode()){

            if( (inUInow != null) && (inUInow.getClass() == ResultFragment.class)) {
                adapterToStore = ((ResultFragment) inUInow).getAdapter();
            }
            else if(inUiPlaceTag != null) {
                fragTag = inUiPlaceTag;
            }
        }
        else {

            if(inRESnow != null) {
                adapterToStore = inRESnow.getAdapter();
            }
        }

        if(adapterToStore != null && shouldStore(adapterToStore, adapterToUpload))
            myBackStack.add(adapterToStore);
        else if (!fragTag.equalsIgnoreCase("")) {
            SuperFragment toStore = (SuperFragment) getSupportFragmentManager().findFragmentByTag(fragTag);
            lastUIEntry = toStore.getLastEntry();
            myBackStack.add(fragTag);
        }
    }



    /**
     * If input contains any slashes, get rid of it along with what follows it
     * @param input
     * @return the key without the slash if any contained ( example drivers/1 --> drivers )
     */
    private String deSlash(String input){

        if(input.length() == 0 || input.charAt(0) == '/')
            return "";
        else
            return input.charAt(0) + deSlash(input.substring(1));

    }


    /**
     * Check whether to store the old adapter or not
     * @param toStore adapter to store or not
     * @param toUpload adapter to upload
     * @return True if going from sites to news OR from calendar to result or qualifying. False otherwise
     */
    private boolean shouldStore(RecyclerView.Adapter toStore, RecyclerView.Adapter toUpload){

        return ( (toStore.getClass() == NewsSitesAdapter.class && toUpload.getClass() == NewsAdapter.class)
                        || ( (toStore.getClass() == CalendarRaceAdapter.class) && ( (toUpload.getClass() == QualifyingResultsAdapter.class)
                                                                                        || (toUpload.getClass() == UIResultAdapter.class) ))) ?
                true : false;

    }



    @Override
    public boolean getFromPreferences(String key, boolean defaultValue){
        SharedPreferences preferences = getSharedPreferences(MainActivity.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);

        return preferences.getBoolean(key, defaultValue);
    }

    @Override
    public void writeToPreferences(String key, boolean value){

        SharedPreferences preferences = getSharedPreferences(MainActivity.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(key, value);
        editor.apply();

    }

    @Override
    public ArrayList<Object> getAppBackstack() {
        return this.myBackStack;
    }

}//MainActivity
