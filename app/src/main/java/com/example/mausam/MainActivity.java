package com.example.mausam;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//http://api.openweathermap.org/data/2.5/weather?q=mumbai&appid=938bb3a45ec1e77e05c50788aeaaf94f

public class MainActivity extends AppCompatActivity {

    String curr_weather = "";
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
                    jsonString+=c;
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
                jsonObject = jsonArray.getJSONObject(0);
                curr_weather = jsonObject.getString("description");
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void getWeather(View view){
        String cityName = ((TextView)findViewById(R.id.inputView)).getText().toString();

        // API OpenWeather
        DownloadTask task = new DownloadTask();
        String jsonOutput = "";
        try {
            jsonOutput = task.execute("http://api.openweathermap.org/data/2.5/weather?q=" +cityName+ "&appid=938bb3a45ec1e77e05c50788aeaaf94f").get();
            task.onPostExecute(jsonOutput);
            ((TextView)findViewById(R.id.weatherView)).setText(curr_weather);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((ConstraintLayout)findViewById(R.id.weatherLayout)).setVisibility(View.INVISIBLE);
        final TextView introText = ((TextView)findViewById(R.id.introText));
        ((ImageView)findViewById(R.id.introImage)).setVisibility(View.VISIBLE);
        introText.setVisibility(View.VISIBLE);
        introText.setX(-800);
        introText.animate().translationX(5).setDuration(1000);

        //Intro starts
        new CountDownTimer(2000,2000){
            @Override
            public void onTick(long rem){
             }
            @Override
            public void onFinish() {
                ((ImageView)findViewById(R.id.introImage)).setVisibility(View.INVISIBLE);
                introText.setVisibility(View.INVISIBLE);
                ((ConstraintLayout)findViewById(R.id.weatherLayout)).setVisibility(View.VISIBLE);
            }
        }.start();
    }
}
