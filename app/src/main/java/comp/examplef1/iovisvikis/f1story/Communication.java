
package comp.examplef1.iovisvikis.f1story;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import comp.examplef1.iovisvikis.f1story.AsyncTasks.DownloadFragment;

import java.util.ArrayList;
import java.util.HashMap;


//the callback interface - the fragments communicate with each other through the activity - decoupling
public interface Communication{

    //will be updating the searchedForUpdates variable
    void setSearchedForUpdates(boolean searhedForUpdates);

    DownloadFragment getDownloadFragment();

    HashMap<String, ArrayList<String>> getDriversEras();
    HashMap<String, ArrayList<String>> getConstructorsEras();
    HashMap<String, ArrayList<String>> getCircuitsEras();

    ArrayList<String> getAllSeasons();
    ArrayList<String> getAllDrivers();
    ArrayList<String> getAllConstructors();
    ArrayList<String> getAllCircuits();

    HashMap<String, String[]> getDriversIDsURLS();
    HashMap<String, String[]> getConstructorsIDsURLs();
    HashMap<String, String[]> getCircuitsIDsURLs();
    HashMap<String, String[]> getSeasonURLs();

    SQLiteDatabase getAppDatabase();
    SoundFragment getSoundFragment();

    //will be changing the adapter in the result list view
    void setResultFragment(RecyclerView.Adapter adapter);

    boolean hasInternetConnection();

    boolean apiResponds();

    void onDialogPositiveClick(String userInput, String key);

    android.support.v4.app.Fragment findOrCreateNewFragment(String tag);
    void addFragment(android.support.v4.app.Fragment toAdd);

    void launchSingleSelectionDialog(Bundle args);
    void launchMultipleSelectionDialog(Bundle args);

    boolean inSmallScreenMode();

    void blockOrientationChanges();
    void allowOrientationChanges();

    void setNotificationsOn(boolean onOrOff);
    void cancelAllNotifications();

    boolean isSoundsOn();

    boolean getFromPreferences(String key, boolean defaultValue);
    void writeToPreferences(String key, boolean value);

    ArrayList<Object> getAppBackstack();

}//Communication


