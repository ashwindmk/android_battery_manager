package com.ashwin.android.batterymanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String SUB_TAG = MainActivity.class.getSimpleName();

    BroadcastReceiver batteryBroadcast;
    IntentFilter intentFilter;

    TextView batteryInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intentFilterAndBroadcast();

        batteryInfoTextView = findViewById(R.id.battery_info_text_view);

        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(v -> {
            register();
        });

        Button unregisterButton = findViewById(R.id.unregister_button);
        unregisterButton.setOnClickListener(v -> {
            unregister();
        });
    }

    private void intentFilterAndBroadcast() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        batteryBroadcast = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    // Are we charging / charged?
                    int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                            || status == BatteryManager.BATTERY_STATUS_FULL;
                    String chargeStatus = null;
                    switch (status) {
                        case BatteryManager.BATTERY_STATUS_UNKNOWN:
                            chargeStatus = "UNKNOWN";
                            break;
                        case BatteryManager.BATTERY_STATUS_CHARGING:
                            chargeStatus = "CHARGING";
                            break;
                        case BatteryManager.BATTERY_STATUS_DISCHARGING:
                            chargeStatus = "DISCHARGING";
                            break;
                        case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                            chargeStatus = "NOT_CHARGING";
                            break;
                        case BatteryManager.BATTERY_STATUS_FULL:
                            chargeStatus = "FULL";
                            break;
                    }

                    // How are we charging?
                    int chargeSourceInt = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    String chargeSource = null;
                    switch (chargeSourceInt) {
                        case BatteryManager.BATTERY_PLUGGED_USB:
                            chargeSource = "USB";
                            break;
                        case BatteryManager.BATTERY_PLUGGED_AC:
                            chargeSource = "AC";
                            break;
                        case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                            chargeSource = "WIRELESS";
                            break;
                    }

                    // Battery level
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);

                    // Voltage
                    int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);

                    // Health
                    String health = null;
                    int healthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
                    switch (healthInt) {
                        case BatteryManager.BATTERY_HEALTH_COLD:
                            health = "COLD";
                            break;
                        case BatteryManager.BATTERY_HEALTH_DEAD:
                            health = "DEAD";
                            break;
                        case BatteryManager.BATTERY_HEALTH_GOOD:
                            health = "GOOD";
                            break;
                        case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                            health = "OVERHEAT";
                            break;
                        case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                            health = "UNKNOWN";
                            break;
                        case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                            health = "OVER_VOLTAGE";
                            break;
                        case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                            health = "UNSPECIFIED_FAILURE";
                            break;
                    }

                    // Battery type/technology
                    String technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);

                    // Battery temperature
                    float temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);

                    Log.d(Constant.APP_TAG, SUB_TAG + ": level: " + level + " %");
                    Log.d(Constant.APP_TAG, SUB_TAG + ": is-charging: " + isCharging);
                    Log.d(Constant.APP_TAG, SUB_TAG + ": charge-status: " + chargeStatus);
                    Log.d(Constant.APP_TAG, SUB_TAG + ": charge-source: " + chargeSource);
                    Log.d(Constant.APP_TAG, SUB_TAG + ": voltage: " + voltage + " V");
                    Log.d(Constant.APP_TAG, SUB_TAG + ": health: " + health);
                    Log.d(Constant.APP_TAG, SUB_TAG + ": technology: " + technology);
                    Log.d(Constant.APP_TAG, SUB_TAG + ": temperature: " + temperature + " °C");

                    try {
                        JSONObject json = bundleToJson(intent.getExtras());
                        json.put(BatteryManager.EXTRA_HEALTH, health);
                        json.put(BatteryManager.EXTRA_STATUS, chargeStatus);
                        json.put(BatteryManager.EXTRA_PLUGGED, chargeSource);
                        batteryInfoTextView.setText(json.toString(2));
                    } catch (JSONException e) {
                        Log.e(Constant.APP_TAG, SUB_TAG + ": Error converting JSON to String", e);
                    }
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        //register();
    }

    private JSONObject bundleToJson(Bundle bundle) {
        JSONObject json = new JSONObject();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            try {
                // json.put(key, bundle.get(key)); see edit below
                json.put(key, JSONObject.wrap(bundle.get(key)));
            } catch(JSONException e) {
                // Handle exception here
                Log.e(Constant.APP_TAG, SUB_TAG + ": Error converting " + key + " to JSON", e);
            }
        }
        return json;
    }

    private void register() {
        registerReceiver(batteryBroadcast, intentFilter);
    }

    private void unregister() {
        unregisterReceiver(batteryBroadcast);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregister();
    }
}
