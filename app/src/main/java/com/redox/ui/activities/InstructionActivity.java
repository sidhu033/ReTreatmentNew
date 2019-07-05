package com.redox.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.redox.R;
import com.redox.ui.adapters.TreatmentLogsAdapter;
import com.j256.ormlite.stmt.query.In;

import java.util.Timer;
import java.util.TimerTask;


public class InstructionActivity extends AppCompatActivity {

    TextView textView1;
    Button btnNext;
    ImageView imgBtn;
    private PreferenceHelper preferenceHelper;
    private Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);
        preferenceHelper = new PreferenceHelper(this);

       /* if(preferenceHelper.getIsLogin()) {
            Intent intent = new Intent(InstructionActivity.this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(intent);
            this.finish();
        }*/

        try {
            getSupportActionBar().hide();
        }catch (Exception e){

        }

        textView1 = findViewById(R.id.tv1);
        btnNext = findViewById(R.id.tv2);
        imgBtn = findViewById(R.id.btn_img);
        imgBtn.setColorFilter(getResources().getColor(R.color.white));
        setTitle("Welcome to Redoxer Treatment ");
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferenceHelper.putIsLogin(false);
                startActivity(new Intent(InstructionActivity.this, HomeActivity.class));
            }
        });
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(InstructionActivity.this);
        builder.setCancelable(false);
        builder.setMessage("Do you want to Logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user pressed "yes", then he is allowed to exit from application
                preferenceHelper.putIsLogin(false);
                Intent intent = new Intent(InstructionActivity.this,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                InstructionActivity.this.finish();
            }
        });

        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app
                InstructionActivity.super.onBackPressed();
                dialog.cancel();
            }
        });
        AlertDialog alert=builder.create();
        alert.show();
    }
    @Override
    protected void onPause() {
        super.onPause();

        timer = new Timer();
        Log.i("Main", "Invoking logout timer");
        LogOutTimerTask logoutTimeTask = new LogOutTimerTask();
        timer.schedule(logoutTimeTask, 1800000); //auto logout in 5 minutes
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timer != null) {
            timer.cancel();
            Log.i("Main", "cancel timer");
            timer = null;
        }

    }

    private class LogOutTimerTask extends TimerTask {

        @Override
        public void run() {

            //redirect user to login screen
            preferenceHelper.putIsLogin(false);
            /*Intent intent = new Intent(InstructionActivity.this,LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);*/
            InstructionActivity.this.finish();
        }
    }


}
