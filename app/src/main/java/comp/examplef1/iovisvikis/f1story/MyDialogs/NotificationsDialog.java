
package comp.examplef1.iovisvikis.f1story.MyDialogs;

import android.app.AlarmManager;
import android.app.Dialog;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;

import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatTextView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import comp.examplef1.iovisvikis.f1story.APICommunicator;
import comp.examplef1.iovisvikis.f1story.Communication;
import comp.examplef1.iovisvikis.f1story.MainActivity;
import comp.examplef1.iovisvikis.f1story.MyAdapters.CalendarRace;
import comp.examplef1.iovisvikis.f1story.NotificationBroadcast;
import comp.examplef1.iovisvikis.f1story.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;


//Created by iovisvikis on 27/5/2017.



public class NotificationsDialog extends DialogFragment {


    //turn to millisecs
    private final long FIVE_MINUTES = 5*60*1000;
    private final long TEN_MINUTES = 10*60*1000;
    private final long FIFTEEN_MINUTES = 15*60*1000;
    private final long THIRTY_MINUTES = 30*60*1000;
    private final long ONE_HOUR = 60*60*1000;
    private final long TWO_HOURS = 2*60*60*1000;
    private final long TWENTY_FOUR_HOURS = 24*60*60*1000;


    private Date todayGMT, nextEventDateGMT;
    private SimpleDateFormat parser;

    private String circuitName;

    //This is where the options for each selection will be put
    private LinearLayout raceOptions, qualifyingOptions;
    private AppCompatCheckBox raceBox, qualifyingBox;

    private AppCompatTextView circuitInfoTxt;

    private Communication act;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        act = (Communication) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        act.blockOrientationChanges();

