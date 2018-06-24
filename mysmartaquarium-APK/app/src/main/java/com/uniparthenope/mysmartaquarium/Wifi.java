package com.uniparthenope.mysmartaquarium;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Wifi extends AppCompatActivity
{
    String SSID1;
    String pass1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(com.uniparthenope.mysmartaquarium.R.layout.activity_wifi);

        Button btn = (Button) findViewById(com.uniparthenope.mysmartaquarium.R.id.send_wifi);
        btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                EditText testo1 = (EditText) findViewById(com.uniparthenope.mysmartaquarium.R.id.password);
                String pass = testo1.getText().toString();
                String SSID = getIntent().getExtras().getString("ssid");
                pass1 = pass;
                SSID1 = SSID;
                String url = String.format("http://192.168.4.1/setting?ssid=" + SSID + "&pass=" + pass);
                new GetWeather().execute(url);
            }
        });
    }

    private class GetWeather extends AsyncTask<String,Void,String>{

        public GetWeather(){

        }

        @Override
        protected String doInBackground(String... strings)
        {
            String weather = "UNDEFINED";

            try{
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();

                String inputString;
                while((inputString = bufferedReader.readLine())!= null){
                    builder.append(inputString);
                }

                JSONObject topLevel = new JSONObject(builder.toString());
                JSONObject main = topLevel.getJSONObject("Success");

                weather = String.valueOf(main.getString("Success"));

                Log.v("A",weather);

                urlConnection.disconnect();
            }
            catch (IOException | JSONException e){
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(String temp)
        {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + SSID1 + "\"";
            //conf.preSharedKey = "\"" + pass1 + "\"";

            WifiManager wifiManager1 = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager1.addNetwork(conf);

            List<WifiConfiguration> list = wifiManager1.getConfiguredNetworks();
            for (WifiConfiguration i : list)
            {
                if (i.SSID != null && i.SSID.equals("\"" + SSID1 + "\""))
                {
                    wifiManager1.disconnect();
                    wifiManager1.enableNetwork(i.networkId,true);
                    wifiManager1.reconnect();

                    break;
                }
            }



            Toast.makeText(getApplicationContext(),"Congratulatio, aquarium added successfulluy!!",Toast.LENGTH_LONG).show();
            Intent i = new Intent(Wifi.this, UserActivity.class);
            startActivity(i);
        }
    }

}
