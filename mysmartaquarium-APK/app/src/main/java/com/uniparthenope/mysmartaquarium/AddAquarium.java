package com.uniparthenope.mysmartaquarium;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class AddAquarium extends AppCompatActivity
{
    private DBHelper mydb;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(com.uniparthenope.mysmartaquarium.R.layout.activity_add_aquarium);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(com.uniparthenope.mysmartaquarium.R.id.toolbar_AddAquarium);
        toolbar.setTitle("");
        TextView title = (TextView) findViewById(com.uniparthenope.mysmartaquarium.R.id.add_aquarium__toolbar_title);
        title.setText("Register");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        mydb = new DBHelper(this);

        Button btn = (Button) findViewById(com.uniparthenope.mysmartaquarium.R.id.add_aquarium_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                EditText ID = (EditText) findViewById(com.uniparthenope.mysmartaquarium.R.id.ID_acquario);

                if(mydb.insertContact(ID.getText().toString()))
                {
                    Toast.makeText(getApplicationContext(), "done", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "not done", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });

    }
}