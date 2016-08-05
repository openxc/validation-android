package com.openxc.openxcstarter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.telephony.CellIdentityGsm;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.HeadlampStatus;
import com.openxc.measurements.HighBeamStatus;
import com.openxc.measurements.IgnitionStatus;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.Odometer;
import com.openxc.measurements.ParkingBrakeStatus;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.TorqueAtTransmission;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.VehicleButtonEvent;
import com.openxc.measurements.VehicleDoorStatus;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.WindshieldWiperStatus;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.EventedSimpleVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;
import com.openxc.units.Boolean;
import com.openxcplatform.openxcstarter.BuildConfig;
import com.openxcplatform.openxcstarter.R;
import com.openxc.VehicleManager;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.EngineSpeed;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarException;
import java.util.regex.Pattern;

import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class StarterActivity extends ListActivity implements OnClickListener, OnItemSelectedListener {

    //TODO: Pull VI type (Reference, CrossChasm, etc)...will require changes to firmware first

    private static final String TAG = "starterActivity";

    private VehicleManager mVehicleManager;
    private TextView mAcceleratorPedalView, mBrakePedalView, mEngineSpeedView, mFuelConsumedView, mFuelLevelView, mLatitudeView, mLongitudeView, mOdometerView,
            mSteeringWheelView, mTransmissionTorqueView, mTransmissionGearView, mVehicleSpeedView, mHeadlampView, mHighBeamView, mIgnitionView,
            mParkingBrakeView, mVehicleButtonView, mVehicleDoorView, mWiperView, mListenerView;

    private TextView accelSelection, brakeSelection, engineSpeedSelection, fuelConsumedSelection, fuelLevelSelection, latSelection, longSelection,
            odometerSelection, steerAngleSelection, torqueSelection, gearSelection, speedSelection, headLampSelection, highbeamSelection, ignitionSelection,
            parkingBrakeSelection, buttonSelection, doorSelection, wiperSelection;

    private EditText accelIssue, brakeIssue, engineSpeedIssue, fuelConsumedIssue, fuelLevelIssue, latIssue, longIssue, odometerIssue, steerAngleIssue,
            torqueIssue, gearIssue, speedIssue, headLampIssue, highbeamIssue, ignitionIssue, parkingBrakeIssue, buttonIssue, doorIssue, wiperIssue, vinInput,
            userVin;

    Set<String> receivedMessages = new HashSet<String>();
    private Set<String> allowedMessages = new HashSet<String>();

    private Toast mToast;
    SharedPreferences sharedPrefs;

    public TextView mVIVersion;
    public TextView mDeviceID;
    public TextView messageCount;
    public String[] signalResponse = new String[19];
    public String[] signalNames = new String[19];
    public String[] issueResponse = new String[19];
    public String[] jsonFormat = new String[19];
    public String[] postArray = new String[27];
    public String modelYearSelected;
    public String modelSelected;
    public TextView vinNum;
    public String version;
    public String deviceId;
    public String VIN;
    public int scanned = 0;

    private TextView mViVersionView;
    private TextView mViDeviceIdView;

    ArrayList<String> listItems=new ArrayList<String>();
    ArrayAdapter<String> adapter;
    private SlidingUpPanelLayout myLayout;

    private Button scanBtn;
    private TextView contentTxt;

    public static Activity activity;

    @Override //Creates the menu
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_about was selected
            case R.id.action_about: //Shows about popup
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                // set title
                alertDialogBuilder.setTitle("About");

                String versionCode = BuildConfig.VERSION_NAME;


                // set dialog message
                alertDialogBuilder
                        .setMessage("OpenXC Firmware Validation\nVersion: " + versionCode + "\n\nStored Name: "
                                + sharedPrefs.getString("UserName", "None") + "\nStored Email: " +
                                sharedPrefs.getString("UserEmail", "None"))
                        .setCancelable(true)
                        .setNegativeButton("Close",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                break;
            // action with ID action_reset was selected
            case R.id.action_reset: //Resets app to all defaults
                //Toast.makeText(this, "Reset", Toast.LENGTH_SHORT).show();
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                break;
            case R.id.action_clear_pii: //Clears out email and name
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.clear();
                editor.commit();
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Name and email have been cleared", Toast.LENGTH_SHORT);
                toast.show();
                break;
            case R.id.action_enter_pii: //Allows user to enter their info
                showInputDialog();
                break;


            default:
                break;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);

        myLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        myLayout.setPanelHeight(0);

        // grab a reference to the engine speed text object in the UI, so we can
        // manipulate its value later from Java code
        mAcceleratorPedalView = (TextView) findViewById(R.id.accelerator_pedal);
        mBrakePedalView = (TextView) findViewById(R.id.brake_pedal);
        mEngineSpeedView = (TextView) findViewById(R.id.engine_speed);
        mFuelConsumedView = (TextView) findViewById(R.id.fuel_consumed);
        mFuelLevelView = (TextView) findViewById(R.id.fuel_level);
        mLatitudeView = (TextView) findViewById(R.id.latitude);
        mLongitudeView = (TextView) findViewById(R.id.longitude);
        mOdometerView = (TextView) findViewById(R.id.odometer);
        mSteeringWheelView = (TextView) findViewById(R.id.steering_wheel);
        mTransmissionTorqueView = (TextView) findViewById(R.id.transmission_torque);
        mTransmissionGearView = (TextView) findViewById(R.id.transmission_gear);
        mVehicleSpeedView = (TextView) findViewById(R.id.vehicle_speed);
        mHeadlampView = (TextView) findViewById(R.id.headlamp_status);
        mHighBeamView = (TextView) findViewById(R.id.highbeam_status);
        mIgnitionView = (TextView) findViewById(R.id.ignition_status);
        mParkingBrakeView = (TextView) findViewById(R.id.parking_brake);
        mVehicleButtonView = (TextView) findViewById(R.id.button_event);
        mVehicleDoorView = (TextView) findViewById(R.id.door_status);
        mWiperView = (TextView) findViewById(R.id.windshield_wipers);

        mVIVersion = (TextView) findViewById(R.id.viVersion);
        mDeviceID = (TextView) findViewById(R.id.viDeviceId);
        vinNum = (TextView) findViewById(R.id.vin);

        Button yesAccel = (Button) findViewById(R.id.yesAccel);
        yesAccel.setOnClickListener(this); // calling onClick() method
        Button yesBrake = (Button) findViewById(R.id.yesBrake);
        yesBrake.setOnClickListener(this);
        Button yesEngineSpeed = (Button) findViewById(R.id.yesEngineSpeed);
        yesEngineSpeed.setOnClickListener(this);
        Button yesFuelconsumed = (Button) findViewById(R.id.yesFuelConsumed);
        yesFuelconsumed.setOnClickListener(this);
        Button yesFuelLevel = (Button) findViewById(R.id.yesFuelLevel);
        yesFuelLevel.setOnClickListener(this);
        Button yesHeadlamps = (Button) findViewById(R.id.yesHeadlamp);
        yesHeadlamps.setOnClickListener(this);
        Button yesHighbeams = (Button) findViewById(R.id.yesHighbeam);
        yesHighbeams.setOnClickListener(this);
        Button yesIgnition = (Button) findViewById(R.id.yesIgnition);
        yesIgnition.setOnClickListener(this);
        Button yesLatitude = (Button) findViewById(R.id.yesLat);
        yesLatitude.setOnClickListener(this);
        Button yesLongitude = (Button) findViewById(R.id.yesLong);
        yesLongitude.setOnClickListener(this);
        Button yesOdometer = (Button) findViewById(R.id.yesOdometer);
        yesOdometer.setOnClickListener(this);
        Button yesParkingBrake = (Button) findViewById(R.id.yesParkingBrake);
        yesParkingBrake.setOnClickListener(this);
        Button yesSteeringAngle = (Button) findViewById(R.id.yesSteerAngle);
        yesSteeringAngle.setOnClickListener(this);
        Button yesTorque = (Button) findViewById(R.id.yesTorque);
        yesTorque.setOnClickListener(this);
        Button yesGear = (Button) findViewById(R.id.yesGear);
        yesGear.setOnClickListener(this);
        Button yesButton = (Button) findViewById(R.id.yesButton);
        yesButton.setOnClickListener(this);
        Button yesDoor = (Button) findViewById(R.id.yesDoor);
        yesDoor.setOnClickListener(this);
        Button yesSpeed = (Button) findViewById(R.id.yesVehicleSpeed);
        yesSpeed.setOnClickListener(this);
        Button yesWipers = (Button) findViewById(R.id.yesWipers);
        yesWipers.setOnClickListener(this);

        Button noAccel = (Button) findViewById(R.id.noAccel);
        noAccel.setOnClickListener(this); // calling onClick() method
        Button noBrake = (Button) findViewById(R.id.noBrake);
        noBrake.setOnClickListener(this);
        Button noEngineSpeed = (Button) findViewById(R.id.noEngineSpeed);
        noEngineSpeed.setOnClickListener(this);
        Button noFuelconsumed = (Button) findViewById(R.id.noFuelConsumed);
        noFuelconsumed.setOnClickListener(this);
        Button noFuelLevel = (Button) findViewById(R.id.noFuelLevel);
        noFuelLevel.setOnClickListener(this);
        Button noHeadlamps = (Button) findViewById(R.id.noHeadlamp);
        noHeadlamps.setOnClickListener(this);
        Button noHighbeams = (Button) findViewById(R.id.noHighbeam);
        noHighbeams.setOnClickListener(this);
        Button noIgnition = (Button) findViewById(R.id.noIgnition);
        noIgnition.setOnClickListener(this);
        Button noLatitude = (Button) findViewById(R.id.noLat);
        noLatitude.setOnClickListener(this);
        Button noLongitude = (Button) findViewById(R.id.noLong);
        noLongitude.setOnClickListener(this);
        Button noOdometer = (Button) findViewById(R.id.noOdometer);
        noOdometer.setOnClickListener(this);
        Button noParkingBrake = (Button) findViewById(R.id.noParkingBrake);
        noParkingBrake.setOnClickListener(this);
        Button noSteeringAngle = (Button) findViewById(R.id.noSteerAngle);
        noSteeringAngle.setOnClickListener(this);
        Button noTorque = (Button) findViewById(R.id.noTorque);
        noTorque.setOnClickListener(this);
        Button noGear = (Button) findViewById(R.id.noGear);
        noGear.setOnClickListener(this);
        Button noButton = (Button) findViewById(R.id.noButton);
        noButton.setOnClickListener(this);
        Button noDoor = (Button) findViewById(R.id.noDoor);
        noDoor.setOnClickListener(this);
        Button noSpeed = (Button) findViewById(R.id.noVehicleSpeed);
        noSpeed.setOnClickListener(this);
        Button noWipers = (Button) findViewById(R.id.noWipers);
        noWipers.setOnClickListener(this);
        Button resetAccel = (Button) findViewById(R.id.resetAccel);
        resetAccel.setOnClickListener(this); // calling onClick() method
        Button resetBrake = (Button) findViewById(R.id.resetBrake);
        resetBrake.setOnClickListener(this);
        Button resetEngineSpeed = (Button) findViewById(R.id.resetEngineSpeed);
        resetEngineSpeed.setOnClickListener(this);
        Button resetFuelconsumed = (Button) findViewById(R.id.resetFuelConsumed);
        resetFuelconsumed.setOnClickListener(this);
        Button resetFuelLevel = (Button) findViewById(R.id.resetFuelLevel);
        resetFuelLevel.setOnClickListener(this);
        Button resetHeadlamps = (Button) findViewById(R.id.resetHeadlamp);
        resetHeadlamps.setOnClickListener(this);
        Button resetHighbeams = (Button) findViewById(R.id.resetHighbeam);
        resetHighbeams.setOnClickListener(this);
        Button resetIgnition = (Button) findViewById(R.id.resetIgnition);
        resetIgnition.setOnClickListener(this);
        Button resetLatitude = (Button) findViewById(R.id.resetLat);
        resetLatitude.setOnClickListener(this);
        Button resetLongitude = (Button) findViewById(R.id.resetLong);
        resetLongitude.setOnClickListener(this);
        Button resetOdometer = (Button) findViewById(R.id.resetOdometer);
        resetOdometer.setOnClickListener(this);
        Button resetParkingBrake = (Button) findViewById(R.id.resetParkingBrake);
        resetParkingBrake.setOnClickListener(this);
        Button resetSteeringAngle = (Button) findViewById(R.id.resetSteerAngle);
        resetSteeringAngle.setOnClickListener(this);
        Button resetTorque = (Button) findViewById(R.id.resetTorque);
        resetTorque.setOnClickListener(this);
        Button resetGear = (Button) findViewById(R.id.resetGear);
        resetGear.setOnClickListener(this);
        Button resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setOnClickListener(this);
        Button resetDoor = (Button) findViewById(R.id.resetDoor);
        resetDoor.setOnClickListener(this);
        Button resetSpeed = (Button) findViewById(R.id.resetVehicleSpeed);
        resetSpeed.setOnClickListener(this);
        Button resetWipers = (Button) findViewById(R.id.resetWipers);
        resetWipers.setOnClickListener(this);

        Button saveFile = (Button) findViewById(R.id.saveFile);
        saveFile.setOnClickListener(this);

        scanBtn = (Button)findViewById(R.id.scan_button);
        contentTxt = (TextView)findViewById(R.id.vin);
        scanBtn.setOnClickListener(this);

        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        setListAdapter(adapter);

        Button manualVinEntry = (Button) findViewById(R.id.manual_vin_button);
        manualVinEntry.setOnClickListener(this);

        boolean isConnected = isInternetAvailable();

        if(isConnected){
            //Pulling data for the vehicle models and years so we don't have to update the app every time a new model comes out
            try{
                new DownloadWebpageTask(new AsyncResult() {
                    @Override
                    public void onResult(JSONObject object) {
                        processJson(object);
                    }
                }).execute("https://spreadsheets.google.com/tq?key=1YP4XIwWZ2ubY4rd4jLzBn6do3VQT582KsKwQcfKBWJ8");
            } catch (Exception e) {

            }

        } else { //No internet = no way to pull stuff from the cloud. Default to a hardcoded list, which was current as of 5/27/2016
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No internet detected, loading default data", Toast.LENGTH_LONG);
            toast.show();

            String data = "{\"version\":\"0.6\",\"reqId\":\"0\",\"status\":\"ok\",\"sig\":\"1424404733\",\"table\":{\"cols\":[{\"id\":\"A\",\"label\":\"Models\",\"type\":\"string\"},{\"id\":\"B\",\"label\":\"Years\",\"type\":\"number\",\"pattern\":\"General\"}],\"rows\":[{\"c\":[{\"v\":\"C-Max\"},{\"v\":2005.0,\"f\":\"2005\"}]},{\"c\":[{\"v\":\"E-350\"},{\"v\":2006.0,\"f\":\"2006\"}]},{\"c\":[{\"v\":\"Ecosport\"},{\"v\":2007.0,\"f\":\"2007\"}]},{\"c\":[{\"v\":\"Edge\"},{\"v\":2008.0,\"f\":\"2008\"}]},{\"c\":[{\"v\":\"Escape\"},{\"v\":2009.0,\"f\":\"2009\"}]},{\"c\":[{\"v\":\"Expedition\"},{\"v\":2010.0,\"f\":\"2010\"}]},{\"c\":[{\"v\":\"Explorer\"},{\"v\":2011.0,\"f\":\"2011\"}]},{\"c\":[{\"v\":\"F-150\"},{\"v\":2012.0,\"f\":\"2012\"}]},{\"c\":[{\"v\":\"F-250\"},{\"v\":2013.0,\"f\":\"2013\"}]},{\"c\":[{\"v\":\"Falcon\"},{\"v\":2014.0,\"f\":\"2014\"}]},{\"c\":[{\"v\":\"Fiesta\"},{\"v\":2015.0,\"f\":\"2015\"}]},{\"c\":[{\"v\":\"Figo\"},{\"v\":2016.0,\"f\":\"2016\"}]},{\"c\":[{\"v\":\"Five Hundred\"},{\"v\":2017.0,\"f\":\"2017\"}]},{\"c\":[{\"v\":\"Flex\"},{\"v\":null}]},{\"c\":[{\"v\":\"Focus (automatic transmission)\"},{\"v\":null}]},{\"c\":[{\"v\":\"Focus (manual transmission)\"},{\"v\":null}]},{\"c\":[{\"v\":\"Focus Classic\"},{\"v\":null}]},{\"c\":[{\"v\":\"Focus Electric\"},{\"v\":null}]},{\"c\":[{\"v\":\"Freestar\"},{\"v\":null}]},{\"c\":[{\"v\":\"Freestyle\"},{\"v\":null}]},{\"c\":[{\"v\":\"Fusion\"},{\"v\":null}]},{\"c\":[{\"v\":\"Kuga\"},{\"v\":null}]},{\"c\":[{\"v\":\"Mariner\"},{\"v\":null}]},{\"c\":[{\"v\":\"MKS\"},{\"v\":null}]},{\"c\":[{\"v\":\"MKX\"},{\"v\":null}]},{\"c\":[{\"v\":\"MKZ\"},{\"v\":null}]},{\"c\":[{\"v\":\"Mondeo\"},{\"v\":null}]},{\"c\":[{\"v\":\"Mustang\"},{\"v\":null}]},{\"c\":[{\"v\":\"Navigator\"},{\"v\":null}]},{\"c\":[{\"v\":\"Navigator\"},{\"v\":null}]},{\"c\":[{\"v\":\"Ranger\"},{\"v\":null}]},{\"c\":[{\"v\":\"Super Duty\"},{\"v\":null}]},{\"c\":[{\"v\":\"Taurus\"},{\"v\":null}]},{\"c\":[{\"v\":\"Territory\"},{\"v\":null}]},{\"c\":[{\"v\":\"Transit\"},{\"v\":null}]},{\"c\":[{\"v\":\"Transit Connect\"},{\"v\":null}]}]}}";
            int start = data.indexOf("{", data.indexOf("{") + 1);
            int end = data.lastIndexOf("}");
            String jsonResponse = data.substring(start, end);

            try{
                JSONObject cachedInfo = new JSONObject(jsonResponse);
                processJson(cachedInfo);
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
        sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);


        if(sharedPrefs.getBoolean("first_time", true))
        {
            showInputDialog();
            sharedPrefs.edit().putBoolean("first_time", false).commit();
        }
    }

    protected void showInputDialog() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

        final EditText user_name = (EditText) promptView.findViewById(R.id.name);
        final EditText user_email = (EditText) promptView.findViewById(R.id.email);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sharedPrefs.edit().putString("UserEmail", user_email.getText().toString()).commit();
                        sharedPrefs.edit().putString("UserName", user_name.getText().toString()).commit();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    //Checks to see if the app can reach the internet...as long as google.com is working
    public boolean isInternetAvailable() {
        try {
            //InetAddress ipAddr = InetAddress.getByName("www.google.com"); //You can replace it with your name
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

            //if (ipAddr.equals("") && activeNetworkInfo == null && !activeNetworkInfo.isConnected()) {
            if (activeNetworkInfo == null && !activeNetworkInfo.isConnected()) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            return false;
        }

    }

    private void processJson(JSONObject object) { //Processing the data we get from the spreadsheet for models and years

        List<String> models = new ArrayList<String>();
        List<String> modelYears = new ArrayList<String>();

        // Spinner element
        Spinner model_year = (Spinner) findViewById(R.id.model_year);
        Spinner model = (Spinner) findViewById(R.id.model);

        // Spinner click listener
        model_year.setOnItemSelectedListener(this);
        model.setOnItemSelectedListener(this);

        try {
            JSONArray rows = object.getJSONArray("rows");

            for (int r = 0; r < rows.length(); ++r) {
                JSONObject row = rows.getJSONObject(r);
                JSONArray columns = row.getJSONArray("c");

                if(columns.getJSONObject(0).getString("v") != "null")
                    models.add(columns.getJSONObject(0).getString("v"));
                if(columns.getJSONObject(1).getString("v") != "null")
                    modelYears.add(columns.getJSONObject(1).getString("v").substring(0,4));
            }

            // Creating adapter for spinner
            ArrayAdapter<String> modelYearDataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, modelYears);
            ArrayAdapter<String> modelsDataApapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, models);

            // Drop down layout style - list view with radio button
            modelYearDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            modelsDataApapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // attaching data adapter to spinner
            model_year.setAdapter(modelYearDataAdapter);
            model.setAdapter(modelsDataApapter);

            //final TeamsAdapter adapter = new TeamsAdapter(this, R.layout.team, teams);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private synchronized void updateViInfo() { //Pulling and displaying the VI info in the app.
        //TODO: Would like for the little pause you experience in the app when the info loads to go away...
        vinInput = (EditText) findViewById(R.id.manualVIN);

        if(mVehicleManager != null) {
            // Must run in another thread or we get circular references to
            // VehicleService -> StatusFragment -> VehicleService and the
            // callback will just fail silently and be removed forever.
            new Thread(new Runnable() {
                public void run() {
                    try {
                        version = mVehicleManager.getVehicleInterfaceVersion();
                        deviceId = mVehicleManager.getVehicleInterfaceDeviceId();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                mVIVersion.setText("VI Version: " + version);
                                mDeviceID.setText("VI Device ID: " + deviceId);

                                //vinNum.setText("VIN: Manual Entry");
                                //vinInput.setVisibility(View.VISIBLE);

                                //TODO: Automatically pull VIN from the vehicle and only display the manual entry if it fails to pull

                                /*VehicleMessage response = mVehicleManager.request(new DiagnosticRequest(1,0x02, 0x09));

                                if(response != null)
                                {
                                    VIN = response.toString();
                                    vinNum.setText("VIN: " + VIN);
                                }
                                else
                                {
                                    vinNum.setText("VIN: Manual Entry");
                                    vinInput.setVisibility(View.VISIBLE);
                                }*/
                                //vinNum.setText("Manual VIN");
                            }
                        });
                    } catch(NullPointerException e) {
                        // A bit of a hack, should probably use a lock - but
                        // this can happen if this view is being paused and it's
                        // not really a problem.
                    }
                }
            }).start();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { //Saving the user selection to a usable variable

        String item = parent.getItemAtPosition(position).toString();

        if(parent.getId() == R.id.model_year)
        {
            modelYearSelected = item;
        }
        else if(parent.getId() == R.id.model)
        {
            modelSelected = item;
        }

    }

    public void onNothingSelected(AdapterView arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClick(View v) {
        // default method for handling onClick Events...

        //TODO: I'm sure there is a better way to do this...
        Button yesAccel = (Button) findViewById(R.id.yesAccel);
        Button yesBrake = (Button) findViewById(R.id.yesBrake);
        Button yesEngineSpeed = (Button) findViewById(R.id.yesEngineSpeed);
        Button yesFuelconsumed = (Button) findViewById(R.id.yesFuelConsumed);
        Button yesFuelLevel = (Button) findViewById(R.id.yesFuelLevel);
        Button yesHeadlamps = (Button) findViewById(R.id.yesHeadlamp);
        Button yesHighbeams = (Button) findViewById(R.id.yesHighbeam);
        Button yesIgnition = (Button) findViewById(R.id.yesIgnition);
        Button yesLatitude = (Button) findViewById(R.id.yesLat);
        Button yesLongitude = (Button) findViewById(R.id.yesLong);
        Button yesOdometer = (Button) findViewById(R.id.yesOdometer);
        Button yesParkingBrake = (Button) findViewById(R.id.yesParkingBrake);
        Button yesSteeringAngle = (Button) findViewById(R.id.yesSteerAngle);
        Button yesTorque = (Button) findViewById(R.id.yesTorque);
        Button yesGear = (Button) findViewById(R.id.yesGear);
        Button yesButton = (Button) findViewById(R.id.yesButton);
        Button yesDoor = (Button) findViewById(R.id.yesDoor);
        Button yesSpeed = (Button) findViewById(R.id.yesVehicleSpeed);
        Button yesWipers = (Button) findViewById(R.id.yesWipers);
        Button noAccel = (Button) findViewById(R.id.noAccel);
        Button noBrake = (Button) findViewById(R.id.noBrake);
        Button noEngineSpeed = (Button) findViewById(R.id.noEngineSpeed);
        Button noFuelconsumed = (Button) findViewById(R.id.noFuelConsumed);
        Button noFuelLevel = (Button) findViewById(R.id.noFuelLevel);
        Button noHeadlamps = (Button) findViewById(R.id.noHeadlamp);
        Button noHighbeams = (Button) findViewById(R.id.noHighbeam);
        Button noIgnition = (Button) findViewById(R.id.noIgnition);
        Button noLatitude = (Button) findViewById(R.id.noLat);
        Button noLongitude = (Button) findViewById(R.id.noLong);
        Button noOdometer = (Button) findViewById(R.id.noOdometer);
        Button noParkingBrake = (Button) findViewById(R.id.noParkingBrake);
        Button noSteeringAngle = (Button) findViewById(R.id.noSteerAngle);
        Button noTorque = (Button) findViewById(R.id.noTorque);
        Button noGear = (Button) findViewById(R.id.noGear);
        Button noButton = (Button) findViewById(R.id.noButton);
        Button noDoor = (Button) findViewById(R.id.noDoor);
        Button noSpeed = (Button) findViewById(R.id.noVehicleSpeed);
        Button noWipers = (Button) findViewById(R.id.noWipers);
        Button resetAccel = (Button) findViewById(R.id.resetAccel);
        Button resetBrake = (Button) findViewById(R.id.resetBrake);
        Button resetEngineSpeed = (Button) findViewById(R.id.resetEngineSpeed);
        Button resetFuelconsumed = (Button) findViewById(R.id.resetFuelConsumed);
        Button resetFuelLevel = (Button) findViewById(R.id.resetFuelLevel);
        Button resetHeadlamps = (Button) findViewById(R.id.resetHeadlamp);
        Button resetHighbeams = (Button) findViewById(R.id.resetHighbeam);
        Button resetIgnition = (Button) findViewById(R.id.resetIgnition);
        Button resetLatitude = (Button) findViewById(R.id.resetLat);
        Button resetLongitude = (Button) findViewById(R.id.resetLong);
        Button resetOdometer = (Button) findViewById(R.id.resetOdometer);
        Button resetParkingBrake = (Button) findViewById(R.id.resetParkingBrake);
        Button resetSteeringAngle = (Button) findViewById(R.id.resetSteerAngle);
        Button resetTorque = (Button) findViewById(R.id.resetTorque);
        Button resetGear = (Button) findViewById(R.id.resetGear);
        Button resetButton = (Button) findViewById(R.id.resetButton);
        Button resetDoor = (Button) findViewById(R.id.resetDoor);
        Button resetSpeed = (Button) findViewById(R.id.resetVehicleSpeed);
        Button resetWipers = (Button) findViewById(R.id.resetWipers);

        accelSelection = (TextView) findViewById(R.id.selectedAccel);
        brakeSelection = (TextView) findViewById(R.id.selectedBrake);
        engineSpeedSelection = (TextView) findViewById(R.id.selectedEngineSpeed);
        fuelConsumedSelection = (TextView) findViewById(R.id.selectedFuelConsumed);
        fuelLevelSelection = (TextView) findViewById(R.id.selectedFuelLevel);
        latSelection = (TextView) findViewById(R.id.selectedLat);
        longSelection = (TextView) findViewById(R.id.selectedLong);
        odometerSelection = (TextView) findViewById(R.id.selectedOdometer);
        steerAngleSelection = (TextView) findViewById(R.id.selectedSteerAngle);
        torqueSelection = (TextView) findViewById(R.id.selectedTorque);
        gearSelection = (TextView) findViewById(R.id.selectedGear);
        speedSelection = (TextView) findViewById(R.id.selectedVehicleSpeed);
        headLampSelection = (TextView) findViewById(R.id.selectedHeadLamp);
        highbeamSelection = (TextView) findViewById(R.id.selectedHighbeam);
        ignitionSelection = (TextView) findViewById(R.id.selectedIgnition);
        parkingBrakeSelection = (TextView) findViewById(R.id.selectedParkingBrake);
        buttonSelection = (TextView) findViewById(R.id.selectedButton);
        doorSelection = (TextView) findViewById(R.id.selectedDoor);
        wiperSelection = (TextView) findViewById(R.id.selectedWipers);

        accelIssue = (EditText) findViewById(R.id.textAccel);
        brakeIssue = (EditText) findViewById(R.id.textBrake);
        engineSpeedIssue = (EditText) findViewById(R.id.textEngineSpeed);
        fuelConsumedIssue = (EditText) findViewById(R.id.textFuelConsumed);
        fuelLevelIssue = (EditText) findViewById(R.id.textFuelLevel);
        latIssue = (EditText) findViewById(R.id.textLat);
        longIssue = (EditText) findViewById(R.id.textLong);
        odometerIssue = (EditText) findViewById(R.id.textOdometer);
        steerAngleIssue = (EditText) findViewById(R.id.textSteerAngle);
        torqueIssue = (EditText) findViewById(R.id.textTorque);
        gearIssue = (EditText) findViewById(R.id.textGear);
        speedIssue = (EditText) findViewById(R.id.textVehicleSpeed);
        headLampIssue = (EditText) findViewById(R.id.textHeadlamp);
        highbeamIssue = (EditText) findViewById(R.id.textHighbeam);
        ignitionIssue = (EditText) findViewById(R.id.textIgnition);
        parkingBrakeIssue = (EditText) findViewById(R.id.textParkingBrake);
        buttonIssue = (EditText) findViewById(R.id.textButton);
        doorIssue = (EditText) findViewById(R.id.textDoor);
        wiperIssue = (EditText) findViewById(R.id.textWipers);

        userVin = (EditText) findViewById(R.id.manualVIN);

        EditText emailAddress = (EditText) findViewById(R.id.email);
        EditText name = (EditText) findViewById(R.id.name);

        switch (v.getId()) {

            case R.id.scan_button:
                userVin.setVisibility(View.GONE);
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.initiateScan();
                break;
            case R.id.manual_vin_button:
                userVin.setVisibility(View.VISIBLE);
                TextView vinScanShow = (TextView) findViewById(R.id.vin);
                vinScanShow.setVisibility(View.GONE);
                scanned = 0;
                break;
            case R.id.yesAccel: //User selected the yes button
                yesAccel.setVisibility(View.GONE); //Hiding YES button
                noAccel.setVisibility(View.GONE); //Hiding NO button
                resetAccel.setVisibility(View.VISIBLE); //Showing reset button
                accelSelection.setVisibility(View.VISIBLE); //Showing which button was pressed
                accelSelection.setText("Yes");
                signalResponse[0] = "Yes"; //Recording user selection
                signalNames[0] = "Accelerator Pedal Position"; //Recording the signal in the same array position
                break;

            case R.id.yesBrake:
                yesBrake.setVisibility(View.GONE);
                noBrake.setVisibility(View.GONE);
                resetBrake.setVisibility(View.VISIBLE);
                brakeSelection.setVisibility(View.VISIBLE);
                brakeSelection.setText("Yes");
                signalResponse[1] = "Yes";
                signalNames[1] = "Brake Pedal";
                break;

            case R.id.yesEngineSpeed:
                yesEngineSpeed.setVisibility(View.GONE);
                noEngineSpeed.setVisibility(View.GONE);
                resetEngineSpeed.setVisibility(View.VISIBLE);
                engineSpeedSelection.setVisibility(View.VISIBLE);
                engineSpeedSelection.setText("Yes");
                signalResponse[2] = "Yes";
                signalNames[2] = "Engine Speed";
                break;

            case R.id.yesFuelConsumed:
                yesFuelconsumed.setVisibility(View.GONE);
                noFuelconsumed.setVisibility(View.GONE);
                resetFuelconsumed.setVisibility(View.VISIBLE);
                fuelConsumedSelection.setVisibility(View.VISIBLE);
                fuelConsumedSelection.setText("Yes");
                signalResponse[3] = "Yes";
                signalNames[3] = "Fuel Consumed";
                break;

            case R.id.yesFuelLevel:
                yesFuelLevel.setVisibility(View.GONE);
                noFuelLevel.setVisibility(View.GONE);
                resetFuelLevel.setVisibility(View.VISIBLE);
                fuelLevelSelection.setVisibility(View.VISIBLE);
                fuelLevelSelection.setText("Yes");
                signalResponse[4] = "Yes";
                signalNames[4] = "Fuel Level";
                break;

            case R.id.yesHeadlamp:
                yesHeadlamps.setVisibility(View.GONE);
                noHeadlamps.setVisibility(View.GONE);
                resetHeadlamps.setVisibility(View.VISIBLE);
                headLampSelection.setVisibility(View.VISIBLE);
                headLampSelection.setText("Yes");
                signalResponse[5] = "Yes";
                signalNames[5] = "Headlamps";
                break;

            case R.id.yesHighbeam:
                yesHighbeams.setVisibility(View.GONE);
                noHighbeams.setVisibility(View.GONE);
                resetHighbeams.setVisibility(View.VISIBLE);
                highbeamSelection.setVisibility(View.VISIBLE);
                highbeamSelection.setText("Yes");
                signalResponse[6] = "Yes";
                signalNames[6] = "Highbeams";
                break;

            case R.id.yesIgnition:
                yesIgnition.setVisibility(View.GONE);
                noIgnition.setVisibility(View.GONE);
                resetIgnition.setVisibility(View.VISIBLE);
                ignitionSelection.setVisibility(View.VISIBLE);
                ignitionSelection.setText("Yes");
                signalResponse[7] = "Yes";
                signalNames[7] = "Ignition";
                break;

            case R.id.yesLat:
                yesLatitude.setVisibility(View.GONE);
                noLatitude.setVisibility(View.GONE);
                resetLatitude.setVisibility(View.VISIBLE);
                latSelection.setVisibility(View.VISIBLE);
                latSelection.setText("Yes");
                signalResponse[8] = "Yes";
                signalNames[8] = "Latitude";
                break;

            case R.id.yesLong:
                yesLongitude.setVisibility(View.GONE);
                noLongitude.setVisibility(View.GONE);
                resetLongitude.setVisibility(View.VISIBLE);
                longSelection.setVisibility(View.VISIBLE);
                longSelection.setText("Yes");
                signalResponse[9] = "Yes";
                signalNames[9] = "Longitude";
                break;

            case R.id.yesOdometer:
                yesOdometer.setVisibility(View.GONE);
                noOdometer.setVisibility(View.GONE);
                resetOdometer.setVisibility(View.VISIBLE);
                odometerSelection.setVisibility(View.VISIBLE);
                odometerSelection.setText("Yes");
                signalResponse[10] = "Yes";
                signalNames[10] = "Odometer";
                break;

            case R.id.yesParkingBrake:
                yesParkingBrake.setVisibility(View.GONE);
                noParkingBrake.setVisibility(View.GONE);
                resetParkingBrake.setVisibility(View.VISIBLE);
                parkingBrakeSelection.setVisibility(View.VISIBLE);
                parkingBrakeSelection.setText("Yes");
                signalResponse[11] = "Yes";
                signalNames[11] = "Parking Brake";
                break;

            case R.id.yesSteerAngle:
                yesSteeringAngle.setVisibility(View.GONE);
                noSteeringAngle.setVisibility(View.GONE);
                resetSteeringAngle.setVisibility(View.VISIBLE);
                steerAngleSelection.setVisibility(View.VISIBLE);
                steerAngleSelection.setText("Yes");
                signalResponse[12] = "Yes";
                signalNames[12] = "Steering Wheel Angle";
                break;

            case R.id.yesTorque:
                yesTorque.setVisibility(View.GONE);
                noTorque.setVisibility(View.GONE);
                resetTorque.setVisibility(View.VISIBLE);
                torqueSelection.setVisibility(View.VISIBLE);
                torqueSelection.setText("Yes");
                signalResponse[13] = "Yes";
                signalNames[13] = "Trans Torque";
                break;

            case R.id.yesGear:
                yesGear.setVisibility(View.GONE);
                noGear.setVisibility(View.GONE);
                resetGear.setVisibility(View.VISIBLE);
                gearSelection.setVisibility(View.VISIBLE);
                gearSelection.setText("Yes");
                signalResponse[14] = "Yes";
                signalNames[14] = "Transmission Gear";
                break;

            case R.id.yesButton:
                yesButton.setVisibility(View.GONE);
                noButton.setVisibility(View.GONE);
                resetButton.setVisibility(View.VISIBLE);
                buttonSelection.setVisibility(View.VISIBLE);
                buttonSelection.setText("Yes");
                signalResponse[15] = "Yes";
                signalNames[15] = "Button";
                break;

            case R.id.yesDoor:
                yesDoor.setVisibility(View.GONE);
                noDoor.setVisibility(View.GONE);
                resetDoor.setVisibility(View.VISIBLE);
                doorSelection.setVisibility(View.VISIBLE);
                doorSelection.setText("Yes");
                signalResponse[16] = "Yes";
                signalNames[16] = "Door Status";
                break;

            case R.id.yesVehicleSpeed:
                yesSpeed.setVisibility(View.GONE);
                noSpeed.setVisibility(View.GONE);
                resetSpeed.setVisibility(View.VISIBLE);
                speedSelection.setVisibility(View.VISIBLE);
                speedSelection.setText("Yes");
                signalResponse[17] = "Yes";
                signalNames[17] = "Vehicle Speed";
                break;

            case R.id.yesWipers:
                yesWipers.setVisibility(View.GONE);
                noWipers.setVisibility(View.GONE);
                resetWipers.setVisibility(View.VISIBLE);
                wiperSelection.setVisibility(View.VISIBLE);
                wiperSelection.setText("Yes");
                signalResponse[18] = "Yes";
                signalNames[18] = "Wipers";
                break;

            case R.id.noAccel:
                yesAccel.setVisibility(View.GONE);
                noAccel.setVisibility(View.GONE);
                resetAccel.setVisibility(View.VISIBLE);
                accelSelection.setVisibility(View.VISIBLE);
                accelIssue.setVisibility(View.VISIBLE);
                accelSelection.setText("No");
                signalResponse[0] = "No";
                signalNames[0] = "Accelerator Pedal Position";
                break;

            case R.id.noBrake:
                yesBrake.setVisibility(View.GONE);
                noBrake.setVisibility(View.GONE);
                resetBrake.setVisibility(View.VISIBLE);
                brakeSelection.setVisibility(View.VISIBLE);
                brakeIssue.setVisibility(View.VISIBLE);
                brakeSelection.setText("No");
                signalResponse[1] = "No";
                signalNames[1] = "Brake Pedal";
                break;

            case R.id.noEngineSpeed:
                yesEngineSpeed.setVisibility(View.GONE);
                noEngineSpeed.setVisibility(View.GONE);
                resetEngineSpeed.setVisibility(View.VISIBLE);
                engineSpeedSelection.setVisibility(View.VISIBLE);
                engineSpeedIssue.setVisibility(View.VISIBLE);
                engineSpeedSelection.setText("No");
                signalResponse[2] = "No";
                signalNames[2] = "Engine Speed";
                break;

            case R.id.noFuelConsumed:
                yesFuelconsumed.setVisibility(View.GONE);
                noFuelconsumed.setVisibility(View.GONE);
                resetFuelconsumed.setVisibility(View.VISIBLE);
                fuelConsumedSelection.setVisibility(View.VISIBLE);
                fuelConsumedIssue.setVisibility(View.VISIBLE);
                fuelConsumedSelection.setText("No");
                signalResponse[3] = "No";
                signalNames[3] = "Fuel Consumed";
                break;

            case R.id.noFuelLevel:
                yesFuelLevel.setVisibility(View.GONE);
                noFuelLevel.setVisibility(View.GONE);
                resetFuelLevel.setVisibility(View.VISIBLE);
                fuelLevelSelection.setVisibility(View.VISIBLE);
                fuelLevelIssue.setVisibility(View.VISIBLE);
                fuelLevelSelection.setText("No");
                signalResponse[4] = "No";
                signalNames[4] = "Fuel Level";
                break;

            case R.id.noHeadlamp:
                yesHeadlamps.setVisibility(View.GONE);
                noHeadlamps.setVisibility(View.GONE);
                resetHeadlamps.setVisibility(View.VISIBLE);
                headLampSelection.setVisibility(View.VISIBLE);
                headLampIssue.setVisibility(View.VISIBLE);
                headLampSelection.setText("No");
                signalResponse[5] = "No";
                signalNames[5] = "Headlamps";
                break;

            case R.id.noHighbeam:
                yesHighbeams.setVisibility(View.GONE);
                noHighbeams.setVisibility(View.GONE);
                resetHighbeams.setVisibility(View.VISIBLE);
                highbeamSelection.setVisibility(View.VISIBLE);
                highbeamIssue.setVisibility(View.VISIBLE);
                highbeamSelection.setText("No");
                signalResponse[6] = "No";
                signalNames[6] = "Highbeams";
                break;

            case R.id.noIgnition:
                yesIgnition.setVisibility(View.GONE);
                noIgnition.setVisibility(View.GONE);
                resetIgnition.setVisibility(View.VISIBLE);
                ignitionSelection.setVisibility(View.VISIBLE);
                ignitionIssue.setVisibility(View.VISIBLE);
                ignitionSelection.setText("No");
                signalResponse[7] = "No";
                signalNames[7] = "Ignition";
                break;

            case R.id.noLat:
                yesLatitude.setVisibility(View.GONE);
                noLatitude.setVisibility(View.GONE);
                resetLatitude.setVisibility(View.VISIBLE);
                latSelection.setVisibility(View.VISIBLE);
                latIssue.setVisibility(View.VISIBLE);
                latSelection.setText("No");
                signalResponse[8] = "No";
                signalNames[8] = "Latitude";
                break;

            case R.id.noLong:
                yesLongitude.setVisibility(View.GONE);
                noLongitude.setVisibility(View.GONE);
                resetLongitude.setVisibility(View.VISIBLE);
                longSelection.setVisibility(View.VISIBLE);
                longIssue.setVisibility(View.VISIBLE);
                longSelection.setText("No");
                signalResponse[9] = "No";
                signalNames[9] = "Longitude";
                break;

            case R.id.noOdometer:
                yesOdometer.setVisibility(View.GONE);
                noOdometer.setVisibility(View.GONE);
                resetOdometer.setVisibility(View.VISIBLE);
                odometerSelection.setVisibility(View.VISIBLE);
                odometerIssue.setVisibility(View.VISIBLE);
                odometerSelection.setText("No");
                signalResponse[10] = "No";
                signalNames[10] = "Odometer";
                break;

            case R.id.noParkingBrake:
                yesParkingBrake.setVisibility(View.GONE);
                noParkingBrake.setVisibility(View.GONE);
                resetParkingBrake.setVisibility(View.VISIBLE);
                parkingBrakeSelection.setVisibility(View.VISIBLE);
                parkingBrakeIssue.setVisibility(View.VISIBLE);
                parkingBrakeSelection.setText("No");
                signalResponse[11] = "No";
                signalNames[11] = "Parking Brake";
                break;

            case R.id.noSteerAngle:
                yesSteeringAngle.setVisibility(View.GONE);
                noSteeringAngle.setVisibility(View.GONE);
                resetSteeringAngle.setVisibility(View.VISIBLE);
                steerAngleSelection.setVisibility(View.VISIBLE);
                steerAngleIssue.setVisibility(View.VISIBLE);
                steerAngleSelection.setText("No");
                signalResponse[12] = "No";
                signalNames[12] = "Steering Wheel Angle";
                break;

            case R.id.noTorque:
                yesTorque.setVisibility(View.GONE);
                noTorque.setVisibility(View.GONE);
                resetTorque.setVisibility(View.VISIBLE);
                torqueSelection.setVisibility(View.VISIBLE);
                torqueIssue.setVisibility(View.VISIBLE);
                torqueSelection.setText("No");
                signalResponse[13] = "No";
                signalNames[13] = "Trans Torque";
                break;

            case R.id.noGear:
                yesGear.setVisibility(View.GONE);
                noGear.setVisibility(View.GONE);
                resetGear.setVisibility(View.VISIBLE);
                gearSelection.setVisibility(View.VISIBLE);
                gearIssue.setVisibility(View.VISIBLE);
                gearSelection.setText("No");
                signalResponse[14] = "No";
                signalNames[14] = "Transmission Gear";
                break;

            case R.id.noButton:
                yesButton.setVisibility(View.GONE);
                noButton.setVisibility(View.GONE);
                resetButton.setVisibility(View.VISIBLE);
                buttonSelection.setVisibility(View.VISIBLE);
                buttonIssue.setVisibility(View.VISIBLE);
                buttonSelection.setText("No");
                signalResponse[15] = "No";
                signalNames[15] = "Button";
                break;

            case R.id.noDoor:
                yesDoor.setVisibility(View.GONE);
                noDoor.setVisibility(View.GONE);
                resetDoor.setVisibility(View.VISIBLE);
                doorSelection.setVisibility(View.VISIBLE);
                doorIssue.setVisibility(View.VISIBLE);
                doorSelection.setText("No");
                signalResponse[16] = "No";
                signalNames[16] = "Door Status";
                break;

            case R.id.noVehicleSpeed:
                yesSpeed.setVisibility(View.GONE);
                noSpeed.setVisibility(View.GONE);
                resetSpeed.setVisibility(View.VISIBLE);
                speedSelection.setVisibility(View.VISIBLE);
                speedIssue.setVisibility(View.VISIBLE);
                speedSelection.setText("No");
                signalResponse[17] = "No";
                signalNames[17] = "Vehicle Speed";
                break;

            case R.id.noWipers:
                yesWipers.setVisibility(View.GONE);
                noWipers.setVisibility(View.GONE);
                resetWipers.setVisibility(View.VISIBLE);
                wiperSelection.setVisibility(View.VISIBLE);
                wiperIssue.setVisibility(View.VISIBLE);
                wiperSelection.setText("No");
                signalResponse[18] = "No";
                signalNames[18] = "Wipers";
                break;

            case R.id.resetAccel:
                yesAccel.setVisibility(View.VISIBLE);
                noAccel.setVisibility(View.VISIBLE);
                resetAccel.setVisibility(View.GONE);
                accelSelection.setVisibility(View.GONE);
                accelIssue.setVisibility(View.GONE);
                accelIssue.setText(null);
                signalResponse[0] = "Not Tested";
                signalNames[0] = "Accelerator Pedal Position";
                break;

            case R.id.resetBrake:
                yesBrake.setVisibility(View.VISIBLE);
                noBrake.setVisibility(View.VISIBLE);
                resetBrake.setVisibility(View.GONE);
                brakeSelection.setVisibility(View.GONE);
                brakeIssue.setVisibility(View.GONE);
                brakeIssue.setText(null);
                signalResponse[1] = "Not Tested";
                signalNames[1] = "Brake Pedal";
                break;

            case R.id.resetEngineSpeed:
                yesEngineSpeed.setVisibility(View.VISIBLE);
                noEngineSpeed.setVisibility(View.VISIBLE);
                resetEngineSpeed.setVisibility(View.GONE);
                engineSpeedSelection.setVisibility(View.GONE);
                engineSpeedIssue.setVisibility(View.GONE);
                engineSpeedIssue.setText(null);
                signalResponse[2] = "Not Tested";
                signalNames[2] = "Engine Speed";
                break;

            case R.id.resetFuelConsumed:
                yesFuelconsumed.setVisibility(View.VISIBLE);
                noFuelconsumed.setVisibility(View.VISIBLE);
                resetFuelconsumed.setVisibility(View.GONE);
                fuelConsumedSelection.setVisibility(View.GONE);
                fuelConsumedIssue.setVisibility(View.GONE);
                fuelConsumedIssue.setText(null);
                signalResponse[3] = "Not Tested";
                signalNames[3] = "Fuel Consumed";
                break;

            case R.id.resetFuelLevel:
                yesFuelLevel.setVisibility(View.VISIBLE);
                noFuelLevel.setVisibility(View.VISIBLE);
                resetFuelLevel.setVisibility(View.GONE);
                fuelLevelSelection.setVisibility(View.GONE);
                fuelLevelIssue.setVisibility(View.GONE);
                fuelLevelIssue.setText(null);
                signalResponse[4] = "Not Tested";
                signalNames[4] = "Fuel Level";
                break;

            case R.id.resetHeadlamp:
                yesHeadlamps.setVisibility(View.VISIBLE);
                noHeadlamps.setVisibility(View.VISIBLE);
                resetHeadlamps.setVisibility(View.GONE);
                headLampSelection.setVisibility(View.GONE);
                headLampIssue.setVisibility(View.GONE);
                headLampIssue.setText(null);
                signalResponse[5] = "Not Tested";
                signalNames[5] = "Headlamps";
                break;

            case R.id.resetHighbeam:
                yesHighbeams.setVisibility(View.VISIBLE);
                noHighbeams.setVisibility(View.VISIBLE);
                resetHighbeams.setVisibility(View.GONE);
                highbeamSelection.setVisibility(View.GONE);
                highbeamIssue.setVisibility(View.GONE);
                highbeamIssue.setText(null);
                signalResponse[6] = "Not Tested";
                signalNames[6] = "Highbeams";
                break;

            case R.id.resetIgnition:
                yesIgnition.setVisibility(View.VISIBLE);
                noIgnition.setVisibility(View.VISIBLE);
                resetIgnition.setVisibility(View.GONE);
                ignitionSelection.setVisibility(View.GONE);
                ignitionIssue.setVisibility(View.GONE);
                ignitionIssue.setText(null);
                signalResponse[7] = "Not Tested";
                signalNames[7] = "Ignition";
                break;

            case R.id.resetLat:
                yesLatitude.setVisibility(View.VISIBLE);
                noLatitude.setVisibility(View.VISIBLE);
                resetLatitude.setVisibility(View.GONE);
                latSelection.setVisibility(View.GONE);
                latIssue.setVisibility(View.GONE);
                latIssue.setText(null);
                signalResponse[8] = "Not Tested";
                signalNames[8] = "Latitude";
                break;

            case R.id.resetLong:
                yesLongitude.setVisibility(View.VISIBLE);
                noLongitude.setVisibility(View.VISIBLE);
                resetLongitude.setVisibility(View.GONE);
                longSelection.setVisibility(View.GONE);
                longIssue.setVisibility(View.GONE);
                longIssue.setText(null);
                signalResponse[9] = "Not Tested";
                signalNames[9] = "Longitude";
                break;

            case R.id.resetOdometer:
                yesOdometer.setVisibility(View.VISIBLE);
                noOdometer.setVisibility(View.VISIBLE);
                resetOdometer.setVisibility(View.GONE);
                odometerSelection.setVisibility(View.GONE);
                odometerIssue.setVisibility(View.GONE);
                odometerIssue.setText(null);
                signalResponse[10] = "Not Tested";
                signalNames[10] = "Odometer";
                break;

            case R.id.resetParkingBrake:
                yesParkingBrake.setVisibility(View.VISIBLE);
                noParkingBrake.setVisibility(View.VISIBLE);
                resetParkingBrake.setVisibility(View.GONE);
                parkingBrakeSelection.setVisibility(View.GONE);
                parkingBrakeIssue.setVisibility(View.GONE);
                parkingBrakeIssue.setText(null);
                signalResponse[11] = "Not Tested";
                signalNames[11] = "Parking Brake";
                break;

            case R.id.resetSteerAngle:
                yesSteeringAngle.setVisibility(View.VISIBLE);
                noSteeringAngle.setVisibility(View.VISIBLE);
                resetSteeringAngle.setVisibility(View.GONE);
                steerAngleSelection.setVisibility(View.GONE);
                steerAngleIssue.setVisibility(View.GONE);
                steerAngleIssue.setText(null);
                signalResponse[12] = "Not Tested";
                signalNames[12] = "Steering Wheel Angle";
                break;

            case R.id.resetTorque:
                yesTorque.setVisibility(View.VISIBLE);
                noTorque.setVisibility(View.VISIBLE);
                resetTorque.setVisibility(View.GONE);
                torqueSelection.setVisibility(View.GONE);
                torqueIssue.setVisibility(View.GONE);
                torqueIssue.setText(null);
                signalResponse[13] = "Not Tested";
                signalNames[13] = "Trans Torque";
                break;

            case R.id.resetGear:
                yesGear.setVisibility(View.VISIBLE);
                noGear.setVisibility(View.VISIBLE);
                resetGear.setVisibility(View.GONE);
                gearSelection.setVisibility(View.GONE);
                gearIssue.setVisibility(View.GONE);
                gearIssue.setText(null);
                signalResponse[14] = "Not Tested";
                signalNames[14] = "Transmission Gear";
                break;

            case R.id.resetButton:
                yesButton.setVisibility(View.VISIBLE);
                noButton.setVisibility(View.VISIBLE);
                resetButton.setVisibility(View.GONE);
                buttonSelection.setVisibility(View.GONE);
                buttonIssue.setVisibility(View.GONE);
                buttonIssue.setText(null);
                signalResponse[15] = "Not Tested";
                signalNames[15] = "Button";
                break;

            case R.id.resetDoor:
                yesDoor.setVisibility(View.VISIBLE);
                noDoor.setVisibility(View.VISIBLE);
                resetDoor.setVisibility(View.GONE);
                doorSelection.setVisibility(View.GONE);
                doorIssue.setVisibility(View.GONE);
                doorIssue.setText(null);
                signalResponse[16] = "Not Tested";
                signalNames[16] = "Door Status";
                break;

            case R.id.resetVehicleSpeed:
                yesSpeed.setVisibility(View.VISIBLE);
                noSpeed.setVisibility(View.VISIBLE);
                resetSpeed.setVisibility(View.GONE);
                speedSelection.setVisibility(View.GONE);
                speedIssue.setVisibility(View.GONE);
                speedIssue.setText(null);
                signalResponse[17] = "Not Tested";
                signalNames[17] = "Vehicle Speed";
                break;

            case R.id.resetWipers:
                yesWipers.setVisibility(View.VISIBLE);
                noWipers.setVisibility(View.VISIBLE);
                resetWipers.setVisibility(View.GONE);
                wiperSelection.setVisibility(View.GONE);
                wiperIssue.setVisibility(View.GONE);
                wiperIssue.setText(null);
                signalResponse[18] = "Not Tested";
                signalNames[18] = "Wipers";
                break;

            case R.id.saveFile: //Not a save anymore, but posts to a Google doc

                issueResponse[0] = accelIssue.getText().toString();
                issueResponse[1] = brakeIssue.getText().toString();
                issueResponse[2] = engineSpeedIssue.getText().toString();
                issueResponse[3] = fuelConsumedIssue.getText().toString();
                issueResponse[4] = fuelLevelIssue.getText().toString();
                issueResponse[5] = headLampIssue.getText().toString();
                issueResponse[6] = highbeamIssue.getText().toString();
                issueResponse[7] = ignitionIssue.getText().toString();
                issueResponse[8] = latIssue.getText().toString();
                issueResponse[9] = longIssue.getText().toString();
                issueResponse[10] = odometerIssue.getText().toString();
                issueResponse[11] = parkingBrakeIssue.getText().toString();
                issueResponse[12] = steerAngleIssue.getText().toString();
                issueResponse[13] = torqueIssue.getText().toString();
                issueResponse[14] = gearIssue.getText().toString();
                issueResponse[15] = buttonIssue.getText().toString();
                issueResponse[16] = doorIssue.getText().toString();
                issueResponse[17] = speedIssue.getText().toString();
                issueResponse[18] = wiperIssue.getText().toString();

                if(scanned == 0)
                    VIN = vinInput.getText().toString();
                else
                    VIN = vinNum.getText().toString().substring(5);

                if(modelYearSelected != null) //Making sure we dont get any null responses here
                    postArray[0] = modelYearSelected;
                else
                    postArray[0] = "No year selected";

                if(modelSelected != null)
                    postArray[1] = modelSelected;
                else
                    postArray[1] = "No model selected";

                if(version != null)
                    postArray[2] = version;
                else
                    postArray[2] = "No VI connected";

                if(deviceId != null)
                    postArray[3] = deviceId;
                else
                    postArray[3] = "No VI connected";

                if(VIN.equals(""))
                    postArray[4] = "No VIN entered";
                else
                    postArray[4] = VIN;

                for(int i=0; i<=signalNames.length-1; i++)
                {
                    //jsonFormat[i] = "{\"name\":\"" + signalNames[i] + "\",\"value\":\"" + signalResponse[i] + "\",\"issue\":\"" + issueResponse[i] + "\"}\n"; //(For email)

                    //Creating the HTML POST statement
                    if(signalNames[i] != null)
                    {
                        if(issueResponse[i].equals(""))
                            postArray[i+5] = signalResponse[i];
                        else
                            postArray[i+5] = signalResponse[i] + ": " + issueResponse[i];
                    }
                    else
                        postArray[i+5] = "Signal not tested";
                }

                if(sharedPrefs.getString("UserName", "None") != "None") {
                    postArray[24] = sharedPrefs.getString("UserName", "None");
                    postArray[25] = sharedPrefs.getString("UserEmail", "None");
                }
                else{
                    postArray[24] = "No Name Entered";
                    postArray[25] = "No Email Entered";
                }

                Set copyM = receivedMessages;
                copyM.removeAll(allowedMessages);

                if(!copyM.isEmpty()) {
                    postArray[26] = copyM.toString();
                }
                else
                    postArray[26] = "No extra signals detected";

                Context mContext = getApplicationContext();

                PostToGoogleFormTask postToGoogleFormTask = new PostToGoogleFormTask();
                postToGoogleFormTask.myContext=mContext;
                postToGoogleFormTask.execute(postArray);

                //Sends email instead of posting to google sheets
                /*StringBuilder strBuilder = new StringBuilder();
                for (int i = 0; i < jsonFormat.length; i++) {
                    strBuilder.append(jsonFormat[i]);
                }
                String jsonOutput = strBuilder.toString();*/

                /*Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"user@server.com"});
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Validation: " + modelYearSelected + " " + modelSelected);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Vehicle: " + modelYearSelected + " " + modelSelected + "\nVIN: " + VIN + "\nVI Version: " + version + "\nVI Device ID: " + deviceId + "\n\n" + jsonOutput);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);*/

                break;

            default:
                break;
        }

        // SCROLL VIEW HACK: It would not allow the user to scroll after they interacted with a text box, so this fixes that.
        ScrollView view = (ScrollView)findViewById(R.id.scrollView1);
        view.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocusFromTouch();
                return false;
            }
        });
    }

    //Getting data back from ZXing barcode scanner
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            String scanContent = scanningResult.getContents();
            //String scanFormat = scanningResult.getFormatName();//Used to get format if needed down the line
            contentTxt.setText("VIN: " + scanContent); //Shows recieved VIN
            TextView scanVin = (TextView) findViewById(R.id.vin);
            scanVin.setVisibility(View.VISIBLE);
            scanned = 1;
        }
        else{ //Nothing recieved from scanner
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No scan data received!", Toast.LENGTH_SHORT);
                toast.show();
            }
    }

    @Override
    public void onPause() {
        super.onPause();
        // When the activity goes into the background or exits, we want to make
        // sure to unbind from the service to avoid leaking memory
        if (mVehicleManager != null) {
            Log.i(TAG, "Unbinding from Vehicle Manager");
            // Remember to remove your listeners, in typical Android
            // fashion.
            mVehicleManager.removeListener(EngineSpeed.class, mSpeedListener);
            mVehicleManager.removeListener(AcceleratorPedalPosition.class, mAcceleratorPedalListener);
            mVehicleManager.removeListener(BrakePedalStatus.class, mBrakePedalListener);
            mVehicleManager.removeListener(FuelConsumed.class, mFuelConsumedListener);
            mVehicleManager.removeListener(FuelLevel.class, mFuelLevelListener);
            mVehicleManager.removeListener(Latitude.class, mLatitudeListener);
            mVehicleManager.removeListener(Longitude.class, mLongitudeListener);
            mVehicleManager.removeListener(Odometer.class, mOdometerListener);
            mVehicleManager.removeListener(SteeringWheelAngle.class, mSteeringWheelAngleListener);
            mVehicleManager.removeListener(TorqueAtTransmission.class, mTorqueAtTransmissionListener);
            mVehicleManager.removeListener(TransmissionGearPosition.class, mGearListener);
            mVehicleManager.removeListener(VehicleSpeed.class, mVehicleSpeedListener);
            mVehicleManager.removeListener(HeadlampStatus.class, mHeadlampListener);
            mVehicleManager.removeListener(HighBeamStatus.class, mHighbeamListener);
            mVehicleManager.removeListener(IgnitionStatus.class, mIgnitionListener);
            mVehicleManager.removeListener(ParkingBrakeStatus.class, mParkingBrakeListener);
            mVehicleManager.removeListener(VehicleButtonEvent.class, mVehicleButtonListener);
            mVehicleManager.removeListener(VehicleDoorStatus.class, mDoorListener);
            mVehicleManager.removeListener(WindshieldWiperStatus.class, mWiperListener);
            mVehicleManager.removeListener(SimpleVehicleMessage.class, mListener);
            mVehicleManager.removeListener(EventedSimpleVehicleMessage.class, mListener);
            unbindService(mConnection);
            mVehicleManager = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // When the activity starts up or returns from the background,
        // re-connect to the VehicleManager so we can receive updates.
        if (mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /* This is an OpenXC measurement listener object - the type is recognized
     * by the VehicleManager as something that can receive measurement updates.
     * Later in the file, we'll ask the VehicleManager to call the receive()
     * function here whenever a new EngineSpeed value arrives.
     */
    EngineSpeed.Listener mSpeedListener = new EngineSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            // When we receive a new EngineSpeed value from the car, we want to
            // update the UI to display the new value. First we cast the generic
            // Measurement back to the type we know it to be, an EngineSpeed.
            final EngineSpeed speed = (EngineSpeed) measurement;
            // In order to modify the UI, we have to make sure the code is
            // running on the "UI thread" - Google around for this, it's an
            // important concept in Android.
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    // Finally, we've got a new value and we're running on the
                    // UI thread - we set the text of the EngineSpeed view to
                    // the latest value
                    mEngineSpeedView.setText("Engine speed: "
                            + speed.getValue().intValue() + " rpm");
                }
            });
        }
    };

    AcceleratorPedalPosition.Listener mAcceleratorPedalListener = new AcceleratorPedalPosition.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final AcceleratorPedalPosition position = (AcceleratorPedalPosition) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mAcceleratorPedalView.setText("Accelerator Pedal Position: "
                            + round(position.getValue().doubleValue(),2) + "%");
                }
            });
        }
    };

    BrakePedalStatus.Listener mBrakePedalListener = new BrakePedalStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final BrakePedalStatus status = (BrakePedalStatus) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mBrakePedalView.setText("Brake Pedal: " + status);
                }
            });
        }
    };

    FuelConsumed.Listener mFuelConsumedListener = new FuelConsumed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final FuelConsumed amountConsumed = (FuelConsumed) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mFuelConsumedView.setText("Fuel Consumed: " + round((amountConsumed.getValue().doubleValue()) * 0.26, 2) + " Gal");
                }
            });
        }
    };

    FuelLevel.Listener mFuelLevelListener = new FuelConsumed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final FuelLevel level = (FuelLevel) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mFuelLevelView.setText("Fuel Level: " + round(level.getValue().doubleValue(),2) + "%");
                }
            });
        }
    };

    Latitude.Listener mLatitudeListener = new Latitude.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final Latitude latitude = (Latitude) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mLatitudeView.setText("Latitude: " + latitude.getValue().doubleValue());
                }
            });
        }
    };

    Longitude.Listener mLongitudeListener = new Longitude.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final Longitude longitude = (Longitude) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mLongitudeView.setText("Longitude: " + longitude.getValue().doubleValue());
                }
            });
        }
    };

    Odometer.Listener mOdometerListener = new Odometer.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final Odometer odometer = (Odometer) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mOdometerView.setText("Odometer: " + round((odometer.getValue().doubleValue()) * 0.62,4) + "mi");
                }
            });
        }
    };

    SteeringWheelAngle.Listener mSteeringWheelAngleListener = new SteeringWheelAngle.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final SteeringWheelAngle steeringAngle = (SteeringWheelAngle) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mSteeringWheelView.setText("Steering Wheel Angle: " + round(steeringAngle.getValue().doubleValue(),2) + "deg");
                }
            });
        }
    };

    TorqueAtTransmission.Listener mTorqueAtTransmissionListener = new TorqueAtTransmission.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final TorqueAtTransmission tourqe = (TorqueAtTransmission) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mTransmissionTorqueView.setText("Transmission Torque: " + round(tourqe.getValue().doubleValue(),2) + "Nm");
                }
            });
        }
    };

    TransmissionGearPosition.Listener mGearListener = new TransmissionGearPosition.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final TransmissionGearPosition gear = (TransmissionGearPosition) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mTransmissionGearView.setText("Gear: " + gear.getValue());
                }
            });
        }
    };

    VehicleSpeed.Listener mVehicleSpeedListener = new VehicleSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final VehicleSpeed vehicleSpeed = (VehicleSpeed) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mVehicleSpeedView.setText("Speed: " + round((vehicleSpeed.getValue().doubleValue()) * 0.62,2) + "mph");
                }
            });
        }
    };

    HeadlampStatus.Listener mHeadlampListener = new HeadlampStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final HeadlampStatus lampStatus = (HeadlampStatus) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mHeadlampView.setText("Headlamps: " + lampStatus.getValue());
                }
            });
        }
    };

    HighBeamStatus.Listener mHighbeamListener = new HighBeamStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final HighBeamStatus highBeamStatus = (HighBeamStatus) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mHighBeamView.setText("Highbeams: " + highBeamStatus.getValue());
                }
            });
        }
    };

    IgnitionStatus.Listener mIgnitionListener = new IgnitionStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final IgnitionStatus ignitionStatus = (IgnitionStatus) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mIgnitionView.setText("Ignition: " + ignitionStatus.getValue());
                }
            });
        }
    };

    ParkingBrakeStatus.Listener mParkingBrakeListener = new ParkingBrakeStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final ParkingBrakeStatus parkingBrakeStatus = (ParkingBrakeStatus) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mParkingBrakeView.setText("Parking Brake: " + parkingBrakeStatus.getValue());
                }
            });
        }
    };

    VehicleButtonEvent.Listener mVehicleButtonListener = new VehicleButtonEvent.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final VehicleButtonEvent buttonEvent = (VehicleButtonEvent) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mVehicleButtonView.setText("Button: " + buttonEvent.getValue());
                }
            });
        }
    };

    VehicleDoorStatus.Listener mDoorListener = new VehicleDoorStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final VehicleDoorStatus doorStatus = (VehicleDoorStatus) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mVehicleDoorView.setText("Door Status: " + doorStatus.getValue());
                }
            });
        }
    };

    WindshieldWiperStatus.Listener mWiperListener = new WindshieldWiperStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final WindshieldWiperStatus wiperStatus = (WindshieldWiperStatus) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mWiperView.setText("Windshield Wipers: " + wiperStatus.getValue());
                }
            });
        }
    };

    private boolean extraSignalTrip = false;
    Map<String, String> values = new HashMap<String, String>();

    VehicleMessage.Listener mListener = new VehicleMessage.Listener(){
        @Override
        public void receive(final VehicleMessage message) {
            //Adding all the received message names into an array.
            //Because it's a set, it will only add unique values.
            receivedMessages.add(message.asSimpleMessage().getName());
            //Making copies so we don't lose the originals when we removeAll()
            final Set copyOfReceivedMessages = receivedMessages;
            final Set copyOfAllowedMessages = allowedMessages;
            //If there are any left over after we remove the allowed (aka, there are extra messages),
            //it returns false
            if(!copyOfReceivedMessages.removeAll(copyOfAllowedMessages)) {
                runOnUiThread(new Runnable() {
                    public void run()
                    {
                        if(!extraSignalTrip)
                        {
                            //Effectively un-hide the extra signals panel
                            myLayout.setPanelHeight(130);
                        }
                        extraSignalTrip = true;

                        if(!allowedMessages.contains(message.asSimpleMessage().getName()))
                        {
                            if(message instanceof EventedSimpleVehicleMessage) {
                                //Create a string from the message
                                String valueToSet = ((EventedSimpleVehicleMessage) message).getName() + " " +
                                        ((EventedSimpleVehicleMessage) message).getValue() +
                                        ": " + ((EventedSimpleVehicleMessage) message).getEvent();

                                //If the values set doesn't contain the message already, add it
                                if(!values.containsKey(message.asSimpleMessage().getName())) {
                                    values.put(((EventedSimpleVehicleMessage) message).getName(),
                                            ((EventedSimpleVehicleMessage) message).getValue() +
                                                    ": " + ((EventedSimpleVehicleMessage) message).getEvent());

                                    listItems.add(valueToSet);

                                    adapter.notifyDataSetChanged();
                                //If the message is already added, we need to update the value
                                }else if(values.containsKey(message.asSimpleMessage().getName())) {
                                    String regex = message.asSimpleMessage().getName() + ".*";
                                    //Searching for the string in the set. Regex is needed since the
                                    //message and the value are in the same string.
                                    ArrayList<String> matches = getMatchingStrings(listItems, regex);
                                    //Get index location of the message
                                    int index = listItems.indexOf(matches.get(0));
                                    //Update the message
                                    listItems.set(index, valueToSet);

                                    adapter.notifyDataSetChanged();
                                }


                            }
                            else {
                                //Same as above, but for a simple messsage
                                String valueToSet = (((SimpleVehicleMessage) message).getName() + ": " +
                                        ((SimpleVehicleMessage) message).getValue());

                                if(!values.containsKey(message.asSimpleMessage().getName())) {
                                    values.put(((SimpleVehicleMessage) message).getName(),
                                            ((SimpleVehicleMessage) message).getValue().toString());

                                    listItems.add(valueToSet);

                                    adapter.notifyDataSetChanged();
                                }else if(values.containsKey(message.asSimpleMessage().getName())) {
                                    String regex = message.asSimpleMessage().getName() + ".*";
                                    ArrayList<String> matches = getMatchingStrings(listItems, regex);
                                    int index = listItems.indexOf(matches.get(0));
                                    listItems.set(index, valueToSet);

                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }

                    }
                });
            }
        }
    };

    //Regex search to look for matching messages when adding to the illegal messages list
    ArrayList<String> getMatchingStrings(ArrayList<String> list, String regex) {

        ArrayList<String> matches = new ArrayList<>();

        Pattern p = Pattern.compile(regex);

        for (String s:list) {
            if (p.matcher(s).matches()) {
                matches.add(s);
            }
        }

        return matches;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the VehicleManager service is
        // established, i.e. bound.
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            // When the VehicleManager starts up, we store a reference to it
            // here in "mVehicleManager" so we can call functions on it
            // elsewhere in our code.
            mVehicleManager = ((VehicleManager.VehicleBinder) service).getService();

            updateViInfo();

            // We want to receive updates whenever the EngineSpeed changes. We
            // have an EngineSpeed.Listener (see above, mSpeedListener) and here
            // we request that the VehicleManager call its receive() method
            // whenever the EngineSpeed changes
            mVehicleManager.addListener(EngineSpeed.class, mSpeedListener);
            allowedMessages.add("engine_speed");
            mVehicleManager.addListener(AcceleratorPedalPosition.class, mAcceleratorPedalListener);
            allowedMessages.add("accelerator_pedal_position");
            mVehicleManager.addListener(BrakePedalStatus.class, mBrakePedalListener);
            allowedMessages.add("brake_pedal_status");
            mVehicleManager.addListener(FuelConsumed.class, mFuelConsumedListener);
            allowedMessages.add("fuel_consumed_since_restart");
            mVehicleManager.addListener(FuelLevel.class, mFuelLevelListener);
            allowedMessages.add("fuel_level");
            mVehicleManager.addListener(Latitude.class, mLatitudeListener);
            allowedMessages.add("latitude");
            mVehicleManager.addListener(Longitude.class, mLongitudeListener);
            allowedMessages.add("longitude");
            mVehicleManager.addListener(Odometer.class, mOdometerListener);
            allowedMessages.add("odometer");
            mVehicleManager.addListener(SteeringWheelAngle.class, mSteeringWheelAngleListener);
            allowedMessages.add("steering_wheel_angle");
            mVehicleManager.addListener(TorqueAtTransmission.class, mTorqueAtTransmissionListener);
            allowedMessages.add("torque_at_transmission");
            mVehicleManager.addListener(TransmissionGearPosition.class, mGearListener);
            allowedMessages.add("transmission_gear_position");
            mVehicleManager.addListener(VehicleSpeed.class, mVehicleSpeedListener);
            allowedMessages.add("vehicle_speed");
            mVehicleManager.addListener(HeadlampStatus.class, mHeadlampListener);
            allowedMessages.add("headlamp_status");
            mVehicleManager.addListener(HighBeamStatus.class, mHighbeamListener);
            allowedMessages.add("high_beam_status");
            mVehicleManager.addListener(IgnitionStatus.class, mIgnitionListener);
            allowedMessages.add("ignition_status");
            mVehicleManager.addListener(ParkingBrakeStatus.class, mParkingBrakeListener);
            allowedMessages.add("parking_brake_status");
            mVehicleManager.addListener(VehicleButtonEvent.class, mVehicleButtonListener);
            allowedMessages.add("button_event");
            mVehicleManager.addListener(VehicleDoorStatus.class, mDoorListener);
            allowedMessages.add("door_status");
            mVehicleManager.addListener(WindshieldWiperStatus.class, mWiperListener);
            allowedMessages.add("windshield_wiper_status");

            mVehicleManager.addListener(SimpleVehicleMessage.class, mListener);
            mVehicleManager.addListener(EventedSimpleVehicleMessage.class, mListener);
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
        }
    };

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.starter, menu);
        return true;
    }*/
}

