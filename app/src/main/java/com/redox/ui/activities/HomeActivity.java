package com.redox.ui.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.redox.R;
import com.redox.ui.widgets.custom_fab.FloatingActionButton;
import com.redox.ui.widgets.custom_fab.FloatingActionMenu;
import com.redox.ui.widgets.custom_fab.SubActionButton;
import com.redox.services.BluetoothLeService;
import com.redox.services.RedoxerDeviceService;
import com.redox.ui.fragments.DashboardFragment;
import com.redox.ui.adapters.HomeFragmentPagerAdapter;
import com.ts.tech.maa.main.services.SharedPreferencesService;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jp.wasabeef.blurry.Blurry;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, DashboardFragment.DashboardFragmentEvents {

    private static final String TAG = "HomeActivity";

    private String RIGHT_DEVICE_ADDRESS;
    private String LEFT_DEVICE_ADDRESS;

    public static final int NAME_SYNCED_WITH_SERVER = 1;
    public static final int NAME_NOT_SYNCED_WITH_SERVER = 0;


    private RedoxerDeviceService mRedoxerDeviceService;
    private DashboardFragment mDashboardFragament;
    private SharedPreferencesService mSharedPreferencesService;

    private BroadcastReceiver mBatteryLevelReciever;
    private PreferenceHelper preferenceHelper;
    private Timer timer;


    public static int position = 0;
    public int count = 0, whiteColor = 0, colorAccent = 0;
    HomeFragmentPagerAdapter fragmentPagerAdapter;
    ViewPager viewPager;
    ImageView homeImg, summaryImg;
    TextView home_txt;
    TextView summary_txt;
    LinearLayout homeTabview, llBottomMenu, llMainParent;
    RelativeLayout summaryTabview;
    SubActionButton.Builder lCSubBuilder;
    SubActionButton subFabButton1, subFabButton2, subFabButton3;
    FloatingActionMenu leftCenterMenu;
    ImageView tvHideScreen;
    Resources resources;
    List<String> mList;

    public BluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private String starttime;
    public int r_sys,r_dia,r_pulse,l_sys,l_dia,l_pulse,status;

    //done
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setTitle("               Redoxer Treatment     ");

        // Prohibit screen sleeping while treatment is going on
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }catch(Exception e){

        }

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get device addresses
        mSharedPreferencesService = new SharedPreferencesService(this);
        LEFT_DEVICE_ADDRESS = mSharedPreferencesService.getString("LEFTHANDDEVICEADDRESS", "");
        RIGHT_DEVICE_ADDRESS = mSharedPreferencesService.getString("RIGHTHANDDEVICEADDRESS", "");

        //TODO: Uncomment once testing is done
        if (LEFT_DEVICE_ADDRESS.isEmpty() && RIGHT_DEVICE_ADDRESS.isEmpty()) {
            Toast.makeText(this, "Please configure devices", Toast.LENGTH_LONG).show();
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            finish();
            return;
        }

        initializeView();
        initializeFabMenu();
        setUpPager();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        mBatteryLevelReciever= new BatteryBroadcastReceiver();
    }

    @Override
    protected void onStart()
    {
        registerReceiver(mBatteryLevelReciever, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        super.onStart();
    }
    @Override
    protected void onStop() {
        unregisterReceiver(mBatteryLevelReciever);
        super.onStop();
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            // TODO: Uncomment once testing done
            mBluetoothLeService.connect(RIGHT_DEVICE_ADDRESS);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    //private ProgressDialog mProgressDialog;
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
            {
                //mConnected = true;
                //updateConnectionState(R.string.connected);
                //invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
            {
                //Toast.makeText(HomeActivity.this, "Device disconnected! Please try again", Toast.LENGTH_LONG).show();
               /* if (mDashboardFragament != null) {
                    stopTreatmentWithError("Device disconnected! Please restart devices and try again");
                }*/
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());

                mRedoxerDeviceService = new RedoxerDeviceService(mBluetoothLeService, mBluetoothLeService.mBluetoothGatt);
                try
                {
                         boolean status = mRedoxerDeviceService.startDevice();
                         Log.d(TAG,"start device"+mRedoxerDeviceService.startDevice());

                        if(status == true)
                        {
                         Thread.sleep(5000);
                          boolean readstatus = mRedoxerDeviceService.readdevice();

                          handleResponseDevice(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));

                        }
                        else
                        {
                            Log.d(TAG,"false");
                        }

                       /* if(status ==true)
                        {

                                 mRedoxerDeviceService.readdevice();
                                 handleResponseDevice(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));

                        }*/
                }
                catch (Exception e)
                {
                    stopTreatmentWithError("Failed to start device! Please restart device and try again");
                    e.printStackTrace();
                }
                /*mProgressDialog= new ProgressDialog(HomeActivity.this);
                mProgressDialog.setMessage("Measuring BP...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();*/

            }
            else  if(BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))
            {

                handleResponseDevice(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));
               // handleResponseFromDevice(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };
    private void handleResponseDevice(byte[] data)
    {
        if(data !=null)
        {
            Log.d(TAG,"data"+data);
        }
        else
        {
            Log.d(TAG,"data"+data);
        }

    }
    private void handleResponseFromDevice(byte[] hexData)
    {
        Log.d(TAG, "handleResponseFromDevice: " + BluetoothLeService.bytesToHex(hexData));
        String c = hexData.toString();

        // To catch possible array out of bound exception
        try {
            // Check if response headers are present
            if (hexData[0] == (byte) 0xD0 && hexData[1] == (byte) 0xC2)
            {
                Log.d(TAG,"hexData");
                final int dataLength = hexData[2];
                final int command = hexData[3];
                final int highBp = hexData[4] & 0xFF;
                final int lowBp = hexData[5] & 0xFF;
                final int pulse = hexData[6] & 0xFF;

                // Error Condition
                if (command == (byte) 0xCC && lowBp == 0 && pulse == 0)
                {

                    String errorMessage = "";
                    if (highBp == 1) {
                        errorMessage = "Sensor is not working correctly!!";
                    } else if (highBp == 2) {
                        errorMessage = "Heartbeat or Blood Pressure not detected!! Please place device on correct position.";
                    } else if (highBp == 3) {
                        errorMessage = "Device is not measuring correctly!!";
                    } else if (highBp == 4) {
                        errorMessage = "Cuff are too loose or leak!!";
                    } else if (highBp == 5) {
                        errorMessage = "Trachea is blocked!!";
                    } else if (highBp == 6) {
                        errorMessage = "Measurement of pressure fluctuations!!";
                    } else if (highBp == 7) {
                        errorMessage = "Pressure exceeds the upper limit!!";
                    } else if (highBp == 8) {
                        errorMessage = "The calibration data invalid!!";
                    } else {
                        errorMessage = "Something went wrong";
                    }

                    //Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();

                    if (mDashboardFragament != null)
                    {
                        /*if (mProgressDialog!=null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }*/
                        stopTreatmentWithError(errorMessage);
                    }

                } else
                    {
                    if (mDashboardFragament != null)
                    {
                        mDashboardFragament.displayBpReadings(highBp, lowBp, pulse);

                        if (command == (byte) 0xCC)
                        {

                            /*if (mProgressDialog!=null && mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }*/

                            // Delay for narration from device that tells BP readings before treatment starts
                            new Handler().postDelayed(new Runnable()
                            {
                                @Override
                                public void run() {

                                    mDashboardFragament.startOrResumeTreatment(starttime, r_sys, r_dia, r_pulse, l_sys, l_dia, l_pulse, status);
                                }
                            }, 30 * 1000);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        timer = new Timer();
        Log.i("Main", "Invoking logout timer");
        LogOutTimerTask logoutTimeTask = new LogOutTimerTask();
        timer.schedule(logoutTimeTask, 1800000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // If attempted to unbind not binded service
        try {
           // unbindService(mServiceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBluetoothLeService = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect peripherals.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
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
            try {
                preferenceHelper.putIsLogin(false);
                /*Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);*/
                HomeActivity.this.finish();
            } catch (RuntimeException e)

            {
                e.printStackTrace();
            }
        }
    }

    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void initializeView() {

        viewPager = (ViewPager) findViewById(R.id.dashboard_viewpager);
        homeImg = (ImageView) findViewById(R.id.home_img);
        summaryImg = (ImageView) findViewById(R.id.summary_img);
        home_txt = (TextView) findViewById(R.id.home_txt);
        summary_txt = (TextView) findViewById(R.id.summary_txt);
        homeTabview = (LinearLayout) findViewById(R.id.home_tabview);
        summaryTabview = (RelativeLayout) findViewById(R.id.summary_tabview);
        whiteColor = ContextCompat.getColor(HomeActivity.this, R.color.white);
        colorAccent = ContextCompat.getColor(HomeActivity.this, R.color.colorAccent);
        resources = getResources();
        tvHideScreen = (ImageView) findViewById(R.id.tv_hide_screen);
        llBottomMenu = (LinearLayout) findViewById(R.id.ll_bottom_menu);
        llMainParent = (LinearLayout) findViewById(R.id.ll_parent);
        ColorFilter colorFilter = new ColorFilter();
        ImageView imgBar = findViewById(R.id.img_lower_bar);
        imgBar.setColorFilter(getResources().getColor(R.color.bg2));
        setBottomMenuTextColor(0, 12);
        initializeList();
        showMessageDialog();
    }

    private void showMessageDialog() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Dialog dialog = new Dialog(HomeActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.custom_dialog);
                MessageAdapter adapter = new MessageAdapter(HomeActivity.this, mList);
                final ViewPager viewPager = dialog.findViewById(R.id.view_pager1);
                viewPager.setAdapter(adapter);

                viewPager.setClipToPadding(false);
                viewPager.setPadding(48, 0, 48, 0);
                viewPager.setPageMargin(24);
                TabLayout tabLayout = dialog.findViewById(R.id.tab_layout);
                tabLayout.setupWithViewPager(viewPager);

                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                final Button btnNext = dialog.findViewById(R.id.btn_next);
                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e(TAG, "onClick: " + (mList.size() - 1) + " " + viewPager.getCurrentItem());
                        if (mList.size() - 2 == viewPager.getCurrentItem()) {
                            btnNext.setText("Close");
                        } else {
                            btnNext.setText("NEXT");
                        }
                        if (mList.size() - 1 > viewPager.getCurrentItem()) {
                            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                        } else {
                            dialog.dismiss();
                            // sendBroadcast(new Intent(DIALOG_CLOSE));
                        }
                    }
                });
                Button btnCancel = dialog.findViewById(R.id.btn_cancel);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        // sendBroadcast(new Intent(DIALOG_CLOSE));
                    }
                });
            }
        }, 500000);
    }

    private void initializeList() {
        mList = new ArrayList<>();
        mList.add("Protection To Heart, Brain, Kidney, Liver and Other Vital Organs Against Injury and biological insults ");
        mList.add("Protection Against Multi Organ Failure");
        mList.add("Blood pressure diagnosis and control");
        mList.add("Cardiovascular Risks Attenuated");
        mList.add("Potent Defence against lifestyle disease");
        mList.add("Benefits That Mimic Physical Exercise");
        mList.add("Enhances body’s Defence Mechanism");
        mList.add("Benefits Include Antiaging & Performance Enhancement");
        mList.add("Improving extreme athletic performance without steroids");
    }

    private void setUpPager() {
        fragmentPagerAdapter = new HomeFragmentPagerAdapter(getSupportFragmentManager(), HomeActivity.this);
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.getAdapter().notifyDataSetChanged();
        fragmentPagerAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(position);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setBottomMenuTextColor(position, 12);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });

        mDashboardFragament = (DashboardFragment) fragmentPagerAdapter.dashBoardFragment;

        homeTabview.setOnClickListener(this);
        summaryTabview.setOnClickListener(this);
    }

    private void initializeFabMenu() {
        final Resources resources = getResources();
        int redActionButtonSize = resources.getDimensionPixelSize(R.dimen.red_action_button_size);
        int redActionButtonMargin = resources.getDimensionPixelOffset(R.dimen.action_button_margin);
        int marginBottom = resources.getDimensionPixelOffset(R.dimen.margin_bottom);
        int redActionButtonContentSize = resources.getDimensionPixelSize(R.dimen.red_action_button_content_size);
        int redActionButtonContentMargin = resources.getDimensionPixelSize(R.dimen.red_action_button_content_margin);
        int redActionMenuRadius = resources.getDimensionPixelSize(R.dimen.red_action_menu_radius);
        int blueSubActionButtonSize = resources.getDimensionPixelSize(R.dimen.blue_sub_action_button_size);
        int blueSubActionButtonContentMargin = resources.getDimensionPixelSize(R.dimen.blue_sub_action_button_content_margin);

        final ImageView fabIconStar = new ImageView(this);
        fabIconStar.setImageDrawable(ResourcesCompat.getDrawable(resources,
                R.drawable.ic_add_circle_outline_white_24dp, null));
        FloatingActionButton.LayoutParams starParams = new FloatingActionButton
                .LayoutParams(redActionButtonSize, redActionButtonSize);
        starParams.setMargins(redActionButtonMargin,
                redActionButtonMargin,
                redActionButtonMargin,
                5);
        fabIconStar.setLayoutParams(starParams);

        FloatingActionButton.LayoutParams fabIconStarParams = new FloatingActionButton
                .LayoutParams(redActionButtonContentSize, redActionButtonContentSize);
        fabIconStarParams.setMargins(redActionButtonContentMargin,
                redActionButtonContentMargin,
                redActionButtonContentMargin,
                redActionButtonContentMargin);
        final FloatingActionButton leftCenterButton = new FloatingActionButton.Builder(this)
                .setContentView(fabIconStar, fabIconStarParams)
                .setBackgroundDrawable(R.drawable.fab_main_button_action_selector)
                .setPosition(FloatingActionButton.POSITION_BOTTOM_CENTER)
                .setLayoutParams(starParams)
                .build();

        lCSubBuilder = new SubActionButton.Builder(this);
        lCSubBuilder.setBackgroundDrawable(ResourcesCompat.getDrawable(resources,
                R.drawable.fab_button_action_selector, null));
        FrameLayout.LayoutParams blueContentParams = new FrameLayout.LayoutParams
                (FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        lCSubBuilder.setLayoutParams(blueContentParams);

        // Set custom layout params
        FrameLayout.LayoutParams blueParams = new FrameLayout
                .LayoutParams(blueSubActionButtonSize, blueSubActionButtonSize);
        lCSubBuilder.setLayoutParams(blueParams);

        ImageView lcIcon1 = new ImageView(this);
        ImageView lcIcon2 = new ImageView(this);
        ImageView lcIcon3 = new ImageView(this);

        lcIcon1.setImageDrawable(ResourcesCompat.getDrawable(resources,
                R.drawable.setting_menu_button_48, null));
        lcIcon2.setImageDrawable(ResourcesCompat.getDrawable(resources,
                R.drawable.setting_menu_button_48, null));
        lcIcon3.setImageDrawable(ResourcesCompat.getDrawable(resources,
                R.drawable.analytics_48dp, null));

        subFabButton1 = lCSubBuilder.setContentView(lcIcon1, blueContentParams).build();
        subFabButton2 = lCSubBuilder.setContentView(lcIcon2, blueContentParams).build();
        subFabButton3 = lCSubBuilder.setContentView(lcIcon3, blueContentParams).build();

        subFabButton1.setId(R.id.sub_button1);
        subFabButton2.setId(R.id.sub_button2);
        subFabButton3.setId(R.id.sub_button3);

        LinearLayout settingLinearLayout = new LinearLayout(this);
        settingLinearLayout.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        settingLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        tv.setText(getResources().getString(R.string.settings));
        tv.setTextColor(whiteColor);
        tv.setTextSize(13);
        tv.setId(R.id.tv_setting_sub_menu);
        // tv.setPadding(10, -10, 10, 10);
        tv.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        settingLinearLayout.addView(tv);
        subFabButton1.addView(settingLinearLayout);

        TextView tv2 = new TextView(this);
        tv2.setText(getResources().getString(R.string.profile));
        tv2.setTextColor(whiteColor);
        tv2.setTextSize(13);
        tv2.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        subFabButton2.addView(tv2);

        TextView tv3 = new TextView(this);
        tv3.setText(getResources().getString(R.string.analytics));
        tv3.setTextColor(whiteColor);
        tv3.setTextSize(13);
        tv3.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        subFabButton3.addView(tv3);


        leftCenterMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(subFabButton2)
                .addSubActionView(subFabButton3)
                .addSubActionView(subFabButton1)
                .setRadius(redActionMenuRadius)
                .setStartAngle(210)
                .setEndAngle(330)
                .attachTo(leftCenterButton)
                .build();

        subFabButton1.setOnClickListener(this);
        subFabButton2.setOnClickListener(this);
        subFabButton3.setOnClickListener(this);

        if (tvHideScreen != null) {
            tvHideScreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e(TAG, "onClick:tvHideScreen 1");
                    leftCenterMenu.close(true);
                }
            });

        }

        leftCenterMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override

            public void onMenuOpened(FloatingActionMenu menu) {
                /*
                 * open plus button and blur the background*/
                setBottomMenuTextColor(position, 13);
                tvHideScreen.setEnabled(true);
                Blurry.with(HomeActivity.this)
                        .radius(15)
                        .sampling(1)
                        .color(Color.argb(48, 128, 128, 128))
                        .animate()
                        .capture(findViewById(R.id.ll_parent))
                        .into((ImageView) findViewById(R.id.tv_hide_screen));

                tvHideScreen.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvHideScreen.setVisibility(View.VISIBLE);
                        viewPager.setVisibility(View.INVISIBLE);
                        llBottomMenu.setVisibility(View.INVISIBLE);
                        viewPager.setEnabled(false);
                    }
                }, 300);


                fabMenuAnimation(fabIconStar, 0, 45, R.drawable.minus_circle_outline);
            }

            @Override
            public void onMenuClosed(FloatingActionMenu menu) {
                /*
                 * close plus button and remove blur the background*/
                setBottomMenuTextColor(position, 14);
                viewPager.setVisibility(View.VISIBLE);
                llBottomMenu.setVisibility(View.VISIBLE);
                tvHideScreen.setVisibility(View.GONE);

                Blurry.delete((ViewGroup) findViewById(R.id.re_view_pager_parent));
                viewPager.setEnabled(true);

                fabMenuAnimation(fabIconStar, 45, 0, R.drawable.ic_add_circle_outline_white_24dp);
            }
        });
    }

    public void fabMenuAnimation(final ImageView fabIconStar, int startD, int endD, final int resourceColor) {

        ObjectAnimator floatingButtonAnimator = ObjectAnimator.ofFloat(fabIconStar,
                "rotation", startD, endD);
        floatingButtonAnimator.setDuration(500);
        floatingButtonAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                fabIconStar.setImageDrawable(ResourcesCompat.getDrawable(resources,
                        resourceColor, null));
                fabIconStar.setRotation(45);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }
        });
        floatingButtonAnimator.start();
    }

    private void setBottomMenuTextColor(int position, int call) {
        //Log.i(TAG, "setBottomMenuTextColor: " + call);
        if (position == 0) {
            HomeActivity.position = 0;
            homeImg.setImageResource(R.drawable.home_blue_24dp);
            //home_txt.setTypeface(Typeface.SERIF,Typeface.NORMAL);
            home_txt.setTextColor(colorAccent);
            summaryImg.setImageResource(R.drawable.summary_white_24dp);
            summary_txt.setTextColor(whiteColor);


        } else if (position == 1) {
            HomeActivity.position = 1;
            summaryImg.setImageResource(R.drawable.summary_blue_24dp);
            summary_txt.setTextColor(colorAccent);
            homeImg.setImageResource(R.drawable.home_white_24dp);
            home_txt.setTextColor(whiteColor);

        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.home_tabview: {
                viewPager.setCurrentItem(0, true);
                break;
            }
            case R.id.summary_tabview: {
                viewPager.setCurrentItem(1, true);
                break;
            }
            case R.id.sub_button1:
                break;
            case R.id.sub_button2:
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                break;
            case R.id.sub_button3:
                Toast.makeText(HomeActivity.this, "Analytics", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onCycleOneDeviceOneFinished() {
        mBluetoothLeService.connect(LEFT_DEVICE_ADDRESS);
    }

    public class MessageAdapter extends PagerAdapter {
        LayoutInflater layoutInflater;
        TextView textView;
        List<String> mList;

        public MessageAdapter(Context context, List<String> mList) {
            // super(fm);
            layoutInflater = ((Activity) context).getLayoutInflater();
            this.mList = mList;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = layoutInflater.inflate(R.layout.message_content, container, false);
            textView = view.findViewById(R.id.tv_message);
            textView.setText(mList.get(position));
            ((ViewPager) container).addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            ((ViewPager) container).removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == (View) object;
        }
    }

    public void stopTreatmentWithError(String message) {

        if (mDashboardFragament != null) {
            mDashboardFragament.stopTimer();
            mDashboardFragament.resetCycleValue();
            mDashboardFragament.stopBlinkingAnimations();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(message);
        builder.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mDashboardFragament != null) {
                    if (mDashboardFragament != null) {
                        if (mDashboardFragament.currentCycle == mDashboardFragament.RIGHT_HAND_CYCLE_1) {
                            if (mBluetoothLeService.isConnected(RIGHT_DEVICE_ADDRESS)) {
                                try {
                                    mRedoxerDeviceService.startDevice();
                                } catch (Exception e) {
                                    stopTreatmentWithError("Failed to start right hand device! Please restart device and try again");
                                    e.printStackTrace();
                                }
                            } else {
                                mBluetoothLeService.connect(RIGHT_DEVICE_ADDRESS);
                            }
                        } else if (mDashboardFragament.currentCycle == mDashboardFragament.LEFT_HAND_CYCLE_1) {
                            if (mBluetoothLeService.isConnected(LEFT_DEVICE_ADDRESS)) {
                                try {
                                    mRedoxerDeviceService.startDevice();
                                } catch (Exception e) {
                                    stopTreatmentWithError("Failed to start left hand device! Please restart device and try again");
                                    e.printStackTrace();
                                }
                            } else {
                                mBluetoothLeService.connect(LEFT_DEVICE_ADDRESS);
                            }
                        }
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }


    private class BatteryBroadcastReceiver extends BroadcastReceiver {
        private final static String BATTERY_LEVEL = "level";

        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BATTERY_LEVEL, 0);
            if (level <= 10) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setMessage("Battery Low! Please connect charger...")

                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                dialog.dismiss();
                            }
                        });
                // Create the AlertDialog object and return it
                builder.create();
                builder.show();
            }
        }
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setCancelable(false);
        builder.setMessage("Do you want to Logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user pressed "yes", then he is allowed to exit from application
                preferenceHelper.putIsLogin(false);
                Intent intent = new Intent(HomeActivity.this,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                HomeActivity.this.finish();
            }
        });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app
                HomeActivity.super.onBackPressed();
                dialog.cancel();
            }
        });
        AlertDialog alert=builder.create();
        alert.show();
    }
}
