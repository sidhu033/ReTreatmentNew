package com.redox.localdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.redox.localdb.models.TreatmentLog;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class AppLocalData extends OrmLiteSqliteOpenHelper {

    // name of the database file for your application -- change to something appropriate for your app
    private static final String DATABASE_NAME = "redoxer.db";
    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 1;

    // the DAO objects use to access the JS DB tables
    private Dao<TreatmentLog, Integer> treatmentLogDao = null;

    public AppLocalData(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static AppLocalData appLocalData;
    public static AppLocalData getInstance(Context context){
        if (appLocalData == null) {
            appLocalData = OpenHelperManager.getHelper(context, AppLocalData.class);
        }
        return appLocalData;
    }

    /**
     * This is called when the database is first created. Usually you should call createTable statements here to create
     * the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.i(AppLocalData.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, TreatmentLog.class);
        } catch (SQLException e) {
            Log.e(AppLocalData.class.getName(), "Can't create database", e);
        }
    }

    /**
     * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
     * the various data to match the new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.i(AppLocalData.class.getName(), "onUpgrade");
            TableUtils.dropTable(connectionSource, TreatmentLog.class, true);

            // after we drop the old databases, we create the new ones
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(AppLocalData.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the Database Access Object (DAO) for our Customer class. It will create it or just give the cached
     * value.
     */

    public Dao<TreatmentLog, Integer> getTreatmentLogDao() throws SQLException {
        if (treatmentLogDao == null) {
            treatmentLogDao = getDao(TreatmentLog.class);
        }
        return treatmentLogDao;
    }




    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        treatmentLogDao = null;
    }
}
