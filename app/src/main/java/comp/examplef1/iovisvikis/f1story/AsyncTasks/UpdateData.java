package comp.examplef1.iovisvikis.f1story.AsyncTasks;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import comp.examplef1.iovisvikis.f1story.Communication;
import comp.examplef1.iovisvikis.f1story.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by iovisvikis on 24/10/2016.
 */

public class UpdateData extends AsyncTask<Object, Integer, Void> {

    //NO NEED TO CHECK FOR CONNECTION -- ALREADY CHECKED BEFORE CALLING TO EXECUTION

    @Override
    protected Void doInBackground(Object... objects) {

        MainActivity act = (MainActivity) objects[0];

        HashMap<String, ArrayList<String>> erasMap;
        HashMap<String, String[]> idsMap;

        ArrayList<String> allList;


        String idColumnName = (String) objects[1];
        String fullNameColumnName = (String) objects[2];

        SQLiteDatabase f1DataBase = act.getAppDatabase();

        String kind;

        if(fullNameColumnName.contains("DRIVER")){
            erasMap = act.getDriversEras();
            idsMap = act.getDriversIDsURLS();
            allList = act.getAllDrivers();
            kind = "_DRIVERS";
        }
        else if(fullNameColumnName.contains("CONSTRUCTOR")){
            erasMap = act.getConstructorsEras();
            idsMap = act.getConstructorsIDsURLs();
            allList = act.getAllConstructors();
            kind = "_CONSTRUCTORS";
        }
        else if(fullNameColumnName.contains("CIRCUIT")){
            erasMap = act.getCircuitsEras();
            idsMap = act.getCircuitsIDsURLs();
            allList = act.getAllCircuits();
            kind = "_CIRCUITS";
        }
        else{ //it is the seasons task
            kind = "_SEASONS";
            idsMap = act.getSeasonURLs();
            allList = act.getAllSeasons();
            erasMap = null;
        }


        if(erasMap != null) {

            for (String key : erasMap.keySet()) {

                String tableName = key + kind;
                String dropTableQuery = "DROP TABLE IF EXISTS '" + tableName +"';";
                //Log.d("UpdateData", dropTableQuery);
                f1DataBase.execSQL(dropTableQuery);

                String createTableQuery = "CREATE TABLE IF NOT EXISTS '" + tableName + "' (" + idColumnName + " VARCHAR PRIMARY KEY, " + fullNameColumnName + " VARCHAR NOT NULL);";
                //Log.d("UpdateData", createTableQuery);
                f1DataBase.execSQL(createTableQuery);

                ArrayList<String> fullNames = erasMap.get(key);

                for (int index = 0; index < fullNames.size(); index++) {

                    String fullName = fullNames.get(index);
                    String id = idsMap.get(fullName)[0];

                    if(fullName.contains("'"))
                        fullName = fullName.replace("'", "''");

                    String insertQuery = "INSERT INTO '" + tableName + "' (" + idColumnName + ", " + fullNameColumnName + ") VALUES ('" + id + "', '" + fullName + "');";
                    //Log.d("UpdateData", insertQuery);
                    f1DataBase.execSQL(insertQuery);
                }

            }
        }


        //CREATE ALL_XXXX TABLES
        String allTableName = "ALL" + kind;
        String dropAllTable = "DROP TABLE IF EXISTS '" + allTableName +"';";
        //Log.d("UpdateData", dropAllTable);
        f1DataBase.execSQL(dropAllTable);

        String  createAllTable;

        if(!allTableName.equalsIgnoreCase("ALL_SEASONS"))
            createAllTable = "CREATE TABLE IF NOT EXISTS '" + allTableName + "' (" + idColumnName + " VARCHAR PRIMARY KEY, " + fullNameColumnName + " VARCHAR NOT NULL, URL VARCHAR NOT NULL);";
        else
            createAllTable = "CREATE TABLE IF NOT EXISTS '" + allTableName + "' (" + fullNameColumnName + " VARCHAR NOT NULL, URL VARCHAR NOT NULL)";

        //Log.d("UpdateData", createAllTable);
        f1DataBase.execSQL(createAllTable);

        for(String name : allList){

            String allInsertQuery;

            String url = idsMap.get(name)[1];

            if(!allTableName.equalsIgnoreCase("ALL_SEASONS")) {

                String id = idsMap.get(name)[0];

                if(name.contains("'"))
                    name = name.replace("'", "''");

                allInsertQuery = "INSERT INTO '" + allTableName + "' (" + idColumnName + ", " + fullNameColumnName + ", URL) VALUES ('" + id + "', '" + name + "', '" + url + "');";
            }
            else {
                allInsertQuery = "INSERT INTO '" + allTableName + "' (" + fullNameColumnName +", URL) VALUES ('" + name + "', '" + url + "');";
            }

            //Log.d("UpdateData", allInsertQuery);
            f1DataBase.execSQL(allInsertQuery);
        }

        return null;
    }



}//UpdateData



