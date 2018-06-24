/*
 *  Copyright 2013-2016 Amazon.com,
 *  Inc. or its affiliates. All Rights Reserved.
 *
 *  Licensed under the Amazon Software License (the "License").
 *  You may not use this file except in compliance with the
 *  License. A copy of the License is located at
 *
 *      http://aws.amazon.com/asl/
 *
 *  or in the "license" file accompanying this file. This file is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *  CONDITIONS OF ANY KIND, express or implied. See the License
 *  for the specific language governing permissions and
 *  limitations under the License.
 */

package com.uniparthenope.mysmartaquarium;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.UpdateAttributesHandler;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UserActivity extends AppCompatActivity
{
    private final String TAG="MainActivity";

    private boolean flag = true;

    private NavigationView nDrawer;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;
    private AlertDialog userDialog;
    private ProgressDialog waitDialog;
    private ListView attributesList;
    private static final String LOG_TAG=UserActivity.class.getCanonicalName();
    private Switch luce;

    Switch switchButton, switchButton1, switchButton2, switchButton3;
    TextView textView, textView2;

    // Cognito user objects
    private CognitoUser user;
    private CognitoUserSession session;
    private CognitoUserDetails details;

    // User details
    private String username;

    // To track changes to user details
    private final List<String> attributesToDelete = new ArrayList<>();

    // --- Constants to modify per your configuration ---

    // IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com
    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "xxxxxx"; //!!!
    // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // AWS IoT permissions.
    private static final String COGNITO_POOL_ID = "xxxxxxxxx";  //!!
    // Name of the AWS IoT policy to attach to a newly created certificate
    private static final String AWS_IOT_POLICY_NAME = "xxxxxxx"; //!!

    // Region of AWS IoT
    private static final Regions MY_REGION = Regions.US_WEST_2; //!!
    // Filename of KeyStore file on the filesystem
    private static final String KEYSTORE_NAME = "iot_keystore2";
    // Password for the private key in the KeyStore
    private static final String KEYSTORE_PASSWORD = "password2";
    // Certificate and key aliases in the KeyStore
    private static final String CERTIFICATE_ID = "default2";

    TextView tvTemperature;
    TextView tvHumidity;
    TextView tvLight;
    TextView tvID;
    TextView tvImmersione_1;
    TextView tvImmersione_2;
    TextView tvImmersione_media;

    Button send;
    Button btn_save;
    Button add_food;
    Spinner publish_temp;
    Spinner publish_feed;
    Spinner publish_sun;

    AWSIotClient mIotAndroidClient;
    AWSIotMqttManager mqttManager;
    String clientId;
    String keystorePath;
    String keystoreName;
    String keystorePassword;
    ImageView presence_state;

    String topic;
    String temp;
    String food;
    String sun;

    KeyStore clientKeyStore = null;
    String certificateId;
    CognitoCachingCredentialsProvider credentialsProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            topic = extras.getString("ID");
        }

        super.onCreate(savedInstanceState);
        setContentView(com.uniparthenope.mysmartaquarium.R.layout.activity_user);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set toolbar for this screen
        toolbar = (Toolbar) findViewById(com.uniparthenope.mysmartaquarium.R.id.main_toolbar);
        toolbar.setTitle("Live Aquarium");
        TextView main_title = (TextView) findViewById(com.uniparthenope.mysmartaquarium.R.id.main_toolbar_title);
        main_title.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        send = (Button) findViewById(com.uniparthenope.mysmartaquarium.R.id.send);
        send.setOnClickListener(wifi);
        publish_temp = (Spinner) findViewById(com.uniparthenope.mysmartaquarium.R.id.spinner_temp);
        publish_feed = (Spinner) findViewById(com.uniparthenope.mysmartaquarium.R.id.spinner_feed);
        publish_sun = (Spinner) findViewById(com.uniparthenope.mysmartaquarium.R.id.spinner_sun);

        btn_save = (Button) findViewById(com.uniparthenope.mysmartaquarium.R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String pub_temp = String.valueOf(publish_temp.getSelectedItem());
                String pub_feed = String.valueOf(publish_feed.getSelectedItem());
                String pub_sun = String.valueOf(publish_sun.getSelectedItem());

                try {
                    mqttManager.publishString(pub_temp + "T", "ITEM", AWSIotMqttQos.QOS0);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Publish error.", e);
                }

                try {
                    mqttManager.publishString(pub_feed + "F", "ITEM", AWSIotMqttQos.QOS0);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Publish error.", e);
                }
                try {
                    mqttManager.publishString(pub_sun + "S", "ITEM", AWSIotMqttQos.QOS0);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Publish error.", e);
                }
            }
        });

        add_food = (Button) findViewById(com.uniparthenope.mysmartaquarium.R.id.add_food);
        add_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                try {
                    mqttManager.publishString("FEED", "ITEM", AWSIotMqttQos.QOS0);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Publish error.", e);
                }
            }
        });

        tvTemperature = (TextView) findViewById(com.uniparthenope.mysmartaquarium.R.id.Temperature);
        tvHumidity = (TextView) findViewById(com.uniparthenope.mysmartaquarium.R.id.Humidity);
        tvLight = (TextView) findViewById(com.uniparthenope.mysmartaquarium.R.id.light);
        tvID = (TextView) findViewById(com.uniparthenope.mysmartaquarium.R.id.ID);
        tvImmersione_1 = (TextView) findViewById(com.uniparthenope.mysmartaquarium.R.id.immersione_1);
        tvImmersione_2 = (TextView) findViewById(com.uniparthenope.mysmartaquarium.R.id.immersione_2);
        tvImmersione_media = (TextView) findViewById(com.uniparthenope.mysmartaquarium.R.id.immersione_media);

        initIoT();
        connect();

        switchButton = (Switch) findViewById(com.uniparthenope.mysmartaquarium.R.id.switchButton);
        switchButton1 = (Switch) findViewById(com.uniparthenope.mysmartaquarium.R.id.switchButton1);
        switchButton2 = (Switch) findViewById(com.uniparthenope.mysmartaquarium.R.id.switchButton3);
        switchButton3 = (Switch) findViewById(com.uniparthenope.mysmartaquarium.R.id.switchButton4);
        textView = (TextView) findViewById(com.uniparthenope.mysmartaquarium.R.id.textView);

        switchButton.setChecked(false);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked)
            {
                if (bChecked)
                {
                    try {
                        mqttManager.publishString("PRESA2_ON", "ITEM", AWSIotMqttQos.QOS0);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Publish error.", e);
                    }
                }
                else
                {
                    try {
                        mqttManager.publishString("PRESA2_OFF", "ITEM", AWSIotMqttQos.QOS0);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Publish error.", e);
                    }
                }
            }
        });

        if (switchButton.isChecked())
        {
            try {
                mqttManager.publishString("PRESA2_ON", "ITEM", AWSIotMqttQos.QOS0);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Publish error.", e);
            }
        }
        else
        {
            try {
                mqttManager.publishString("PRESA2_OFF", "ITEM", AWSIotMqttQos.QOS0);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Publish error.", e);
            }
        }


        switchButton1.setChecked(false);
        switchButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked)
            {
                if (bChecked)
                {
                    try {
                        mqttManager.publishString("PRESA1_ON", "ITEM", AWSIotMqttQos.QOS0);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Publish error.", e);
                    }
                }
                else
                {
                    try {
                        mqttManager.publishString("PRESA1_OFF", "ITEM", AWSIotMqttQos.QOS0);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Publish error.", e);
                    }
                }
            }
        });

        if (switchButton1.isChecked())
        {
            try {
                mqttManager.publishString("PRESA1_ON", "ITEM", AWSIotMqttQos.QOS0);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Publish error.", e);
            }
        }
        else
        {
            try {
                mqttManager.publishString("PRESA1_OFF", "ITEM", AWSIotMqttQos.QOS0);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Publish error.", e);
            }
        }


        switchButton2.setChecked(false);
        switchButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked)
            {
                if (bChecked)
                {
                    try {
                        mqttManager.publishString("PRESA3_ON", "ITEM", AWSIotMqttQos.QOS0);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Publish error.", e);
                    }
                }
                else
                {
                    try {
                        mqttManager.publishString("PRESA3_OFF", "ITEM", AWSIotMqttQos.QOS0);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Publish error.", e);
                    }
                }
            }
        });

        if (switchButton2.isChecked())
        {
            try {
                mqttManager.publishString("PRESA3_ON", "ITEM", AWSIotMqttQos.QOS0);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Publish error.", e);
            }
        }
        else
        {
            try {
                mqttManager.publishString("PRESA3_OFF", "ITEM", AWSIotMqttQos.QOS0);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Publish error.", e);
            }
        }

        switchButton3.setChecked(false);
        switchButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked)
            {
                if (bChecked)
                {
                    try {
                        mqttManager.publishString("LED_ON", "ITEM", AWSIotMqttQos.QOS0);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Publish error.", e);
                    }
                }
                else
                {
                    try {
                        mqttManager.publishString("LED_OFF", "ITEM", AWSIotMqttQos.QOS0);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Publish error.", e);
                    }
                }
            }
        });

        if (switchButton3.isChecked())
        {
            try {
                mqttManager.publishString("LED_ON", "ITEM", AWSIotMqttQos.QOS0);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Publish error.", e);
            }
        }
        else
        {
            try {
                mqttManager.publishString("LED_OFF", "ITEM", AWSIotMqttQos.QOS0);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Publish error.", e);
            }
        }

    }

    public void initIoT(){
        presence_state = (ImageView)findViewById(com.uniparthenope.mysmartaquarium.R.id.presence_state);

        clientId = username+"*"+UUID.randomUUID().toString().substring(0,5);
        // Initialize the AWS Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // context
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );

        Region region = Region.getRegion(MY_REGION);

        // MQTT Client
        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_ENDPOINT);

        // Set keepalive to 10 seconds.  Will recognize disconnects more quickly but will also send
        // MQTT pings every 10 seconds.
        mqttManager.setKeepAlive(10);

        // Set Last Will and Testament for MQTT.  On an unclean disconnect (loss of connection)
        // AWS IoT will publish this message to alert other clients.
        AWSIotMqttLastWillAndTestament lwt = new AWSIotMqttLastWillAndTestament("my/lwt/topic",
                "Android client lost connection", AWSIotMqttQos.QOS0);
        mqttManager.setMqttLastWillAndTestament(lwt);

        // IoT Client (for creation of certificate if needed)
        mIotAndroidClient = new AWSIotClient(credentialsProvider);
        mIotAndroidClient.setRegion(region);

        keystorePath = getFilesDir().getPath();
        keystoreName = KEYSTORE_NAME;
        keystorePassword = KEYSTORE_PASSWORD;
        certificateId = CERTIFICATE_ID;

        // To load cert/key from keystore on filesystem
        try {
            if (AWSIotKeystoreHelper.isKeystorePresent(keystorePath, keystoreName)) {
                if (AWSIotKeystoreHelper.keystoreContainsAlias(certificateId, keystorePath,
                        keystoreName, keystorePassword)) {
                    Log.i(LOG_TAG, "Certificate " + certificateId
                            + " found in keystore - using for MQTT.");
                    // load keystore from file into memory to pass on connection
                    clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                            keystorePath, keystoreName, keystorePassword);
//                    btnConnect.setEnabled(true);
                } else {
                    Log.i(LOG_TAG, "Key/cert " + certificateId + " not found in keystore.");
                }
            } else {
                Log.i(LOG_TAG, "Keystore " + keystorePath + "/" + keystoreName + " not found.");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "An error occurred retrieving cert/key from keystore.", e);
        }

        if (clientKeyStore == null) {
            Log.i(LOG_TAG, "Cert/key was not found in keystore - creating new key and certificate.");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Create a new private key and certificate. This call
                        // creates both on the server and returns them to the
                        // device.
                        CreateKeysAndCertificateRequest createKeysAndCertificateRequest =
                                new CreateKeysAndCertificateRequest();
                        createKeysAndCertificateRequest.setSetAsActive(true);
                        final CreateKeysAndCertificateResult createKeysAndCertificateResult;
                        createKeysAndCertificateResult =
                                mIotAndroidClient.createKeysAndCertificate(createKeysAndCertificateRequest);
                        Log.i(LOG_TAG,
                                "Cert ID: " +
                                        createKeysAndCertificateResult.getCertificateId() +
                                        " created.");

                        // store in keystore for use in MQTT client
                        // saved as alias "default" so a new certificate isn't
                        // generated each run of this application
                        AWSIotKeystoreHelper.saveCertificateAndPrivateKey(certificateId,
                                createKeysAndCertificateResult.getCertificatePem(),
                                createKeysAndCertificateResult.getKeyPair().getPrivateKey(),
                                keystorePath, keystoreName, keystorePassword);

                        // load keystore from file into memory to pass on
                        // connection
                        clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                                keystorePath, keystoreName, keystorePassword);

                        // Attach a policy to the newly created certificate.
                        // This flow assumes the policy was already created in
                        // AWS IoT and we are now just attaching it to the
                        // certificate.
                        AttachPrincipalPolicyRequest policyAttachRequest =
                                new AttachPrincipalPolicyRequest();
                        policyAttachRequest.setPolicyName(AWS_IOT_POLICY_NAME);
                        policyAttachRequest.setPrincipal(createKeysAndCertificateResult
                                .getCertificateArn());
                        mIotAndroidClient.attachPrincipalPolicy(policyAttachRequest);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                btnConnect.setEnabled(true);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(LOG_TAG,
                                "Exception occurred when generating new private key and certificate.",
                                e);
                    }
                }
            }).start();

        }

    }

    private void SubscribeToTopic(String topic)
    {
        try {
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0, new AWSIotMqttNewMessageCallback()
                    {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    try {
                                        String message = new String(data, "UTF-8");
                                        Log.d(LOG_TAG, "Message arrived:");
                                        Log.d(LOG_TAG, "   Topic: " + topic);
                                        Log.d(LOG_TAG, " Message: " + message);

                                        Log.d(LOG_TAG, " Message: " + message);

                                        JSONObject json = new JSONObject(message);

                                        tvID.setText(json.getString("ID"));
                                        tvTemperature.setText(json.getString("Temperature") + " °C");
                                        tvHumidity.setText(json.getString("Humidity") + " %");
                                        tvLight.setText(json.getString("Light"));
                                        tvImmersione_1.setText(json.getString("Water Temperature 1") + " °C");
                                        tvImmersione_2.setText(json.getString("Water Temperature 2") + " °C");
                                        tvImmersione_media.setText(json.getString("Water Temperature Mean") + " °C");
                                        String termos = json.getString("Term");
                                        String opt = json.getString("Opt");
                                        String feed = json.getString("Feed");
                                        String light = json.getString("Light");
                                        String led = json.getString("Led");
                                        String temp_set = json.getString("Temp_set");
                                        String feed_set = json.getString("Feed_set");
                                        String sun_set = json.getString("Sun_set");

                                        if(flag)
                                        {
                                            if(light.equals("1"))
                                            {
                                                switchButton.setChecked(true);
                                            }
                                            else
                                            {
                                                switchButton.setChecked(false);
                                            }

                                            if(termos.equals("1"))
                                            {
                                                switchButton1.setChecked(true);
                                            }
                                            else
                                            {
                                                switchButton1.setChecked(false);
                                            }

                                            if(opt.equals("1"))
                                            {
                                                switchButton2.setChecked(true);
                                            }
                                            else
                                            {
                                                switchButton2.setChecked(false);
                                            }

                                            if(led.equals("1"))
                                            {
                                                switchButton3.setChecked(true);
                                            }
                                            else
                                            {
                                                switchButton3.setChecked(false);
                                            }

                                            String[] x = getResources().getStringArray(com.uniparthenope.mysmartaquarium.R.array.temperature);
                                            int index = Arrays.asList(x).indexOf(temp_set);
                                            publish_temp.setSelection(index);

                                            String[] x1 = getResources().getStringArray(com.uniparthenope.mysmartaquarium.R.array.feed_rations);
                                            int index1 = Arrays.asList(x1).indexOf(feed_set);
                                            publish_feed.setSelection(index1);

                                            String[] x2 = getResources().getStringArray(com.uniparthenope.mysmartaquarium.R.array.hours_sun);
                                            int index2 = Arrays.asList(x2).indexOf(sun_set);
                                            publish_sun.setSelection(index2);

                                            flag=false;
                                        }


                                    } catch (UnsupportedEncodingException e) {
                                        Log.e(LOG_TAG, "Message encoding error.", e);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                            });
                        }
                    });
        } catch (Exception e) {
            Log.e(LOG_TAG, "Subscription error.", e);
        }
    }


    private String timeSince(Date past) {
        past.setTime(past.getTime()*1000);
        String timeSince = "";
        try
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss");
            Date now = new Date();
            long seconds= TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
            long minutes=TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
            long hours=TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
            long days=TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());

            if(seconds<60)
            {
                timeSince = (seconds+" seconds ago");
            }
            else if(minutes<60)
            {
                timeSince = (minutes+" minutes ago");
            }
            else if(hours<24)
            {
                timeSince = (hours+" hours ago");
            }
            else
            {
                timeSince = (days+" days ago");
            }
        }
        catch (Exception j){
            j.printStackTrace();
        }
        return timeSince;

    }

    private void connect(){
        try {
            mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status,
                                            final Throwable throwable) {
                    Log.d(LOG_TAG, "Status = " + String.valueOf(status));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (status == AWSIotMqttClientStatus.Connecting) {
                                presence_state.setImageResource(android.R.drawable.presence_away);

                            } else if (status == AWSIotMqttClientStatus.Connected) {
                                presence_state.setImageResource(android.R.drawable.presence_online);
                                subscribeStandard();

                            } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                                if (throwable != null) {
                                    Log.e(LOG_TAG, "Connection error.", throwable);
                                }
                                presence_state.setImageResource(android.R.drawable.presence_invisible);
                            } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
                                if (throwable != null) {
                                    Log.e(LOG_TAG, "Connection error.", throwable);
                                }
                                presence_state.setImageResource(android.R.drawable.presence_offline);
                            } else {
                                presence_state.setImageResource(android.R.drawable.presence_offline);
                            }
                        }
                    });
                }
            });
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error.", e);
            Context context = getApplicationContext();
            CharSequence text = "Failed to connect to server";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }



    View.OnClickListener wifi = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(UserActivity.this, Search_WiFi.class);
            startActivity(i);
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.uniparthenope.mysmartaquarium.R.menu.activity_user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Find which menu item was selected
        int menuItem = item.getItemId();

        // Do the task
        if(menuItem == com.uniparthenope.mysmartaquarium.R.id.user_update_attribute)
        {
            showWaitDialog("Updating...");
            updateSwitches();
            closeWaitDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateSwitches(){
        connect();
//        subscribeStandard();
    }

    private void subscribeStandard(){
        SubscribeToTopic(topic);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 20:
                // Settings
                if(resultCode == RESULT_OK) {
                    boolean refresh = data.getBooleanExtra("refresh", true);
                    if (refresh) {
                        showAttributes();
                    }
                }
                break;
            case 21:
                // Verify attributes
                if(resultCode == RESULT_OK) {
                    boolean refresh = data.getBooleanExtra("refresh", true);
                    if (refresh) {
//                        showAttributes();
                    }
                }
                break;
            case 22:
                // Add attributes
                if(resultCode == RESULT_OK) {
                    boolean refresh = data.getBooleanExtra("refresh", true);
                    if (refresh) {
//                        showAttributes();
                    }
                }
                break;
        }
    }

    // Handle when the a navigation item is selected
    private void setNavDrawer() {
        nDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                performAction(item);
                return true;
            }
        });
    }

    // Perform the action for the selected navigation item
    private void performAction(MenuItem item) {
        // Close the navigation drawer
        mDrawer.closeDrawers();

        // Find which item was selected
        switch(item.getItemId())
        {
            case com.uniparthenope.mysmartaquarium.R.id.nav_user_add_attribute:
                // Add a new attribute
                addAttribute();
                break;

            case com.uniparthenope.mysmartaquarium.R.id.nav_control_home:
                // Add a new attribute
                controlHome();
                break;

            case com.uniparthenope.mysmartaquarium.R.id.nav_user_change_password:
                // Change password
                changePassword();
                break;
            case com.uniparthenope.mysmartaquarium.R.id.nav_user_verify_attribute:
                // Confirm new user
                // confirmUser();
                attributesVerification();
                break;
            case com.uniparthenope.mysmartaquarium.R.id.nav_user_settings:
                // Show user settings
                showSettings();
                break;
            case com.uniparthenope.mysmartaquarium.R.id.nav_user_sign_out:
                // Sign out from this account
                signOut();
                break;
            case com.uniparthenope.mysmartaquarium.R.id.nav_user_about:
                // For the inquisitive
                Intent aboutAppActivity = new Intent(this, AboutApp.class);
                startActivity(aboutAppActivity);
                break;
        }
    }

    // Get user details from CIP service
    private void getDetails()
    {
        AppHelper.getPool().getUser(username).getDetailsInBackground(detailsHandler);
    }

    // Show user attributes from CIP service
    private void showAttributes() {
        final UserAttributesAdapter attributesAdapter = new UserAttributesAdapter(getApplicationContext());
        final ListView attributesListView;
        attributesListView = (ListView) findViewById(com.uniparthenope.mysmartaquarium.R.id.listViewUserAttributes);
        attributesListView.setAdapter(attributesAdapter);
        attributesList = attributesListView;

        attributesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView data = (TextView) view.findViewById(com.uniparthenope.mysmartaquarium.R.id.editTextUserDetailInput);
                String attributeType = data.getHint().toString();
                String attributeValue = data.getText().toString();
                showUserDetail(attributeType, attributeValue);
            }
        });
    }

    // Update attributes
    private void updateAttribute(String attributeType, String attributeValue) {

        if(attributeType == null || attributeType.length() < 1) {
            return;
        }
        CognitoUserAttributes updatedUserAttributes = new CognitoUserAttributes();
        updatedUserAttributes.addAttribute(attributeType, attributeValue);
        Toast.makeText(getApplicationContext(), attributeType + ": " + attributeValue, Toast.LENGTH_LONG);
        showWaitDialog("Updating...");
        AppHelper.getPool().getUser(AppHelper.getCurrUser()).updateAttributesInBackground(updatedUserAttributes, updateHandler);
    }

    // Show user MFA Settings
    private void showSettings() {
        Intent userSettingsActivity = new Intent(this,SettingsActivity.class);
        startActivityForResult(userSettingsActivity, 20);
    }

    // Add a new attribute
    private void addAttribute() {
        Intent addAttrbutesActivity = new Intent(this,AddAttributeActivity.class);
        startActivityForResult(addAttrbutesActivity, 22);
    }

    // Add a new attribute
    private void controlHome()
    {
        Intent controlHomeActivity = new Intent(this,MyAccount.class);
        startActivityForResult(controlHomeActivity, 23);
    }
    // Delete attribute
    private void deleteAttribute(String attributeName) {
        showWaitDialog("Deleting...");
        List<String> attributesToDelete = new ArrayList<>();
        attributesToDelete.add(attributeName);
        AppHelper.getPool().getUser(AppHelper.getCurrUser()).deleteAttributesInBackground(attributesToDelete, deleteHandler);
    }

    // Change user password
    private void changePassword() {
        Intent changePssActivity = new Intent(this, ChangePasswordActivity.class);
        startActivity(changePssActivity);
    }

    // Verify attributes
    private void attributesVerification() {
        Intent attrbutesActivity = new Intent(this,VerifyActivity.class);
        startActivityForResult(attrbutesActivity, 21);
    }

    private void showTrustedDevices() {
        Intent trustedDevicesActivity = new Intent(this, DeviceSettings.class);
        startActivity(trustedDevicesActivity);
    }

    // Sign out user
    private void signOut() {
        user.signOut();
        exit();
    }


    GetDetailsHandler detailsHandler = new GetDetailsHandler() {
        @Override
        public void onSuccess(CognitoUserDetails cognitoUserDetails) {
            closeWaitDialog();
            // Store details in the AppHandler
            AppHelper.setUserDetails(cognitoUserDetails);
//            showAttributes();
            // Trusted devices?
            handleTrustedDevice();
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            showDialogMessage("Could not fetch user details!", AppHelper.formatException(exception), true);
        }
    };

    private void handleTrustedDevice() {
        CognitoDevice newDevice = AppHelper.getNewDevice();
        if (newDevice != null) {
            AppHelper.newDevice(null);
            trustedDeviceDialog(newDevice);
        }
    }

    private void updateDeviceStatus(CognitoDevice device) {
        device.rememberThisDeviceInBackground(trustedDeviceHandler);
    }

    private void trustedDeviceDialog(final CognitoDevice newDevice) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remember this device?");
        //final EditText input = new EditText(UserActivity.this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        //input.setLayoutParams(lp);
        //input.requestFocus();
        //builder.setView(input);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    //String newValue = input.getText().toString();
                    showWaitDialog("Remembering this device...");
                    updateDeviceStatus(newDevice);
                    userDialog.dismiss();
                } catch (Exception e) {
                    // Log failure
                }
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                } catch (Exception e) {
                    // Log failure
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    // Callback handlers

    UpdateAttributesHandler updateHandler = new UpdateAttributesHandler() {
        @Override
        public void onSuccess(List<CognitoUserCodeDeliveryDetails> attributesVerificationList) {
            // Update successful
            if(attributesVerificationList.size() > 0) {
                showDialogMessage("Updated", "The updated attributes has to be verified",  false);
            }
            getDetails();
        }

        @Override
        public void onFailure(Exception exception) {
            // Update failed
            closeWaitDialog();
            showDialogMessage("Update failed", AppHelper.formatException(exception), false);
        }
    };

    GenericHandler deleteHandler = new GenericHandler() {
        @Override
        public void onSuccess() {
            closeWaitDialog();
            // Attribute was deleted
            Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT);

            // Fetch user details from the the service
            getDetails();
        }

        @Override
        public void onFailure(Exception e) {
            closeWaitDialog();
            // Attribute delete failed
            showDialogMessage("Delete failed", AppHelper.formatException(e), false);

            // Fetch user details from the service
            getDetails();
        }
    };

    GenericHandler trustedDeviceHandler = new GenericHandler() {
        @Override
        public void onSuccess() {
            // Close wait dialog
            closeWaitDialog();
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            showDialogMessage("Failed to update device status", AppHelper.formatException(exception), true);
        }
    };

    private void showUserDetail(final String attributeType, final String attributeValue) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(attributeType);
        final EditText input = new EditText(UserActivity.this);
        input.setText(attributeValue);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        input.setLayoutParams(lp);
        input.requestFocus();
        builder.setView(input);

        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String newValue = input.getText().toString();
                    if(!newValue.equals(attributeValue)) {
                        showWaitDialog("Updating...");
                        updateAttribute(AppHelper.getSignUpFieldsC2O().get(attributeType), newValue);
                    }
                    userDialog.dismiss();
                } catch (Exception e) {
                    // Log failure
                }
            }
        }).setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    deleteAttribute(AppHelper.getSignUpFieldsC2O().get(attributeType));
                } catch (Exception e) {
                    // Log failure
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void showWaitDialog(String message) {
        closeWaitDialog();
        waitDialog = new ProgressDialog(this);
        waitDialog.setTitle(message);
        waitDialog.show();
    }

    private void showDialogMessage(String title, String body, final boolean exit) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    if(exit) {
                        exit();
                    }
                } catch (Exception e) {
                    // Log failure
                    Log.e(TAG,"Dialog dismiss failed");
                    if(exit) {
                        exit();
                    }
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void closeWaitDialog() {
        try {
            waitDialog.dismiss();
        }
        catch (Exception e) {
            //
        }
    }

    private void exit () {
        Intent intent = new Intent();
        if(username == null)
            username = "";
        intent.putExtra("name",username);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void doExit() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                UserActivity.this);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                if(username == null)
                    username = "";
                intent.putExtra("name",username);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        alertDialog.setNegativeButton("No", null);

        alertDialog.setMessage("Do you want to sign out?");
        alertDialog.setTitle(com.uniparthenope.mysmartaquarium.R.string.app_name);
        alertDialog.show();
    }
}
