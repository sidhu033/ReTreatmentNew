package com.redox.ui.fragments;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.redox.R;
import com.redox.localdb.DbHandler;
import com.redox.localdb.models.Log;
import com.redox.ui.adapters.TreatmentLogsAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.redox.app.App.context;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {

    public static final String DIALOG_CLOSE = "com.grv.home.DIALOG_CLOSE";
    public static final String TREATMENT_FINISH = "com.grv.home.FINISH";

    //1 means data is synced and 0 means data is not synced
    public static final int NAME_SYNCED_WITH_SERVER = 1;
    public static final int NAME_NOT_SYNCED_WITH_SERVER = 0;

    //a broadcast to know weather the data is synced or not
    public static final String DATA_SAVED_BROADCAST = "net.simplifiedcoding.datasaved";

    //Broadcast receiver to know the sync status
    //  private BroadcastReceiver broadcastReceiver;
      DbHandler db;
    public static final String URL_SAVE_NAME = "http://192.168.1.15/Android/info1.php";
    ImageView imageView_leftCycle1, imageViewRight_cycle1;
    TextView textViewSys, textViewDia, textViewPulse;
    TextView textViewSysRight, textViewDiaRight, textViewPulseRight;
    TextView textViewLeft_cycle1, textViewLeft_cycle2;
    TextView textViewRight_cycle1, textViewRight_cycle2;
    Animation startAnimation;

    public final int RIGHT_HAND_CYCLE_1 = 1;
    public final int RIGHT_HAND_CYCLE_2 = 2;
    public final int LEFT_HAND_CYCLE_1 = 3;
    public final int LEFT_HAND_CYCLE_2 = 4;

    private final int CYCLE_DURATION = 5 * 60 * 1000;

    public int currentCycle = RIGHT_HAND_CYCLE_1;

    private DashboardFragmentEvents dashboardFragmentEvents;

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null)
                return;
            switch (intent.getAction()) {
                case DIALOG_CLOSE:
                    Toast.makeText(context, "Received ", Toast.LENGTH_SHORT).show();
                    //showReadings(textViewSys, textViewDia, textViewPulse, imageView_leftCycle1);
                    break;
                case TREATMENT_FINISH:
                    showTreatmentCompleteDialog();
                    break;
            }
        }
    };
    private CountDownTimer mCountDownTimer;
    private Date mTreatmentStartTimeStamp;

    TreatmentLogsAdapter treatmentLogsAdapter;

    private ArrayList<Log> logs;
    private TextView tvStartTime;

    public DashboardFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(int position) {
        return new DashboardFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mTreatmentStartTimeStamp = new Date();


        //the broadcast receiver to update sync status
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //loading the names again

            }
        };




        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dashboardFragmentEvents = (DashboardFragmentEvents) getActivity();
        initViews(view);
        //showReadings(textViewSys, textViewDia, textViewPulse, imageView_leftCycle1)
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DIALOG_CLOSE);
        intentFilter.addAction(TREATMENT_FINISH);
        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);
        getActivity().registerReceiver(mBroadcastReceiver, new IntentFilter(DATA_SAVED_BROADCAST));
        getActivity().registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    private void initViews(View view) {

        imageView_leftCycle1 = view.findViewById(R.id.imageview_blink_left_cycle1);
        imageViewRight_cycle1 = view.findViewById(R.id.imageview_blink_right_cycle1);

        textViewLeft_cycle1 = view.findViewById(R.id.textview_cycle1_left);
        textViewLeft_cycle2 = view.findViewById(R.id.textview_cycle2_left);
        textViewRight_cycle1 = view.findViewById(R.id.textview_cycle1_right);
        textViewRight_cycle2 = view.findViewById(R.id.textview_cycle2_right);

        textViewSys = view.findViewById(R.id.textview_sys_left);
        textViewDia = view.findViewById(R.id.textview_dia_left);
        textViewPulse = view.findViewById(R.id.textview_pulse_left);

        textViewSysRight = view.findViewById(R.id.textview_sys_right);
        textViewDiaRight = view.findViewById(R.id.textview_dia_right);
        textViewPulseRight = view.findViewById(R.id.textview_pulse_right);
    }

    public void startBlinkingAnimation(ImageView imageView) {

        startAnimation = AnimationUtils.loadAnimation(context, R.anim.blink);
        imageView.setVisibility(View.VISIBLE);
        imageView.startAnimation(startAnimation);
    }

    public void resetCycleValue() {
        if (!textViewLeft_cycle1.getText().toString().contains("Complete")) {
            textViewLeft_cycle1.setTypeface(textViewLeft_cycle1.getTypeface(), Typeface.BOLD);
            textViewLeft_cycle1.setText("Cycle 1 (5m): " + 0);
        } else if (!textViewRight_cycle1.getText().toString().contains("Complete")) {
            textViewRight_cycle1.setTypeface(textViewRight_cycle1.getTypeface(), Typeface.BOLD);
            textViewRight_cycle1.setText("Cycle 1 (5m): " + 0);
        } else if (!textViewLeft_cycle2.getText().toString().contains("Complete")) {
            textViewLeft_cycle2.setTypeface(textViewLeft_cycle2.getTypeface(), Typeface.BOLD);
            textViewLeft_cycle2.setText("Cycle 2 (5m): " + 0);
        } else {
            textViewRight_cycle2.setTypeface(textViewRight_cycle2.getTypeface(), Typeface.BOLD);
            textViewRight_cycle2.setText("Cycle 2 (5m): " + 0);
        }
    }

    /*
     * this method will
     * load the names from the database
     * with updated sync status
     * */


    /*
     * this method will simply refresh the list
     * */

    /*
     * this method is saving the name to ther server
     * */
    private void saveNameToServer () {

        try {
          Log m =new Log();
            final String starttime = tvStartTime.getText().toString().trim();
            final int r_sys = Integer.parseInt(textViewSysRight.getText().toString().trim());
            final int r_dia = Integer.parseInt(textViewDiaRight.getText().toString().trim());
            final int r_pulse = Integer.parseInt(textViewPulseRight.getText().toString().trim());
            final int l_sys = Integer.parseInt(textViewSys.getText().toString().trim());
            final int l_dia = Integer.parseInt(textViewDia.getText().toString().trim());
            final int l_pulse = Integer.parseInt(textViewPulse.getText().toString().trim());
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_SAVE_NAME,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try {
                                JSONObject obj = new JSONObject(response);
                                if (!obj.getBoolean("error")) {
                                    //if there is a success
                                    //storing the name to sqlite with status synced
                                   startOrResumeTreatment(starttime, r_sys, r_dia, r_pulse, l_sys, l_dia, l_pulse, NAME_SYNCED_WITH_SERVER);
                                } else {
                                    //if there is some error
                                    //saving the name to sqlite with status unsynced
                                    startOrResumeTreatment(starttime, r_sys, r_dia, r_pulse, l_sys, l_dia, l_pulse, NAME_NOT_SYNCED_WITH_SERVER);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //on error storing the name to sqlite with status unsynced
                            startOrResumeTreatment(starttime, r_sys, r_dia, r_pulse, l_sys, l_dia, l_pulse, NAME_NOT_SYNCED_WITH_SERVER);
                        }
                    }) {


                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("starttime", "1234");
                    params.put("r_sys", String.valueOf(r_sys));
                    params.put("r_dia", String.valueOf(r_dia));
                    params.put("r_pulse", String.valueOf(r_pulse));
                    params.put("l_sys", String.valueOf(l_sys));
                    params.put("l_dia", String.valueOf(l_dia));
                    params.put("l_pulse", String.valueOf(l_pulse));
                    return params;

                }
            };

            VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
        }
        catch (RuntimeException e){
            e.printStackTrace();
        }
    }


    //saving the name to local storage
   /* private void startOrResumeTreatment(Log m)
    {
        DatabaseHelper db = new DatabaseHelper(context.getApplicationContext());
        Log n = new Log();
        db.getInstance(getActivity()).addName(n);
        Toast.makeText(context.getApplicationContext(), "Item Added", Toast.LENGTH_SHORT).show();
        logs.add(n);

    }
*/

    public void startOrResumeTreatment(String starttime, int r_sys, int r_dia, int r_pulse, int l_sys, int l_dia, int l_pulse, int status) {

        try {

            Log treatmentLog = new Log();
            treatmentLog.setRightHandDiastolic(Integer.parseInt(textViewDia.getText().toString().replaceAll("[^0-9]", "")));
            treatmentLog.setRightHandSystolic(Integer.parseInt(textViewSys.getText().toString().replaceAll("[^0-9]", "")));
            treatmentLog.setRightHandPulse(Integer.parseInt(textViewPulse.getText().toString().replaceAll("[^0-9]", "")));
            treatmentLog.setLeftHandDiastolic(Integer.parseInt(textViewDiaRight.getText().toString().replaceAll("[^0-9]", "")));
            treatmentLog.setLeftHandSystolic(Integer.parseInt(textViewSysRight.getText().toString().replaceAll("[^0-9]", "")));
            treatmentLog.setLeftHandPulse(Integer.parseInt(textViewPulseRight.getText().toString().replaceAll("[^0-9]", "")));
            treatmentLog.setTreatmentStartTimeStamp(new Date());
            treatmentLog.setTreatmentStartTimeStamp(mTreatmentStartTimeStamp);

            DbHandler db = new DbHandler(context.getApplicationContext());
            //call method to add data to db
            DbHandler database = new DbHandler(context.getApplicationContext());
            //db.addMovies(treatmentLog);

            DbHandler.getInstance(getActivity()).addMovies(starttime, r_sys, r_dia, r_pulse, l_sys, l_dia, l_pulse, status);
            db.close();
            Toast.makeText(context.getApplicationContext(), "Item Added", Toast.LENGTH_SHORT).show();

         /*  Log treatmentLog = new Log();
            treatmentLog.setRightHandDiastolic(Integer.parseInt(textViewDia.getText().toString().replaceAll("[^0-9]", "")));
            treatmentLog.setRightHandSystolic(Integer.parseInt(textViewSys.getText().toString().replaceAll("[^0-9]", "")));
            treatmentLog.setRightHandPulse(Integer.parseInt(textViewPulse.getText().toString().replaceAll("[^0-9]", "")));
            treatmentLog.setLeftHandDiastolic(Integer.parseInt(textViewDiaRight.getText().toString().replaceAll("[^0-9]", "")));
            treatmentLog.setLeftHandSystolic(Integer.parseInt(textViewSysRight.getText().toString().replaceAll("[^0-9]", "")));
            treatmentLog.setLeftHandPulse(Integer.parseInt(textViewPulseRight.getText().toString().replaceAll("[^0-9]", "")));
            treatmentLog.setTreatmentStartTimeStamp(new Date());
            //treatmentLog.setTreatmentStartTimeStamp(mTreatmentStartTimeStamp);*/


        } catch (NumberFormatException e) {
            e.printStackTrace();
        }


        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        mCountDownTimer = new CountDownTimer(CYCLE_DURATION, 1000) {

            public void onTick(long millisUntilFinished) {

                long seconds = millisUntilFinished / 1000;//convert to seconds
                long minutes = seconds / 60;
                if (minutes > 0)//if we have minutes, then there might be some remainder seconds
                    seconds = seconds % 60;//seconds can be between 0-60, so we use the % operator to get the remainder
                //similar to seconds
                String time = formatNumber(minutes) + ":" +
                        formatNumber(seconds);

                if (!textViewLeft_cycle1.getText().toString().contains("Complete")) {
                    textViewLeft_cycle1.setTypeface(textViewLeft_cycle1.getTypeface(), Typeface.BOLD);
                    textViewLeft_cycle1.setText("Cycle 1 (5m): " + time);
                } else if (!textViewRight_cycle1.getText().toString().contains("Complete")) {
                    textViewRight_cycle1.setTypeface(textViewRight_cycle1.getTypeface(), Typeface.BOLD);
                    textViewRight_cycle1.setText("Cycle 1 (5m): " + time);
                } else if (!textViewLeft_cycle2.getText().toString().contains("Complete")) {
                    textViewLeft_cycle2.setTypeface(textViewLeft_cycle2.getTypeface(), Typeface.BOLD);
                    textViewLeft_cycle2.setText("Cycle 2 (5m): " + time);
                } else {
                    textViewRight_cycle2.setTypeface(textViewRight_cycle2.getTypeface(), Typeface.BOLD);
                    textViewRight_cycle2.setText("Cycle 2 (5m): " + time);
                }
            }

            public void onFinish() {
                if (!textViewLeft_cycle1.getText().toString().contains("Complete")) {
                    textViewLeft_cycle1.setText("Cycle 1 (5m): Complete");
                    textViewLeft_cycle1.setTypeface(Typeface.SERIF, Typeface.NORMAL);
                    imageView_leftCycle1.clearAnimation();
                    imageView_leftCycle1.setVisibility(View.GONE);
                    //showReadings(textViewSysRight, textViewDiaRight, textViewPulseRight, imageViewRight_cycle1);
                    currentCycle = LEFT_HAND_CYCLE_1;

                    // TODO: Check if dashboardFragmentEvents not null
                    dashboardFragmentEvents.onCycleOneDeviceOneFinished();

                } else if (!textViewRight_cycle1.getText().toString().contains("Complete")) {
                    textViewRight_cycle1.setText("Cycle 1 (5m): Complete");
                    textViewRight_cycle1.setTypeface(Typeface.SERIF, Typeface.NORMAL);
                    imageViewRight_cycle1.clearAnimation();
                    imageViewRight_cycle1.setVisibility(View.GONE);
                    // imageViewleft_cycle2.setVisibility(View.VISIBLE);
                    currentCycle = RIGHT_HAND_CYCLE_2;
                    //saveNameToServer();
                    //startOrResumeTreatment(starttime, r_sys, r_dia, r_pulse, l_sys, l_dia, l_pulse, status);


                } else if (!textViewLeft_cycle2.getText().toString().contains("Complete")) {
                    textViewLeft_cycle2.setTypeface(Typeface.SERIF, Typeface.NORMAL);
                    textViewLeft_cycle2.setText("Cycle 2 (5m): Complete");
                    imageView_leftCycle1.clearAnimation();
                    /*imageViewleft_cycle2.setVisibility(View.GONE);*/
                    imageView_leftCycle1.setVisibility(View.GONE);
                    currentCycle = LEFT_HAND_CYCLE_2;
                    //saveNameToServer();
                    startOrResumeTreatment(starttime, r_sys, r_dia, r_pulse, l_sys, l_dia, l_pulse, status);
                    saveNameToServer();
                    //startBlinkingAnimation(imageViewRight_cycle1);
                } else {
                    textViewRight_cycle2.setText("Cycle 2 (5m): Complete");
                    textViewRight_cycle2.setTypeface(Typeface.SERIF, Typeface.NORMAL);
                    imageViewRight_cycle1.clearAnimation();
                    imageViewRight_cycle1.setVisibility(View.GONE);
                    getActivity().sendBroadcast(new Intent(TREATMENT_FINISH));
                    showTreatmentCompleteDialog();
                }
            }
        }.start();

        if (currentCycle == RIGHT_HAND_CYCLE_1) {
            startBlinkingAnimation(imageView_leftCycle1);
        } else if (currentCycle == LEFT_HAND_CYCLE_1) {
            startBlinkingAnimation(imageViewRight_cycle1);
        } else if (currentCycle == RIGHT_HAND_CYCLE_2) {
            startBlinkingAnimation(imageView_leftCycle1);
        } else if (currentCycle == LEFT_HAND_CYCLE_2) {
            startBlinkingAnimation(imageViewRight_cycle1);
        }
    }


    /**
     * Stop timer
     */
    public void stopTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    /**
     * Clear animations of both hands
     */
    public void stopBlinkingAnimations() {
        imageView_leftCycle1.clearAnimation();
        imageViewRight_cycle1.clearAnimation();
        imageView_leftCycle1.setVisibility(View.GONE);
        imageViewRight_cycle1.setVisibility(View.GONE);
    }

    /**
     * Display BP readings based on current cycle
     * in right hand or left hand section
     *
     * @param highBp
     * @param lowBp
     * @param pulse
     */
    public void displayBpReadings(int highBp, int lowBp, int pulse) {
        try {

            if (currentCycle == RIGHT_HAND_CYCLE_1) {
                textViewSys.setText("SYS (mmhg): " + "   " + highBp);
                textViewDia.setText("DIA (mmhg): " + "    " + lowBp);
                textViewPulse.setText("PULSE: " + "               " + pulse);
            } else if (currentCycle == LEFT_HAND_CYCLE_1) {
                textViewSysRight.setText("SYS (mmhg): " + "   " + highBp);
                textViewDiaRight.setText("DIA (mmhg): " + "    " + lowBp);
                textViewPulseRight.setText("PULSE: " + "               " + pulse);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showTreatmentCompleteDialog()
    {
        String type="";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Treatment Complete");
        builder.setMessage("Your Redoxer treatment done successfully!. \nSee you next time");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {

                saveNameToServer();
                dialogInterface.dismiss();

            }
        }).show();
        //Create log
      /*  TreatmentLog treatmentLog= new TreatmentLog();
        treatmentLog.setRightHandDiastolic(Integer.parseInt(textViewDia.getText().toString().replaceAll("[^0-9]", "")));
        treatmentLog.setRightHandSystolic(Integer.parseInt(textViewSys.getText().toString()));
        treatmentLog.setRightHandPulse(Integer.parseInt(textViewPulse.getText().toString()));
        treatmentLog.setLeftHandDiastolic(Integer.parseInt(textViewDiaRight.getText().toString()));
        treatmentLog.setLeftHandDiastolic(Integer.parseInt(textViewSysRight.getText().toString()));
        treatmentLog.setLeftHandDiastolic(Integer.parseInt(textViewPulseRight.getText().toString()));
        treatmentLog.setTreatmentEndTimeStamp(new Date());
        treatmentLog.setTreatmentStartTimeStamp(mTreatmentStartTimeStamp);


            Log a =new Log();
          //  DbHandler.getInstance(getActivity()).addMovies(a);*/

    }

    private String formatNumber(long value) {
        if (value < 10)
            return "0" + value;
        return value + "";
    }

    public interface DashboardFragmentEvents {
        void onCycleOneDeviceOneFinished();
    }
}
