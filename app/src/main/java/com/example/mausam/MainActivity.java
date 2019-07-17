package com.example.mausam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;

import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;


//http://api.openweathermap.org/data/2.5/weather?q=mumbai&appid=938bb3a45ec1e77e05c50788aeaaf94f

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;

    // For Debug
    String placeName;
    String []showDetails = {"Invalid","Invalid","-","-","-","-","-","-","-"};

     /*
        0)Weather main
        1)Weather descprition
        2)Min Tempature (f)
        3)Max Tempature (f)
        4)humidity percentage
        5)Pressure (hPa)
        6)Wind speed (m/s)
        7)Wind direction (degrees)
        8)Current Temp (f)

        I have set location update time to 15mins and Distance to 10km
     */

     // Response for Request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,900000,10000,locationListener);
            }
        }
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
                String weatherString = jsonObject.getString("weather");
                JSONArray jsonArray = new JSONArray(weatherString);
                JSONObject mainObject = new JSONObject(jsonObject.get("main").toString());
                JSONObject weatherObject = new JSONObject(jsonArray.get(0).toString());
                JSONObject windObject = new JSONObject(jsonObject.get("wind").toString());

                showDetails[0] = weatherObject.getString("main");
                showDetails[1] = weatherObject.getString("description");
                showDetails[2] = mainObject.getString("temp_min");
                showDetails[3] = mainObject.getString("temp_max");
                showDetails[4] = mainObject.getString("humidity");
                showDetails[5] = mainObject.getString("pressure");
                showDetails[6] = windObject.getString("speed");
                showDetails[7] = windObject.getString("deg");
                showDetails[8] = mainObject.getString("temp");
                placeName = jsonObject.getString("name");
                /*for(int i=0;i<9;i++){
                    Log.i("info",showDetails[i]);
                }*/
                placeWeatherInfo();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void placeWeatherInfo(){
        ((TextView)findViewById(R.id.tempView)).setText(showDetails[8]);
        ((TextView)findViewById(R.id.weatherTitle)).setText(showDetails[0]);
        ((TextView)findViewById(R.id.locationView)).setText(placeName);
    }
    public void getWeather(Location location){
        if(location == null){
            return;
        }
        String jsonOutput = "";
        try {
            // API OpenWeatherMap
            DownloadTask task = new DownloadTask();
            jsonOutput = task.execute("http://api.openweathermap.org/data/2.5/weather?lat="+location.getLatitude()+"&lon="+location.getLongitude()+"&appid=938bb3a45ec1e77e05c50788aeaaf94f").get();
            task.onPostExecute(jsonOutput);
            task.cancel(true);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Get Hour
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH");
        final int currentTime = Integer.parseInt(simpleDateFormat.format(Calendar.getInstance().getTime()));

        // Only PortraitMode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hiding Weather Layout at Startup
        final ConstraintLayout weatherLayout = ((ConstraintLayout)findViewById(R.id.weatherLayout));
        weatherLayout.setVisibility(View.INVISIBLE);

        final TextView introText = ((TextView)findViewById(R.id.introText));
        ((ImageView)findViewById(R.id.introImage)).setVisibility(View.VISIBLE);
        introText.setVisibility(View.VISIBLE);
        introText.setX(-800);
        introText.animate().translationX(5).setDuration(1000);

        // Location Service
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                getWeather(location);
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        //Request for Location
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }else{
            getWeather(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,900000,10000,locationListener);
        }
        // Intro starts
        new CountDownTimer(2000,2000){
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
    }
}