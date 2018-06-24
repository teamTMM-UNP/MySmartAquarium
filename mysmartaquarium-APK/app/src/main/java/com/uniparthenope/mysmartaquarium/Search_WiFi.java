package com.uniparthenope.mysmartaquarium;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Search_WiFi extends AppCompatActivity implements View.OnClickListener
{
    WifiManager wifi;
    ListView lv;
    TextView textStatus;
    Button buttonScan;
    int size = 0;
    List<ScanResult> results;

    String ITEM_KEY = "key";
    ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
    SimpleAdapter adapter;

    /* Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(com.uniparthenope.mysmartaquarium.R.layout.activity_search__wi_fi);

        textStatus = (TextView) findViewById(com.uniparthenope.mysmartaquarium.R.id.textStatus);
        buttonScan = (Button) findViewById(com.uniparthenope.mysmartaquarium.R.id.buttonScan);
        buttonScan.setOnClickListener(this);
        lv = (ListView)findViewById(com.uniparthenope.mysmartaquarium.R.id.list);

        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled() == false)
        {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }
        this.adapter = new SimpleAdapter(Search_WiFi.this, arraylist, com.uniparthenope.mysmartaquarium.R.layout.row, new String[] { ITEM_KEY }, new int[] { com.uniparthenope.mysmartaquarium.R.id.list_value });
        lv.setAdapter(this.adapter);

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                Toast.makeText(getApplicationContext(),"Connection to:"+ arraylist.get(position).get(ITEM_KEY) ,Toast.LENGTH_LONG).show();
                String SSID = arraylist.get(position).get(ITEM_KEY);
                Intent i = new Intent(Search_WiFi.this, Password_Acquario.class);
                i.putExtra("ssid",SSID);
                startActivity(i);
            }
        });

        registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context c, Intent intent)
            {
                results = wifi.getScanResults();
                size = results.size();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public void onClick(View view)
    {
        arraylist.clear();
        wifi.startScan();

        Toast.makeText(this, "Scanning....", Toast.LENGTH_SHORT).show();
        try
        {
            size = size - 1;
            while (size >= 0)
            {
                HashMap<String, String> item = new HashMap<String, String>();

                if(results.get(size).SSID.contains("MySmartAquarium"))
                {
                    item.put(ITEM_KEY, results.get(size).SSID);
                    arraylist.add(item);
                    adapter.notifyDataSetChanged();
                }
                size--;
            }
        }
        catch (Exception e)
        { }
    }
}
