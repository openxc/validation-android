package com.openxc.openxcstarter;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class PostToGoogleFormTask extends AsyncTask<String, Void, Boolean> {

    public static final MediaType FORM_DATA_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    public static final String URL="https://docs.google.com/forms/d/1aB77u5Ah4goOdHlJeCwFyHVLxdnzh3bJ0TjF2GxOyTw/formResponse";
    public static final String model_year="entry.2052543327";
    public static final String model="entry.722707547";
    public static final String vi_version="entry.775326641";
    public static final String vi_Device_id="entry.382641520";
    public static final String vin="entry.1692088013";
    public static final String accel_pedal="entry.765860982";
    public static final String brake_pedal="entry.1323979043";
    public static final String engine_speed="entry.171002715";
    public static final String fuel_consumed="entry.2131667107";
    public static final String fuel_level="entry.647485275";
    public static final String headlamps="entry.285787302";
    public static final String highbeams="entry.458265472";
    public static final String ignition="entry.673845026";
    public static final String latitude="entry.1519560731";
    public static final String longitude="entry.990320440";
    public static final String odometer="entry.1112493113";
    public static final String parking_brake="entry.193685457";
    public static final String steering_wheel_angle="entry.144966288";
    public static final String trans_torque="entry.1144108308";
    public static final String gear="entry.673959174";
    public static final String button="entry.1709748949";
    public static final String door_status="entry.566515079";
    public static final String speed="entry.1818361102";
    public static final String wipers="entry.2067736010";
    public static final String email="entry.1030344084";
    public static final String name="entry.850127133";
    public static final String extra_signals="entry.1641430869";

    public Context myContext;

    @Override
    protected Boolean doInBackground(String... dataToPost) {
        Boolean result = true;
        String modYearS = dataToPost[0];
        String modS = dataToPost[1];
        String viVersionS = dataToPost[2];
        String viDevicIdS = dataToPost[3];
        String vinS = dataToPost[4];
        String accelPedalS = dataToPost[5];
        String brakePedalS = dataToPost[6];
        String engineSpeedS = dataToPost[7];
        String fuelConsumedS = dataToPost[8];
        String fuelLevelS = dataToPost[9];
        String headlampsS = dataToPost[10];
        String highbeamsS = dataToPost[11];
        String ignitionS = dataToPost[12];
        String latS = dataToPost[13];
        String longS = dataToPost[14];
        String odoS = dataToPost[15];
        String parkingBrakeS = dataToPost[16];
        String steerAngleS = dataToPost[17];
        String torqueS = dataToPost[18];
        String gearS = dataToPost[19];
        String buttonS = dataToPost[20];
        String doorStatusS = dataToPost[21];
        String speedS = dataToPost[22];
        String wipersS = dataToPost[23];
        String nameS = dataToPost[24];
        String emailS = dataToPost[25];
        String extraSignalS = dataToPost[26];
        String postBody="";

        try {
            //all values must be URL encoded to make sure that special characters like & | ",etc.
            //do not cause problems
            postBody = model_year + "=" + URLEncoder.encode(modYearS,"UTF-8") +
                    "&" + model + "=" + URLEncoder.encode(modS,"UTF-8") +
                    "&" + vi_version + "=" + URLEncoder.encode(viVersionS,"UTF-8") +
                    "&" + vi_Device_id + "=" + URLEncoder.encode(viDevicIdS,"UTF-8")+
                    "&" + vin + "=" + URLEncoder.encode(vinS,"UTF-8")+
                    "&" + accel_pedal + "=" + URLEncoder.encode(accelPedalS,"UTF-8")+
                    "&" + brake_pedal + "=" + URLEncoder.encode(brakePedalS,"UTF-8")+
                    "&" + engine_speed + "=" + URLEncoder.encode(engineSpeedS,"UTF-8")+
                    "&" + fuel_consumed + "=" + URLEncoder.encode(fuelConsumedS,"UTF-8")+
                    "&" + fuel_level + "=" + URLEncoder.encode(fuelLevelS,"UTF-8")+
                    "&" + headlamps + "=" + URLEncoder.encode(headlampsS,"UTF-8")+
                    "&" + highbeams + "=" + URLEncoder.encode(highbeamsS,"UTF-8")+
                    "&" + ignition + "=" + URLEncoder.encode(ignitionS,"UTF-8")+
                    "&" + latitude + "=" + URLEncoder.encode(latS,"UTF-8")+
                    "&" + longitude + "=" + URLEncoder.encode(longS,"UTF-8")+
                    "&" + odometer + "=" + URLEncoder.encode(odoS,"UTF-8")+
                    "&" + parking_brake + "=" + URLEncoder.encode(parkingBrakeS,"UTF-8")+
                    "&" + steering_wheel_angle + "=" + URLEncoder.encode(steerAngleS,"UTF-8")+
                    "&" + trans_torque + "=" + URLEncoder.encode(torqueS,"UTF-8")+
                    "&" + gear + "=" + URLEncoder.encode(gearS,"UTF-8")+
                    "&" + button + "=" + URLEncoder.encode(buttonS,"UTF-8")+
                    "&" + door_status + "=" + URLEncoder.encode(doorStatusS,"UTF-8")+
                    "&" + speed + "=" + URLEncoder.encode(speedS,"UTF-8")+
                    "&" + wipers + "=" + URLEncoder.encode(wipersS,"UTF-8")+
                    "&" + extra_signals + "=" + URLEncoder.encode(extraSignalS,"UTF-8")+
                    "&" + name + "=" + URLEncoder.encode(nameS,"UTF-8")+
                    "&" + email + "=" + URLEncoder.encode(emailS,"UTF-8");

        } catch (UnsupportedEncodingException ex) {
            result=false;
        }

        try{
            //Create OkHttpClient for sending request
            OkHttpClient client = new OkHttpClient();
            //Create the request body with the help of Media Type
            RequestBody body = RequestBody.create(FORM_DATA_TYPE, postBody);
            Request request = new Request.Builder()
                    .url(URL)
                    .post(body)
                    .build();
            //Send the request
            Response response = client.newCall(request).execute();
        }catch (IOException exception){
            result=false;
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result){
        //Print Success or failure message accordingly
        Toast.makeText(myContext,result?"Data Recorded":"There was an error in sending the data. Please try again.",Toast.LENGTH_LONG).show();
    }
}
