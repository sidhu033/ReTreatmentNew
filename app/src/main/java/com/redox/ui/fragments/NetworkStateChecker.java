package com.redox.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.redox.localdb.DatabaseHelper;
import com.redox.localdb.DbHandler;
import com.redox.localdb.models.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Belal on 1/27/2017.
 */

public class NetworkStateChecker extends BroadcastReceiver {

    //context and database helper object
    private Context context;
    private DbHandler db;
    Log m=new Log();


    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            this.context = context;

            db = new DbHandler(context);

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            //if there is a network
            if (activeNetwork != null) {
                //if connected to wifi or mobile data plan
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {

                    //getting all the unsynced names
                    Cursor cursor = db.getUnsyncedNames();
                    if (cursor.moveToFirst()) {
                        do {
                            //calling the method to save the unsynced name to MySQL
                            saveName(
                                    cursor.getInt(cursor.getColumnIndex(DbHandler.KEY_ID)),
                                    cursor.getString(cursor.getColumnIndex(DbHandler.START_TIME)),
                                    cursor.getInt(cursor.getColumnIndex(DbHandler.R_SYS)),
                                    cursor.getInt(cursor.getColumnIndex(DbHandler.R_DIA)),
                                    cursor.getInt(cursor.getColumnIndex(DbHandler.R_PULSE)),
                                    cursor.getInt(cursor.getColumnIndex(DbHandler.L_SYS)),
                                    cursor.getInt(cursor.getColumnIndex(DbHandler.L_DIA)),
                                    cursor.getInt(cursor.getColumnIndex(DbHandler.L_PULSE))

                            );
                        } while (cursor.moveToNext());
                    }
                }
            }
        }
        catch (RuntimeException e){
            e.printStackTrace();
        }
    }


    /*
     * method taking two arguments
     * name that is to be saved and id of the name from SQLite
     * if the name is successfully sent
     * we will update the status as synced in SQLite
     * */
    private void saveName(final int id, final String starttime, final int r_sys, final int r_dia, final int r_pulse, final int l_sys, final int l_dia, final int l_pulse ) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DashboardFragment.URL_SAVE_NAME,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //updating the status in sqlite
                                db.updateNameStatus(id, DashboardFragment.NAME_SYNCED_WITH_SERVER);

                                //sending the broadcast to refresh the list
                                context.sendBroadcast(new Intent(DashboardFragment.DATA_SAVED_BROADCAST));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("strttime", "1234");
                params.put("r_sys", String.valueOf(r_sys));
                params.put("r_dia", String.valueOf(r_dia));
                params.put("r_pulse",String.valueOf(r_pulse));
                params.put("l_sys", String.valueOf(l_sys));
                params.put("l_dia", String.valueOf(l_dia));
                params.put("l_pulse", String.valueOf(l_pulse));
                return params;
            }
        };

        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }
}