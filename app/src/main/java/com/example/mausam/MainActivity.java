package com.example.mausam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

//https://api.weatherbit.io/v2.0/current?&lat=28&lon=77&key=822648bc2f54494c9af3ed8169801f76

public class MainActivity extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    ImageView iconWeather;
    // For Debug
    String placeName;
    String []showDetails = {"Invalid","Invalid","-","-","-","-","-","-","-","-"};
     /*
        0)Weather main
        1)Pressure (hPa)
        2)Wind speed (m/s)
        3)Wind direction
        4)Current Temp (c)
        5)Weather icon
        6)Visibility
        7)UV Index
        8)Air Quality Index
        9)Relative Humidity
        Location will be updated after 15secs and 10km displacement
     */

    // Response for Request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                buildlocationRequest();
                buildLocationCallback();
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
            }
        }else
            finish();
    }
    public class DownloadTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String...urls) {
            String jsonString = "";
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            try{
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data;
                data = reader.read();
                while (data != -1){
                    char c = (char) data;
                    jsonString += c;
                    data = reader.read();
                }
                return jsonString;
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return jsonString;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try{
                JSONObject jsonObject = new JSONObject(s);
                JSONArray dataArray = new JSONArray(jsonObject.getString("data"));
                jsonObject = dataArray.getJSONObject(0);
                JSONObject weatherObject = new JSONObject(jsonObject.get("weather").toString());
                showDetails[0] = weatherObject.getString("description");
                showDetails[1] = jsonObject.getString("pres");
                showDetails[2] = jsonObject.getString("wind_spd");
                showDetails[3] = jsonObject.getString("wind_cdir_full");
                showDetails[4] = jsonObject.getString("temp");
                showDetails[5] = weatherObject.getString("icon");
                showDetails[6] = jsonObject.getString("vis");
                showDetails[7] = jsonObject.getString("uv");
                showDetails[8] = jsonObject.getString("aqi");
                showDetails[9] = jsonObject.getString("rh");
                placeName = jsonObject.getString("city_name");
                /*for (int i=0;i<=9;i++){
                    Log.i("info",showDetails[i]);
                }*/
                placeWeatherInfo();
            }
            catch (Exception e){
                e.printStackTrace();
                finish();
            }
        }
    }
    public String format(String s){
        float val = Float.parseFloat(s);
        val = (float) (Math.floor(val*100))/100;
        return Float.toString(val);
    }
    public void placeWeatherInfo(){
        showDetails[4] = Integer.toString((int)Double.parseDouble(showDetails[4]));
        ((TextView)findViewById(R.id.tempView)).setText(showDetails[4]);
        ((TextView)findViewById(R.id.placeName)).setText(placeName);
        ((TextView)findViewById(R.id.weatherTitle)).setText(showDetails[0]);
        ((TextView)findViewById(R.id.windSpeed)).setText(format(showDetails[2])+" m/s");
        ((TextView)findViewById(R.id.pressure)).setText(format(showDetails[1]) + " hPa");
        ((TextView)findViewById(R.id.UVIndex)).setText(format(showDetails[7]));
        ((TextView)findViewById(R.id.airQuality)).setText(format(showDetails[8]) + " ppm");
        ((TextView)findViewById(R.id.visibility)).setText(format(showDetails[6]) + " m");
        ((TextView)findViewById(R.id.humidity)).setText(format(showDetails[9]) + "%");
        showDetails[5] = "@drawable/"+showDetails[5];
        int imageResource = getResources().getIdentifier(showDetails[5], null, getPackageName());
        Drawable drawable = ContextCompat.getDrawable(this, imageResource);
        iconWeather.setImageDrawable(drawable);
        iconWeather.setVisibility(View.VISIBLE);

    }
    public void getWeather(Location location) {
        // Log.i("Update","Updating weather deatails");
        if(location == null){
            return;
        }
      //  Log.i("info",Double.toString(location.getLatitude())+" "+location.getLongitude());
        String jsonOutput = "";
        try {
            // API WeatherBit.io
            DownloadTask task = new DownloadTask();
            jsonOutput = task.execute("https://api.weatherbit.io/v2.0/current?&lat="+location.getLatitude()+"&lon="+location.getLongitude()+"&key=822648bc2f54494c9af3ed8169801f76").get();
            task.onPostExecute(jsonOutput);
            task.cancel(true);
        }
        catch (Exception e){
            e.printStackTrace();
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Get Hour
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH");
        final int currentTime = Integer.parseInt(simpleDateFormat.format(Calendar.getInstance().getTime()));

        // Only PortraitMode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hiding Weather Layout at Startup For Splash Screen
        final ConstraintLayout weatherLayout = ((ConstraintLayout)findViewById(R.id.weatherLayout));
        weatherLayout.setVisibility(View.INVISIBLE);

        //Hiding Weather icon
        iconWeather = (ImageView) findViewById(R.id.iconWeather);
        iconWeather.setVisibility(View.INVISIBLE);

        // Splash Screen Startup
        final TextView introText = ((TextView)findViewById(R.id.introText));
        ((ImageView)findViewById(R.id.introImage)).setVisibility(View.VISIBLE);
        introText.setVisibility(View.VISIBLE);
        introText.setX(-800);
        introText.animate().translationX(5).setDuration(1000);

        // Intro starts
        new CountDownTimer(4000,4000){
            @Override
            public void onTick(long rem){
            }
            @Override
            public void onFinish() {
                ((ImageView)findViewById(R.id.introImage)).setVisibility(View.INVISIBLE);
                introText.setVisibility(View.INVISIBLE);
                weatherLayout.setVisibility(View.VISIBLE);
                if(6<=currentTime && 17>=currentTime){
                    weatherLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.daygradient));
                }else if(18<=currentTime && 19>=currentTime){
                    weatherLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.sunset_gradient));
                }else
                    weatherLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.night_gradient));
            }
        }.start();

        //Request to Access location
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }else{
            buildlocationRequest();
            buildLocationCallback();
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
        }
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
             //   Log.i("info",locationResult.toString());
                for(Location location : locationResult.getLocations()){
                    getWeather(location);
                }
            }
        };
    }

    private void buildlocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(10000);
        // Get Location updates from other app's using location services
        locationRequest.setFastestInterval(15*1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

}