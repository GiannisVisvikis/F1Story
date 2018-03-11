package comp.examplef1.iovisvikis.f1story;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import comp.examplef1.iovisvikis.f1story.MyAdapters.NewsSitesAdapter;

import comp.examplef1.iovisvikis.f1story.R;
import comp.examplef1.iovisvikis.f1story.quiz.QuizActivity;


/**
 * Created by ioannisvisvikis on 8/31/17.
 */

public class NavigationScrollViewFragment extends Fragment{

    private Communication act;
    private View drawerView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CardView quizCard, driversCard, constructorsCard, circuitsCard, resultsCard, calendarCard, gridCard, newsCard;
    private boolean wasDrawerOpen = true;
    private boolean userSeenDrawer = false;

    private static final String WAS_DRAWER_OPEN = "DRAWER_WAS_OPEN";
    private static final String USER_SEEN_DRAWER_KEY = "DRAWER_SEEN";


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        act = (Communication) getActivity();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null)
            wasDrawerOpen = savedInstanceState.getBoolean(WAS_DRAWER_OPEN);

    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(WAS_DRAWER_OPEN, wasDrawerOpen);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){

        View root = inflater.inflate(R.layout.navigation_drawer_scrollview, container, false);

        quizCard = root.findViewById(R.id.quiz_Card);
        driversCard = root.findViewById(R.id.driversCard);
        constructorsCard = root.findViewById(R.id.constructorsCard);
        circuitsCard = root.findViewById(R.id.circuitsCard);
        resultsCard = root.findViewById(R.id.resultsCard);
        calendarCard = root.findViewById(R.id.calendarCard);
        gridCard = root.findViewById(R.id.gridCard);
        newsCard = root.findViewById(R.id.newsCard);

        return root;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        quizCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent quizIntent = new Intent(getContext(), QuizActivity.class);
                startActivity(quizIntent);
            }
        });

        driversCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SuperFragment userChoice = (DriversFragment) act.findOrCreateNewFragment(MainActivity.DRIVER_FRAGMENT_TAG);
                act.getSoundFragment().playRandomSound();

                if(!userChoice.isAdded())
                    act.addFragment(userChoice);

                act.getAppBackstack().clear();

                drawerLayout.closeDrawer(drawerView);
            }
        });

        constructorsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SuperFragment userChoice = (ConstructorsFragment) act.findOrCreateNewFragment(MainActivity.CONSTRUCTOR_FRAGMENT_TAG);
                act.getSoundFragment().playRandomSound();

                if(!userChoice.isAdded())
                    act.addFragment(userChoice);

                act.getAppBackstack().clear();

                drawerLayout.closeDrawer(drawerView);
            }
        });

        circuitsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SuperFragment userChoice = (CircuitFragment) act.findOrCreateNewFragment(MainActivity.CIRCUIT_FRAGMENT_TAG);
                act.getSoundFragment().playRandomSound();

                if(!userChoice.isAdded())
                    act.addFragment(userChoice);

                act.getAppBackstack().clear();

                drawerLayout.closeDrawer(drawerView);
            }
        });

        resultsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putString("NAME", "");
                args.putString("FRAGMENT_KIND", "ui");
                args.putString("PURPOSE", "Results");
                args.putBoolean("ALL_OPTIONS", false);
                drawerLayout.closeDrawer(drawerView);
                act.launchMultipleSelectionDialog(args);
            }
        });

        calendarCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = MainActivity.BASIC_URI + "current/races.json";
                Object[] params = {query, "Races", act.getDownloadFragment(), getResources().getString(R.string.getting_races)};
                act.getDownloadFragment().startListAdapterTask(params);
                drawerLayout.closeDrawer(drawerView);
            }
        });

        gridCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query2 = MainActivity.BASIC_URI + "current/constructors.json";
                Object[] params2 = {query2, "Constructors", act.getDownloadFragment(), getResources().getString(R.string.getting_season_grid),
                        "season grid"}; //5th arg needed for discrimination
                act.getDownloadFragment().startListAdapterTask(params2);
                drawerLayout.closeDrawer(drawerView);
            }
        });


        newsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewsSitesAdapter newsSitesAdapter = new NewsSitesAdapter( ((Communication) getActivity()).getDownloadFragment() );
                act.setResultFragment(newsSitesAdapter);
                drawerLayout.closeDrawer(drawerView);
            }
        });

    }



    @Override
    public void onDetach() {
        super.onDetach();

        act = null;
    }




    public void setTheDrawerFragment(View drawerView, final DrawerLayout drawerLayout, Toolbar toolbar){

        //retrieve from stored if any exist, return false otherwise
        userSeenDrawer = act.getFromPreferences(USER_SEEN_DRAWER_KEY, false);


        this.drawerView = drawerView;
        this.drawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open,
                                                                                            R.string.drawer_closed){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                wasDrawerOpen = true;

                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                if(!userSeenDrawer) {
                    userSeenDrawer = true;
                    act.writeToPreferences(USER_SEEN_DRAWER_KEY, userSeenDrawer);
                }

                wasDrawerOpen = false;

                getActivity().invalidateOptionsMenu();
            }
        };

        drawerLayout.addDrawerListener(mDrawerToggle);

        //decide whether to show the drawer or not during orientation changes
        if(!userSeenDrawer || wasDrawerOpen)
            drawerLayout.openDrawer(drawerView);
        else
            drawerLayout.closeDrawer(drawerView);


        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

    }



}
