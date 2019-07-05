package com.redox.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.redox.localdb.models.Log;

/**
 * Created by Belal on 1/27/2017.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    //Constants for Database name, table name, and column names
    public static final String DB_NAME = "redoxer1";
    public static final String TABLE_NAME = "Log";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_START_TIME = "strt_time";
    public static final String COLUMN_R_SYS = "r_sys";
    public static final String COLUMN_R_DIA = "r_dia";
    public static final String COLUMN_R_PULSE = "r_pulse";
    public static final String COLUMN_L_SYS = "l_sys";
    public static final String COLUMN_L_DIA = "l_dia";
    public static final String COLUMN_L_PULSE = "l_pulse";
    public static final String COLUMN_STATUS = "status";

    //database version
    private static final int DB_VERSION = 1;

    //Constructor
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    private static DatabaseHelper appLocalData;
    public static DatabaseHelper getInstance(Context context){
        if (appLocalData == null) {
            appLocalData = new DatabaseHelper(context.getApplicationContext());
            //appLocalData = OpenHelperManager.getHelper(context, DbHandler.class);
        }
        return appLocalData;
    }

    //creating the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME
                + "(" + COLUMN_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_START_TIME +
                " DATETIME, " + COLUMN_R_SYS + " VARCHAR, " + COLUMN_R_DIA + " VARCHAR, " + COLUMN_R_PULSE + " VARCHAR, " + COLUMN_L_SYS + " VARCHAR, " + COLUMN_L_DIA + " VARCHAR, " + COLUMN_L_PULSE + " VARCHAR, " + COLUMN_STATUS +
                " TINYINT);";
        db.execSQL(sql);
    }

    //upgrading the database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS Persons";
        db.execSQL(sql);
        onCreate(db);
    }

    /*
     * This method is taking two arguments
     * first one is the name that is to be saved
     * second one is the status
     * 0 means the name is synced with the server
     * 1 means the name is not synced with the server
     * */
    public boolean addName(Log m) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

       // contentValues.put(COLUMN_START_TIME, m.getTreatmentStartTimeStamp());
        contentValues.put(COLUMN_R_SYS, m.getRightHandSystolic());
        contentValues.put(COLUMN_R_DIA, m.getRightHandDiastolic());
        contentValues.put(COLUMN_R_PULSE, m.getRightHandPulse());
        contentValues.put(COLUMN_L_SYS, m.getLeftHandSystolic());
        contentValues.put(COLUMN_L_DIA, m.getLeftHandDiastolic());
        contentValues.put(COLUMN_L_PULSE, m.getLeftHandPulse());
        contentValues.put(COLUMN_STATUS, m.getStatus());


        db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    /*
     * This method taking two arguments
     * first one is the id of the name for which
     * we have to update the sync status
     * and the second one is the status that will be changed
     * */
    public boolean updateNameStatus(int id, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_STATUS, status);
        db.update(TABLE_NAME, contentValues, COLUMN_ID + "=" + id, null);
        db.close();
        return true;
    }

    /*
     * this method will give us all the name stored in sqlite
     * */
    public Cursor getNames() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME;
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

    /*
     * this method is for getting all the unsynced name
     * so that we can sync it with database
     * */
    public Cursor getUnsyncedNames() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_STATUS + " = 0;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

}