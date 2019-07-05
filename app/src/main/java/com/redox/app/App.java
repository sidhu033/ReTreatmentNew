package com.redox.app;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by gaurav on 5/25/18.
 */

public class App extends Application {
  public static  Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
    }
}
