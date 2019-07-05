package com.redox.localdb;


import android.content.Context;

import com.redox.localdb.models.TreatmentLog;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;

public class AppLocalDataOperations {

    private AppLocalData mAppLocalData;
    private static AppLocalDataOperations instance;

    /**
     * Private constructor for making singleton instance
     *
     * @param context
     */

    private AppLocalDataOperations(Context context) {
        mAppLocalData = AppLocalData.getInstance(context.getApplicationContext());
    }

    /**
     * Get singleton instance
     *
     * @param context
     * @return
     */
    public static AppLocalDataOperations getInstance(Context context) {
        if (instance == null) {
            instance = new AppLocalDataOperations(context);
        }

        return instance;
    }


    /**
     * To store bhajan list
     *
     * @param treatmentLog: list of directories
     * @throws SQLException
     */

    public int createOrUpdateTreatmentLog(TreatmentLog treatmentLog) throws SQLException {


        Dao<TreatmentLog, Integer> treatmentLogDao = mAppLocalData.getTreatmentLogDao();
        return treatmentLogDao.create(treatmentLog);
    }


    /**
     * Fetch all logs
     *
     * @throws SQLException
     */
    public ArrayList<TreatmentLog> getTreatmentLogs() throws SQLException {
        Dao<TreatmentLog, Integer> treatmentLogDao = mAppLocalData.getTreatmentLogDao();

        return (ArrayList<TreatmentLog>) treatmentLogDao.queryBuilder().orderBy("treatmentEndTimeStamp", false).query();
    }
}