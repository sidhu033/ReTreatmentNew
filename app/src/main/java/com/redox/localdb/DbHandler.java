package com.redox.localdb;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.app.FragmentActivity;
import android.widget.EditText;


import com.redox.R;
import com.redox.localdb.models.Log;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.redox.localdb.models.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DbHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "redox";//name of the database
    private static final String TABLE_NAME = "Treatment_Log";//name for the table
    //String DB_PATH = null;
    public static final String KEY_ID = "key";
    public static final String USER_NAME="username";
    private static final String  COLUMN_STATUS ="column_status";
    public static final String START_TIME = "start_time";
    public static final String R_SYS = "r_sys";
    public static final String R_DIA = "r_dia";
    public static final String R_PULSE = "r_pulse";
    public static final String L_SYS ="l_sys" ;
    public static final String L_DIA = "l_dia";
    public static final String L_PULSE = "l_pulse";
    public static final String STATUS ="status";

    private EditText etusername;
    //private SQLiteDatabase mDb;
    //private static Context mycontext;
    // private FloatingActionMenu getAssets;

    public DbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    private static DbHandler appLocalData;
    public static DbHandler getInstance(Context context){
        if (appLocalData == null) {
            appLocalData = new DbHandler(context.getApplicationContext());
            //appLocalData = OpenHelperManager.getHelper(context, DbHandler.class);
        }
        return appLocalData;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        //Query to create table in database
        String CREATE_CONTACTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + START_TIME + " DATETIME," + R_SYS + " INTEGER," + R_DIA + " INTEGER," + R_PULSE + " INTEGER,"  + L_SYS + " INTEGER," + L_DIA + " INTEGER," + L_PULSE + " INTEGER,"  + STATUS + " INTEGER" +")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    //Executes once a database change is occurred
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);
        onCreate(db);
    }


    //method to add row to table
    public  void addMovies(String starttime, int r_sys, int r_dia, int r_pulse, int l_sys, int l_dia, int l_pulse, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
       // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ContentValues values = new ContentValues();
        // values.put(KEY_ID, m.getId());
        ContentValues contentValues =new ContentValues();
        contentValues.put(START_TIME, starttime);
        contentValues.put(R_SYS, r_sys);
        contentValues.put(R_DIA, r_dia);
        contentValues.put(R_PULSE, r_pulse);
        contentValues.put(L_SYS, l_sys);
        contentValues.put(L_DIA, l_dia);
        contentValues.put(L_PULSE, l_pulse);
        contentValues.put(STATUS, status);
        db.insert(TABLE_NAME,null,contentValues);

      //  db.insert(TABLE_NAME, null, values);
        db.close();
    }




    //method to list all details from table
    public List<Log> getAllUsers() {
        List<Log> logList = new ArrayList<Log>();
        //etusername = (EditText) findViewById(R.id.etusername);
        String selectQuery = "SELECT * FROM " + TABLE_NAME;//retrieve data from the database
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                Log m = new Log();
                m.setId(cursor.getInt(0));
                m.setTreatmentStartTimeStamp(new Date());
                m.setRightHandSystolic(cursor.getInt(2));
                m.setRightHandDiastolic(cursor.getInt(3));
                m.setRightHandPulse(cursor.getInt(4));
                m.setLeftHandSystolic(cursor.getInt(5));
                m.setLeftHandDiastolic(cursor.getInt(6));
                m.setLeftHandPulse(cursor.getInt(7));
                logList.add(m);
            } while (cursor.moveToNext());
        }

        return logList;
    }

    public boolean updateNameStatus(int id, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(STATUS, status);
        db.update(TABLE_NAME, contentValues, KEY_ID + "=" + id, null);
        db.close();
        return true;
    }
    public Cursor getUnsyncedNames() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + STATUS + " = 0;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }


}