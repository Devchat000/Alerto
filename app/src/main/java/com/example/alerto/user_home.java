package com.example.alerto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class user_home extends AppCompatActivity {

    public static final  String Message_KEY = "message_key";
    public static final String ACTIVITY_MESSAGE = "Activity_Message";
    SwitchCompat detectAccidentSwitch, detectSpeedSwitch;
    TextView check,accident_dialog_timer_txt,accident_dialog_level_txt, respondTimeLeft;
    ImageView call_national_helpline, call_child_abusing, call_women_safety;
    ConstraintLayout accident_dialog, speed_dialog;
    LinearLayout respondDialog;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Button accident_dialog_help_btn, accident_dialog_ok_btn, respond_ok_btn, speed_in_a_hurry;
    NavigationView navigation_view;
    static int PERMISSION_CODE= 100;

    private BroadcastReceiver accReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String Message = intent.getStringExtra(Message_KEY);

            if(Message.equals("Low")){
                Log.i("Accident Level", "Low");
                accident_dialog_level_txt.setText("We Detected A Low Crash");
                setView();
            }
            else if(Message.equals("Medium")){
                Log.i("Accident Level", "Medium");
                accident_dialog_level_txt.setText("We Detected A Medium Crash");
                setView();
            }
            else if(Message.equals("High")){
                Log.i("Accident Level", "High");
                accident_dialog_level_txt.setText("We Detected A High Crash");
                setView();
            }
            else if(Message.contains("Time1")&& accident_dialog.getVisibility() == View.VISIBLE){
                String Time = Message.substring(6);
                accident_dialog_timer_txt.setText("Calling for help in "+Time+" seconds");
            }
            else if (Message.equals("TimeSecondFirst")) {
                setView();
            }
            else if(Message.contains("Time2")){
                Log.i("Time", "Time2");
                String Time = Message.substring(6);
                respondTimeLeft.setText("\uD83D\uDD51 Sending Respond to Emergency Service in "+Time+" .");
            }
            else if(Message.equals("TimeEmergencyComplete")){
                Log.i("Time", "TimeEmergencyComplete");
                respondTimeLeft.setText("✓ Respond Sent To Emergency Contacts");
            }
            else if(Message.equals("TimeSOSComplete") && accident_dialog.getVisibility() == View.VISIBLE){
                Log.i("Time", "TimeSOSComplete");
                setView();
            }
            else if(Message.equals("OverSpeed")){
                setView();
            }

        }
    };


    private void iAmOkay() {

        SharedPreferences sharedPreferences = getSharedPreferences("View_Visible",MODE_PRIVATE);
        String accidentLevel = sharedPreferences.getString("accidentLevel","Low");
        Intent intent = new Intent(ACTIVITY_MESSAGE);
        boolean flagSensorOnChange = sharedPreferences.getBoolean("accident_detect_flag",true);
        if(!flagSensorOnChange){


            if(accident_dialog.getVisibility()==View.VISIBLE && respondDialog.getVisibility()==View.GONE && (accidentLevel.equals("Low") || accidentLevel.equals("Medium") || accidentLevel.equals("High"))){
                //only cancel the timer and notification
                intent.putExtra(user_home.Message_KEY, "USER_FINE");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
            else if (accident_dialog.getVisibility() == View.GONE && respondDialog.getVisibility() == View.VISIBLE && respondTimeLeft.getVisibility() == View.GONE && (accidentLevel.equals("Medium") || accidentLevel.equals("High"))) {
                //only send message to sos contacts about user is ok also cancel the timer and notification
                intent.putExtra(user_home.Message_KEY, "USER_FINE_SOS");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }


            Toast.makeText(user_home.this, "Ok", Toast.LENGTH_SHORT).show();
        }
        else{
            //Do nothing
            //Toast.makeText(this, "No accident detected", Toast.LENGTH_SHORT).show();


            if (accident_dialog.getVisibility() == View.GONE && respondDialog.getVisibility() == View.VISIBLE && respondTimeLeft.getVisibility() == View.GONE && accidentLevel.equals("Low")) {
                //only send message to sos contacts about user is ok also cancel the timer and notification
                intent.putExtra(user_home.Message_KEY, "USER_FINE_SOS");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
            else if (accident_dialog.getVisibility() == View.GONE && respondDialog.getVisibility() == View.VISIBLE && respondTimeLeft.getVisibility() == View.VISIBLE && respondTimeLeft.getText() == "✓ Respond Sent To Emergency Contacts") {
                //send message to sos contacts and emergency service about user is ok also cancel the timer and notification
                if (accidentLevel.equals("Medium")) {
                    intent.putExtra(user_home.Message_KEY, "USER_FINE_Medium");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
                else if (accidentLevel.equals("High")) {
                    intent.putExtra(user_home.Message_KEY, "USER_FINE_High");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
            }
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("accident_detect_flag",true);
        editor.putBoolean("accident_dialog",false);
        editor.putBoolean("respond_dialog",false);
        editor.putBoolean("respond_time_left",false);
        editor.putBoolean("speed_dialog",false);
        editor.apply();

        setView();

    }

    private void needHelp() {
        final String[] options = {" Ambulance", " Fire Service", " Police"};

        // Create a boolean array to track the checked state of each option
        final boolean[] checkedOptions = {false, false, false};

        // Create the AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(user_home.this);

        // Set the title and message of the dialog
        builder.setTitle("Please Select At least One Service: ");

        // Add the checkboxes to the dialog
        builder.setMultiChoiceItems(options, checkedOptions, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                checkedOptions[which] = isChecked;
                boolean isAnyOptionChecked = false;
                for (boolean checkedOption : checkedOptions) {
                    if (checkedOption) {
                        isAnyOptionChecked = true;
                        break;
                    }
                }
                ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(isAnyOptionChecked);
            }
        });

        // Add an "OK" button to the dialog
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String text = "Message sent to:";
                // Handle the "OK" button click
                // You can access the checked state of each option using the checkedOptions array
                // For example:
                if (checkedOptions[0]) {
                    // Option 1 is checked
                    text += " Ambulance";
                }
                if (checkedOptions[1]) {
                    // Option 2 is checked
                    text += " Fire Service";
                }
                if (checkedOptions[2]) {
                    // Option 3 is checked
                    text += " Police";
                }
                Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();

                if(text.length()>7){
                    Intent intent = new Intent(ACTIVITY_MESSAGE);
                    intent.putExtra(user_home.Message_KEY, text);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                    SharedPreferences sharedPreferences = getSharedPreferences("View_Visible",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("accident_detect_flag",true);
                    editor.putBoolean("accident_dialog",false);
                    editor.putBoolean("respond_dialog",false);
                    editor.putBoolean("respond_time_left",false);
                    editor.putBoolean("speed_dialog",false);
                    editor.apply();

                    setView();
                }
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Disable the "OK" button by default
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(accReceiver,new IntentFilter(G_Force_Counter.SERVICE_MESSAGE));

        setView();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{android.Manifest.permission.SEND_SMS}, 1);
            }
        }

        try{

            String msg = getIntent().getStringExtra("Msg");
            if(msg.equals("HELP")){
                needHelp();
            }
            else if(msg.equals("FINE")){
                iAmOkay();
            } else if (msg.equals("Hurry")) {
                hurry();
            }
        }
        catch (Exception e){

        }
    }

    private void hurry() {
        SharedPreferences sharedPreferences = getSharedPreferences("View_Visible",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("speed_dialog",false);
        editor.apply();

        setView();
    }


    private void setView() {

        SharedPreferences sharedPreferences = getSharedPreferences("View_Visible",MODE_PRIVATE);
        boolean accident_dialog_visibility_flag = sharedPreferences.getBoolean("accident_dialog", false);
        boolean respond_dialog_visibility_flag = sharedPreferences.getBoolean("respond_dialog",false);
        boolean respond_dialog_time_left_visibility_flag = sharedPreferences.getBoolean("respond_time_left",false);
        boolean speed_dialog_visibility_flag = sharedPreferences.getBoolean("speed_dialog",false);
        if(accident_dialog_visibility_flag){
            accident_dialog.setVisibility(View.VISIBLE);
        }
        else{
            accident_dialog.setVisibility(View.GONE);
        }

        if(respond_dialog_visibility_flag){
            respondDialog.setVisibility(View.VISIBLE);
        }
        else{
            respondDialog.setVisibility(View.GONE);
        }

        if(respond_dialog_time_left_visibility_flag){
            respondTimeLeft.setVisibility(View.VISIBLE);
        }
        else{
            respondTimeLeft.setVisibility(View.GONE);
        }


        if(speed_dialog_visibility_flag){
            speed_dialog.setVisibility(View.VISIBLE);
        }
        else {
            speed_dialog.setVisibility(View.GONE);
        }

        Log.i("Set View", "Update View");

    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(accReceiver);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        accident_dialog = findViewById(R.id.accident_dialog);
        speed_dialog = findViewById(R.id.speed_dialog);
        detectAccidentSwitch = findViewById(R.id.detectAccident);
        detectSpeedSwitch = findViewById(R.id.detectSpeed);
        accident_dialog_level_txt = findViewById(R.id.accident_dialog_level_txt);
        accident_dialog_timer_txt = findViewById(R.id.accident_dialog_timer_txt);
        respondDialog = findViewById(R.id.respondDialog);
        respondTimeLeft = findViewById(R.id.respondTimeLeft);
        accident_dialog_help_btn = findViewById(R.id.accident_dialog_help_btn);
        accident_dialog_ok_btn = findViewById(R.id.accident_dialog_ok_btn);
        respond_ok_btn = findViewById(R.id.respond_ok_btn);
        speed_in_a_hurry = findViewById(R.id.speed_in_a_hurry);
        check = findViewById(R.id.check);
        navigation_view = findViewById(R.id.navigation_view);
        call_child_abusing = findViewById(R.id.call_child_abusing);
        call_national_helpline = findViewById(R.id.call_national_helpline);
        call_women_safety = findViewById(R.id.call_women_safety);

        TextView name_txt = navigation_view.inflateHeaderView(R.layout.header_layout).findViewById(R.id.name_txt);

        SharedPreferences user_shared_preference = getSharedPreferences("User_Data",MODE_PRIVATE);
        name_txt.setText(user_shared_preference.getString("name","User Name"));

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);

        if(isMyServiceRunning(G_Force_Counter.class)){
            detectAccidentSwitch.setChecked(true);
        }
        else{
            detectAccidentSwitch.setChecked(false);
        }

        call_child_abusing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(user_home.this,Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(user_home.this,new String[]{Manifest.permission.CALL_PHONE},PERMISSION_CODE);
                }
                else{
                    Intent i = new Intent(Intent.ACTION_CALL);
                    i.setData(Uri.parse("tel:"+"6292286766"));
                    startActivity(i);
                }
            }
        });

        call_national_helpline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(user_home.this,Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(user_home.this,new String[]{Manifest.permission.CALL_PHONE},PERMISSION_CODE);
                }
                else{
                    Intent i = new Intent(Intent.ACTION_CALL);
                    i.setData(Uri.parse("tel:"+"8617722517"));
                    startActivity(i);
                }
            }
        });

        call_women_safety.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(user_home.this,Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(user_home.this,new String[]{Manifest.permission.CALL_PHONE},PERMISSION_CODE);
                }
                else{
                    Intent i = new Intent(Intent.ACTION_CALL);
                    i.setData(Uri.parse("tel:"+"8617722517"));
                    startActivity(i);
                }
            }
        });

        respond_ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iAmOkay();
            }
        });


        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.OpenDrawer, R.string.CloseDrawer);
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        navigationView.bringToFront();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if(id==R.id.profile){
                    Intent intent = new Intent(user_home.this, User_Profile.class);
                    startActivity(intent);
                    Toast.makeText(user_home.this, "Profile", Toast.LENGTH_SHORT).show();

                } else if (id == R.id.about) {
                    Intent intent = new Intent(user_home.this, About.class);
                    startActivity(intent);
//                    Toast.makeText(user_home.this, "About", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.feedback) {
                    Intent intent = new Intent(user_home.this, Feedback.class);
                    startActivity(intent);
//                    Toast.makeText(user_home.this, "FeedBack", Toast.LENGTH_SHORT).show();
                } else{

                    stopService(new Intent(user_home.this,G_Force_Counter.class));
                    stopService(new Intent(user_home.this, Speed_Counter_Service.class));
                    SharedPreferences.Editor user_shared_preference_editor = user_shared_preference.edit();
                    user_shared_preference_editor.clear();
                    user_shared_preference_editor.putBoolean("Login",false);
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(user_home.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

//                    Toast.makeText(user_home.this, "Log Out", Toast.LENGTH_SHORT).show();
                }

                drawerLayout.closeDrawer(GravityCompat.START);

                return true;
            }
        });

        speed_in_a_hurry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("View_Visible",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("speed_dialog",false);
                editor.apply();

                setView();

                Intent intent = new Intent(user_home.this,Speed_Counter_Service.class);
                stopService(intent);
            }
        });

        accident_dialog_ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iAmOkay();
            }
        });


        accident_dialog_help_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                needHelp();
            }
        });

        detectAccidentSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent gForceService = new Intent(user_home.this, G_Force_Counter.class);
                SharedPreferences sharedPreferences = getSharedPreferences("View_Visible",MODE_PRIVATE);
                if(isChecked){
                    if (ActivityCompat.checkSelfPermission(user_home.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(user_home.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        // Request permission to access the location


                        ActivityCompat.requestPermissions(user_home.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                        detectAccidentSwitch.setChecked(false);

                    }else{
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            editor.putBoolean("accident_detect_flag",true);
                            editor.apply();
                            startForegroundService(gForceService);
                        }
                        else{
                            startService(gForceService);
                        }
                    }

                }
                else{
                    boolean flagSensorOnChange = sharedPreferences.getBoolean("accident_detect_flag",true);
                    if(!flagSensorOnChange){
                        Toast.makeText(user_home.this, "First Respond For Detected Accident", Toast.LENGTH_SHORT).show();
                        detectAccidentSwitch.setChecked(true);
                    }
                    else{
                        stopService(gForceService);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("accident_dialog",false);
                        editor.putBoolean("respond_dialog",false);
                        editor.putBoolean("respond_time_left",false);
                        editor.apply();
                        setView();
                    }
                }
            }
        });

        if(isMyServiceRunning(Speed_Counter_Service.class)){
            detectSpeedSwitch.setChecked(true);
        }
        else{
            detectSpeedSwitch.setChecked(false);
        }


        detectSpeedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent(user_home.this,Speed_Counter_Service.class);
                if(isChecked){

                    if (ActivityCompat.checkSelfPermission(user_home.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(user_home.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        // Request permission to access the location

                        ActivityCompat.requestPermissions(user_home.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

                        detectSpeedSwitch.setChecked(false);

                    } else {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent);
                        }
                        else{
                            startService(intent);
                        }

                    }

                }
                else{
                    stopService(intent);
                    speed_dialog.setVisibility(View.GONE);
                }
            }
        });


    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences = getSharedPreferences("View_Visible",MODE_PRIVATE);
        boolean flagSensorOnChange = sharedPreferences.getBoolean("accident_detect_flag",true);


        if(flagSensorOnChange){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("accident_dialog",false);
            editor.putBoolean("respond_dialog",false);
            editor.putBoolean("respond_time_left",false);
            editor.putBoolean("speed_dialog",false);
            editor.apply();
        }
        setView();
    }

    @Override
    public void onBackPressed(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


}
