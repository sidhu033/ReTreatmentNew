package com.redox.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.redox.R;


public class SplashActivity extends AppCompatActivity {
    private PreferenceHelper preferenceHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        preferenceHelper = new PreferenceHelper(this);

        try {
            getSupportActionBar().hide();
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }catch (Exception e){

        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run()
            {
               // preferenceHelper.putIsLogin(false);
                startActivity(new Intent(SplashActivity.this, InstructionActivity.class));
                finish();
            }
        }, 3000);

    }
}