        setCancelable(false);

    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.notify_layout, container, false);

        parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //Formater to get the GMT
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formater.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = new Date();

        try {
            //set today's GMT time
            todayGMT = parser.parse(formater.format(date));
        }
        catch (ParseException pe){
            Log.e("NoTiFiCaTionDiAlog", pe.getMessage());
        }


        circuitInfoTxt = root.findViewById(R.id.circuitInfoText);
        circuitInfoTxt.setText(circuitName);


        qualifyingBox = root.findViewById(R.id.qualifyingBox);
        qualifyingBox.setTag("qualBox");
        raceBox = root.findViewById(R.id.raceBox);
        raceBox.setTag("raceBox");

        circuitInfoTxt = root.findViewById(R.id.circuitInfoText);

        final LinearLayout minutesContainer = (LinearLayout) root.findViewById(R.id.minuteBoxesContainer);
        minutesContainer.removeAllViews();
        //start the options building tasks here
        GetNextDateTask dateTask = new GetNextDateTask();
        dateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{minutesContainer});

        qualifyingBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if(qualifyingBox.isChecked()){
                    raceBox.setChecked(false);
                    minutesContainer.removeAllViews();
                    minutesContainer.addView(qualifyingOptions);

                }
                else{
                    raceBox.setChecked(true);
                    minutesContainer.removeAllViews();
                    minutesContainer.addView(raceOptions);
                }

            }
        });

        raceBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

              if(raceBox.isChecked()){
                  qualifyingBox.setChecked(false);
                  minutesContainer.removeAllViews();
                  minutesContainer.addView(raceOptions);
              }
              else {

                  if(qualifyingBox.isEnabled()){
                      qualifyingBox.setChecked(true);
                      minutesContainer.removeAllViews();
                      minutesContainer.addView(qualifyingOptions);
                  }
                  else {
                      raceBox.setChecked(true);
                      minutesContainer.removeAllViews();
                      minutesContainer.addView(raceOptions);
                  }

              }

            }
        });


        FrameLayout xLayout = (FrameLayout) root.findViewById(R.id.cancelButton);
        xLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((Communication) getActivity()).allowOrientationChanges();
                NotificationsDialog.this.dismiss();
            }
        });


        FrameLayout checkLayout = (FrameLayout) root.findViewById(R.id.checkButton);
        checkLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //cancel all previously set notifications
                act.cancelAllNotifications();

                //set new notifications
                if(qualifyingBox.isEnabled()){

                    long timeToQualifying = nextEventDateGMT.getTime() - todayGMT.getTime() - TWENTY_FOUR_HOURS;

                    for(int index=0; index<qualifyingOptions.getChildCount(); index++){

                        LinearLayout checkContainer = (LinearLayout) qualifyingOptions.getChildAt(index);

                        AppCompatCheckBox checkBox = (AppCompatCheckBox) checkContainer.getChildAt(0);

                        if(checkBox.isChecked()) {
                            String label = checkBox.getText().toString();
                            setAlarm(timeToQualifying, label, "qual");
                        }

                    }

                }

                if(raceBox.isEnabled()){

                    long timeToRace = nextEventDateGMT.getTime() - todayGMT.getTime();

                    for(int index=0; index<raceOptions.getChildCount(); index++){

                        LinearLayout checkContainer = (LinearLayout) raceOptions.getChildAt(index);

                        AppCompatCheckBox checkBox = (AppCompatCheckBox) checkContainer.getChildAt(0);

                        if(checkBox.isChecked()) {
                            String label = checkBox.getText().toString();
                            setAlarm(timeToRace, label, "race");
                        }

                    }

                }

                act.allowOrientationChanges();
                act.setNotificationsOn(true);

                //Let's see what is stored inside

                NotificationsDialog.this.dismiss();
            }
        });


        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return root;
    }


    @Override
    public void onStart() {
        super.onStart();

        int width, height;

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            height = (int) (getResources().getDisplayMetrics().heightPixels * 0.70);
        }
        else{
            width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5);
            height = (int) (getResources().getDisplayMetrics().heightPixels * 0.90) ;
        }

        getDialog().getWindow().setLayout(width, height);

    }



    @Override
    public void onDestroyView() {

        Dialog dialog = getDialog();

        if(dialog!=null && getRetainInstance()){
            dialog.setDismissMessage(null);
        }

        super.onDestroyView();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        act.allowOrientationChanges();

        act = null;
    }


    private void setAlarm(long timeToEvent, String label, String tag){

        long millis;
        int notificationCodeSecond;  //will be used to provide notification ID

        switch (parseNameTag(label)){

            case "5":
                millis = FIVE_MINUTES;
                notificationCodeSecond = 5;
                break;

            case "10":
                millis = TEN_MINUTES;
                notificationCodeSecond = 10;
                break;

            case "15":
                millis = FIFTEEN_MINUTES;
                notificationCodeSecond = 15;
                break;

            case "30":
                millis = THIRTY_MINUTES;
                notificationCodeSecond = 30;
                break;

            case "1":
                millis = ONE_HOUR;
                notificationCodeSecond = 1;
                break;

            default:
                millis = TWO_HOURS;
                notificationCodeSecond = 2;

        }

        String eventType;
        int notificationCodeFirst;

        switch (tag){
            case "qual":
                eventType = getActivity().getResources().getString(R.string.qualifying);
                notificationCodeFirst = 100;
                break;

            default:
                eventType = getActivity().getResources().getString(R.string.race);
                notificationCodeFirst = 200;
        }



        AlarmManager alaMan = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        Intent notifyIntent = new Intent(getActivity(), NotificationBroadcast.class);
        notifyIntent.putExtra("KIND", eventType);
        notifyIntent.putExtra("CIRCUIT", parseCircuitName(circuitName));
        notifyIntent.putExtra("TIME_OFF", label);


        //uncomment for testing
        //long time = System.currentTimeMillis() + millis;

        long timeToAlarm = System.currentTimeMillis() + timeToEvent - millis;

        //for example notification code for qualifying and five minutes will be 100 + 5 = 105
        // notification code for race and half hour notice will be 200 + 30 = 230 and so on, so forth
        int requestCode = notificationCodeFirst + notificationCodeSecond;

        notifyIntent.putExtra("ID", requestCode);

        //update the notifications table in the database. Stored for future cancelations

        try{

            String query = "insert into notifications_table (notif_id, event, time_to_event) values (" +
                                                                requestCode + ", '" + eventType + "', '" + label + "');";

            act.getAppDatabase().execSQL(query);

        }
        catch (SQLiteException sql){
            Log.e("NotifcDlg/SetAlarm", sql.getMessage());
        }


        alaMan.set(AlarmManager.RTC_WAKEUP, /* time */ timeToAlarm , PendingIntent.getBroadcast(getActivity(),
                                                        requestCode, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT));

    }





    private String parseNameTag(String nameTag){

        if(nameTag.length() == 0)
            return "";
        else if(Character.isDigit(nameTag.charAt(0)))
            return nameTag.charAt(0) + parseNameTag(nameTag.substring(1));
        else
            return "";

    }


    private String parseCircuitName(String circuitName){

        if(Character.isDigit(circuitName.charAt(0)) || circuitName.length() == 0)
            return "";
        else
            return circuitName.charAt(0) + parseCircuitName(circuitName.substring(1));

    }



    private class GetNextDateTask extends AsyncTask<Object, Void, Date> {

        private LinearLayout UiOptionsContainer;

        @Override
        protected Date doInBackground(Object... objects) {

            UiOptionsContainer = (LinearLayout) objects[0];

            return findNextEvent();
        }


        @Override
        protected void onPostExecute(Date date){

            if(date == null){
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.season_over)
                                                                                    , Toast.LENGTH_SHORT).show();
                NotificationsDialog.this.dismiss();
            }
            else{
                circuitInfoTxt.setText(circuitName);
                nextEventDateGMT = date;

                //qualifying takes place the previous day same hour
                long timeToQualifying = date.getTime() - todayGMT.getTime() - TWENTY_FOUR_HOURS;
                long timeToRace = date.getTime() - todayGMT.getTime();


                GiveMeTheOptions qualOptions = new GiveMeTheOptions();
                Object[] qualParams = new Object[]{timeToQualifying, qualifyingBox};
                qualOptions.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, qualParams);

                GiveMeTheOptions raceOptions = new GiveMeTheOptions();
                Object[] raceParams = new Object[]{timeToRace, raceBox};
                raceOptions.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, raceParams);
            }
        }




    /*
     * Finds date of the next event
     * @return the date object associated with the race event
     */

        private Date findNextEvent(){

            Date result = null;

            APICommunicator api = new APICommunicator();

            try {

                String query = MainActivity.BASIC_URI + "current/races.json";

                final String finalInfo = api.getInfo(query);

                JSONObject root = new JSONObject(finalInfo);

                JSONArray races = api.getData(root, "Races");

                Date raceDay ;

                for(int index=0; index < races.length(); index++){

                    JSONObject race = races.getJSONObject(index);
                    CalendarRace calendarRace = new CalendarRace(race);

                    raceDay = parser.parse(calendarRace.getDate() + " " + calendarRace.getTime().substring(0,
                                                                                calendarRace.getTime().length()-2));
                                                                                        //get rid of the Z at the end
                    long difference = todayGMT.getTime() - raceDay.getTime();

                    if(difference < 0){
                        result = raceDay;
                        circuitName = calendarRace.getCircuitName() + " " + calendarRace.getDate() + " " +
                                                                                calendarRace.getTime();

                        break;    //found the first race after today. Stop further search
                    }
                }
            }
            catch (JSONException je){
                Log.e("NoTiFiCaTioNSDiaLoG", je.getMessage());
            }
            catch (ParseException pe){
                Log.e("NoTiFiCaTioNSDiaLoG", pe.getMessage());

            }

            return result;
        }


        /**
         *  Compares time left until event and loads available options for notifications to the user
         *  Disables options if no time left
         */
        private class GiveMeTheOptions extends AsyncTask<Object, Void, LinearLayout>{

            private AppCompatCheckBox optionsActivator;

            private long timeToEvent;

            @Override
            protected LinearLayout doInBackground(Object... objects) {

                timeToEvent = (long) objects[0];
                optionsActivator = (AppCompatCheckBox) objects[1];

                if(timeToEvent > FIVE_MINUTES) { //still time for the event (especially useful when building qualifying options

                    LinearLayout innerContainer = new LinearLayout(getActivity());

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                        ViewGroup.LayoutParams.WRAP_CONTENT);
                    innerContainer.setOrientation(LinearLayout.VERTICAL);

                    innerContainer.setLayoutParams(params);

                    LayoutInflater inflater = getActivity().getLayoutInflater();


                    if(timeToEvent > FIVE_MINUTES){
                        View selectionRow = inflater.inflate(R.layout.notify_checkbox, null);
                        AppCompatCheckBox selectionCheck = (AppCompatCheckBox) selectionRow.findViewById(R.id.whitecheckbox);

                        selectionCheck.setText(getActivity().getResources().getString(R.string.five_minutes));

                        innerContainer.addView(selectionRow);
                    }


                    if(timeToEvent > TEN_MINUTES){
                        View selectionRow = inflater.inflate(R.layout.notify_checkbox, null);
                        AppCompatCheckBox selectionCheck = (AppCompatCheckBox) selectionRow.findViewById(R.id.whitecheckbox);

                        selectionCheck.setText(getActivity().getResources().getString(R.string.ten_minutes));

                        innerContainer.addView(selectionRow);
                    }


                    if(timeToEvent > FIFTEEN_MINUTES){
                        View selectionRow = inflater.inflate(R.layout.notify_checkbox, null);
                        AppCompatCheckBox selectionCheck = (AppCompatCheckBox) selectionRow.findViewById(R.id.whitecheckbox);

                        selectionCheck.setText(getActivity().getResources().getString(R.string.fifteen_minutes));

                        innerContainer.addView(selectionRow);
                    }


                    if(timeToEvent > THIRTY_MINUTES){
                        View selectionRow = inflater.inflate(R.layout.notify_checkbox, null);
                        AppCompatCheckBox selectionCheck = (AppCompatCheckBox) selectionRow.findViewById(R.id.whitecheckbox);

                        selectionCheck.setText(getActivity().getResources().getString(R.string.thirty_minutes));

                        innerContainer.addView(selectionRow);
                    }


                    if(timeToEvent > ONE_HOUR){
                        View selectionRow = inflater.inflate(R.layout.notify_checkbox, null);
                        AppCompatCheckBox selectionCheck = (AppCompatCheckBox) selectionRow.findViewById(R.id.whitecheckbox);

                        selectionCheck.setText(getActivity().getResources().getString(R.string.one_hour));

                        innerContainer.addView(selectionRow);
                    }


                    if(timeToEvent > TWO_HOURS){
                        View selectionRow = inflater.inflate(R.layout.notify_checkbox, null);
                        AppCompatCheckBox selectionCheck = (AppCompatCheckBox) selectionRow.findViewById(R.id.whitecheckbox);

                        selectionCheck.setText(getActivity().getResources().getString(R.string.two_hours));

                        innerContainer.addView(selectionRow);
                    }


                    return innerContainer;
                }

                return null; //returned when there is no time for notifications event starts
            }



            @Override
            protected void onPostExecute(LinearLayout parent) {

                String checkTag = (String) optionsActivator.getTag();

                if (parent != null) {

                    if(checkTag.equalsIgnoreCase("raceBox"))
                        raceOptions = parent;
                    else
                        qualifyingOptions = parent;

                    optionsActivator.setEnabled(true);   //make the options available
                    optionsActivator.setChecked(true);

                    UiOptionsContainer.removeAllViews();
                    UiOptionsContainer.addView(parent);  //put them in for the user to see
                }
                else {
                    optionsActivator.setEnabled(false); //no options available

                    if(timeToEvent > 0) {
                        if (checkTag.equalsIgnoreCase("raceBox"))
                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.race_about_start)
                                                                , Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.qual_about_start),
                                                                                        Toast.LENGTH_SHORT).show();

                    }
                }

            }



        }



    }





}//NotificationsDialog




