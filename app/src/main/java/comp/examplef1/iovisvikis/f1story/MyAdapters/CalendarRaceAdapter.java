package comp.examplef1.iovisvikis.f1story.MyAdapters;

import android.content.Intent;
import android.database.sqlite.SQLiteDoneException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import comp.examplef1.iovisvikis.f1story.AsyncTasks.AreResultsUploaded;
import comp.examplef1.iovisvikis.f1story.AsyncTasks.DownloadFragment;
import comp.examplef1.iovisvikis.f1story.MainActivity;
import comp.examplef1.iovisvikis.f1story.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

/**
 * Created by iovisvikis on 14/4/2017.
 */

public class CalendarRaceAdapter extends RecyclerView.Adapter<CalendarRaceAdapter.CalendarViewHolder>{

    private ArrayList<CalendarRace> races;
    private DownloadFragment host;

    private SimpleDateFormat parser;
    private Date todayGMT;

    private ViewGroup parent;


    @Override
    public CalendarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        this.parent = parent;

        LayoutInflater inflater = host.getActivity().getLayoutInflater();
        
        View calendarHolderView = inflater.inflate(R.layout.race_calendar_row, parent, false);
        
        return new CalendarViewHolder(calendarHolderView);
    }



    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    @Override
    public void onBindViewHolder(CalendarViewHolder holder, int position) {

        LayoutInflater inflater = host.getActivity().getLayoutInflater();
        final CalendarRace race = races.get(position);

        FrameLayout infoButton = holder.getInfoButton();
        final String uri = race.getCircuitUrl();
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent infoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                host.startActivity(infoIntent);
            }
        });

        LinearLayout dateTimeLayout = holder.getDateTimeLayout();

        String raceDateTimeTxt = race.getDate() + " " + race.getTime().substring(0, race.getTime().length() - 1);  //15:00:00Z --> 15:00:00

        try {

            Date raceDate = parser.parse(raceDateTimeTxt);

            long difference = todayGMT.getTime() - raceDate.getTime();

            double twoHours = (2 * 60 * 60 * 1000); //will be used to determine if race is over
            long twentyFourHours = (24 * 60 * 60 * 1000); //will use it to determine qualifying day
            long twentyThreeHours = (23 * 60 * 60 * 1000); // will use it to determine if qualufying is over

            String circuitId = race.getCircuitId();

            //query to check if race results are available
            final String qualifyingQuery = MainActivity.BASIC_URI + "current/circuits/" + circuitId + "/qualifying.json";
            String[] qualifyingParams = {qualifyingQuery};

            //query to check if qualifying results are available
            final String resultsQuery = MainActivity.BASIC_URI + "current/circuits/" + circuitId + "/results.json";
            String[] resultsParams = {resultsQuery};

            if(difference < - twentyFourHours){//race more than an day into the future

                AppCompatTextView dateTxt = holder.getRaceDateTxt();
                dateTxt.setText(race.getDate());

                AppCompatTextView timeTxt = holder.getRaceTimeTxt();
                timeTxt.setText(race.getTime());

            }
            else{ // race due in a day

                try {

                    //TODO make a query to the ergast api and find out whether the results or the q3s are available. If so, set a click listener
                    //or add an under construction png and a toast message

                    FrameLayout qualifyingFrame = holder.getQualifyingOnPlace();

                    final FrameLayout qualifyingButton, resultsButton;

                    if(difference > -twentyFourHours && difference < -twentyThreeHours){ //qualifying is on

                        Drawable inProgress = ContextCompat.getDrawable(host.getActivity(), R.drawable.qual_on);
                        qualifyingButton = setButton(inProgress);
                        qualifyingButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(host.getActivity(), host.getResources().getString(R.string.qualifying_in_progress), Toast.LENGTH_SHORT).show();
                            }
                        });
                        qualifyingFrame.addView(qualifyingButton);


                        AppCompatTextView dateTxt = holder.getRaceDateTxt();
                        dateTxt.setText(race.getDate());

                        AppCompatTextView timeTxt = holder.getRaceTimeTxt();
                        timeTxt.setText(race.getTime());

                    }
                    else if (difference > -twentyThreeHours && difference < 0){ // qualifying over, search for qualifying results
                        //check if qualifying results are uploaded
                        boolean qualifyingResultsOn = new AreResultsUploaded().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, qualifyingParams).get();

                        if(qualifyingResultsOn){

                            Drawable qualsOn = ContextCompat.getDrawable(host.getActivity(), R.drawable.notify_qual);
                            qualifyingButton = setButton(qualsOn);
                            qualifyingButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view){
                                    Object[] params = {qualifyingQuery, "QualifyingResults", host, host.getResources().getString(R.string.getting_qualifying_results)};
                                    host.startListAdapterTask(params);
                                }
                            });

                        }
                        else {//not available yet, give an under construction png
                            Drawable qualUnderConstruction = ContextCompat.getDrawable(host.getActivity(), R.drawable.on_way);
                            qualifyingButton = setButton(qualUnderConstruction);
                            qualifyingButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(host.getActivity(), host.getResources().getString(R.string.qualifying_results_nont_ready_yet), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        qualifyingFrame.addView(qualifyingButton);

                        AppCompatTextView dateTxt = holder.getRaceDateTxt();
                        dateTxt.setText(race.getDate());

                        AppCompatTextView timeTxt = (AppCompatTextView) holder.getRaceTimeTxt();
                        timeTxt.setText(race.getTime());


                    }
                    else if (difference >= 0 && difference < twoHours) { //race is on right now, qualifying results should be on by now

                        dateTimeLayout.removeAllViews();

                        View qualifyings_results = inflater.inflate(R.layout.qualifyings_results, parent, false);

                        qualifyingButton = (FrameLayout) qualifyings_results.findViewById(R.id.qualifyingButton);
                        qualifyingButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Object[] params = {qualifyingQuery, "QualifyingResults", host, host.getResources().getString(R.string.getting_qualifying_results)};
                                host.startListAdapterTask(params);
                            }
                        });

                        Drawable resultsInProgress = ContextCompat.getDrawable(host.getActivity(), R.drawable.mic_race_on);
                        resultsButton = qualifyings_results.findViewById(R.id.resultsButton);
                        resultsButton.setBackground(resultsInProgress);
                        resultsButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(host.getActivity(), host.getResources().getString(R.string.race_is_on_right_now), Toast.LENGTH_SHORT).show();
                            }
                        });

                        dateTimeLayout.addView(qualifyings_results);

                    }
                    else if (difference > twoHours && difference < twentyFourHours){ //difference is more than two hours, check whether the results are on

                        dateTimeLayout.removeAllViews();

                        View qualifyings_results = inflater.inflate(R.layout.qualifyings_results, parent, false);

                        qualifyingButton = qualifyings_results.findViewById(R.id.qualifyingButton);
                        qualifyingButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Object[] params = {qualifyingQuery, "QualifyingResults", host, host.getResources().getString(R.string.getting_qualifying_results)};
                                host.startListAdapterTask(params);
                            }
                        });

                        boolean resultsUploaded = new AreResultsUploaded().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, resultsParams).get();

                        resultsButton = qualifyings_results.findViewById(R.id.resultsButton);

                        if(resultsUploaded){
                            resultsButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Object[] params = {resultsQuery, "Results", host, host.getResources().getString(R.string.getting_race_results)};
                                    host.startListAdapterTask(params);
                                }
                            });
                        }
                        else{
                            Drawable resultsUnderConstruction = ContextCompat.getDrawable(host.getActivity(), R.drawable.on_way);
                            resultsButton.setBackground(resultsUnderConstruction);
                            resultsButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(host.getActivity(), host.getResources().getString(R.string.race_results_under_construction), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        dateTimeLayout.addView(qualifyings_results);
                    }
                    else {//more than a day after the race, results should be uploaded by now
                        dateTimeLayout.removeAllViews();

                        View qualifyings_results = inflater.inflate(R.layout.qualifyings_results, parent, false);

                        qualifyingButton = qualifyings_results.findViewById(R.id.qualifyingButton);
                        qualifyingButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Object[] params = {qualifyingQuery, "QualifyingResults", host, host.getResources().getString(R.string.getting_qualifying_results)};
                                host.startListAdapterTask(params);
                            }
                        });

                        resultsButton =  qualifyings_results.findViewById(R.id.resultsButton);
                        resultsButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Object[] params = {resultsQuery, "Results", host, host.getResources().getString(R.string.getting_race_results)};
                                host.startListAdapterTask(params);
                            }
                        });

                        dateTimeLayout.addView(qualifyings_results);
                    }

                }
                catch (InterruptedException ie){
                    Log.e("CLNDRADAPTER", ie.getMessage());
                }
                catch (ExecutionException ee){
                    Log.e("CLNDRADAPTER", ee.getMessage());
                }

            }

        }
        catch (ParseException pe){
            Log.e("CLNDRADPTR", pe.getMessage());
        }
        catch (SQLiteDoneException de){
            Log.e("CLNDRADPTR", " " + de);
        }


        ImageView flagFrame = holder.getCountryFlag();

        host.uploadCountryFlag(flagFrame, race.getCountryName());

        AppCompatTextView circuitTxt = (AppCompatTextView) holder.getCircuitNameTxt();
        circuitTxt.setText(race.getCircuitName());

        AppCompatTextView localityTxt = (AppCompatTextView) holder.getLocalityTxt();
        localityTxt.setText(race.getLocality());

    }


    @Override
    public int getItemCount() {
        return races.size();
    }



    private FrameLayout setButton(Drawable background){
        FrameLayout button = new FrameLayout(host.getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        button.setLayoutParams(params);
        button.setBackground(background);

        return button;
    }
    
    
    

    public CalendarRaceAdapter(DownloadFragment host, JSONArray results){
        //Get current GMT time to compare race dates with
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formater.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = new Date();
        this.parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            this.todayGMT = parser.parse(formater.format(date));
        }
        catch (ParseException pe){
            Log.e("CALRACEADPTR", pe.getMessage());
        }

        //initialize class variables
        this.races = new ArrayList<>();
        this.host = host;


        for(int index=0; index<results.length(); index++) {
            try {
                JSONObject calendarRace = results.getJSONObject(index);

                CalendarRace race = new CalendarRace(calendarRace);
                races.add(race);
            }
            catch (JSONException e){
                Log.e("CALENDARACEADAPT", e.getMessage());
            }
        }

    }



    class CalendarViewHolder extends RecyclerView.ViewHolder{
        public LinearLayout getCalendarRowRoot() {
            return calendarRowRoot;
        }

        public LinearLayout getDateTimeLayout() {
            return dateTimeLayout;
        }

        public FrameLayout getQualifyingOnPlace() {
            return qualifyingOnPlace;
        }

        public FrameLayout getInfoButton() {
            return infoButton;
        }

        public AppCompatTextView getRaceDateTxt() {
            return raceDateTxt;
        }

        public AppCompatTextView getRaceTimeTxt() {
            return raceTimeTxt;
        }

        public AppCompatTextView getLocalityTxt() {
            return localityTxt;
        }

        public AppCompatTextView getCircuitNameTxt() {
            return circuitNameTxt;
        }

        public ImageView getCountryFlag() {
            return countryFlag;
        }

        private LinearLayout calendarRowRoot, dateTimeLayout;
        private FrameLayout qualifyingOnPlace, infoButton;
        private AppCompatTextView raceDateTxt, raceTimeTxt, localityTxt, circuitNameTxt;
        private ImageView countryFlag;

        public CalendarViewHolder(View itemView) {
            super(itemView);

            this.calendarRowRoot = itemView.findViewById(R.id.calendarRowRoot);
            this.dateTimeLayout = itemView.findViewById(R.id.dateTimeLayout);
            this.qualifyingOnPlace = itemView.findViewById(R.id.qualifyingOnPlace);
            this.infoButton = itemView.findViewById(R.id.infoButton);
            this.raceDateTxt = itemView.findViewById(R.id.race_dateTxt);
            this.raceTimeTxt = itemView.findViewById(R.id.race_timeTxt);
            this.countryFlag = itemView.findViewById(R.id.countryFlag);
            this.localityTxt = itemView.findViewById(R.id.localityTxt);
            this.circuitNameTxt = itemView.findViewById(R.id.circuitNameTxt);
        }
    }


}//CalendarRaceAdapter


