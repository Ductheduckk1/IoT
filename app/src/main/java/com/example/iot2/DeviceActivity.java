package com.example.iot2;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.eclipse.paho.client.mqttv3.*;

import java.nio.charset.StandardCharsets;

public class DeviceActivity extends AppCompatActivity {

    private Switch ledSwitch, fanSwitch, pumpSwitch, autoSwitch;
    private TextView ledStatus, fanStatus, pumpStatus, autoStatus;

//    private static final String MQTT_BROKER = "tcp://broker.hivemq.com:1883";
//    private static final String CLIENT_ID = "PtitIotSwitchClient";
    private static final String LED_TOPIC = "PTIT/iot/led";
    private static final String FAN_TOPIC = "PTIT/iot/fan";
    private static final String PUMP_TOPIC = "PTIT/iot/pump";
    private static final String AUTO_TOPIC = "PTIT/iot/auto";

    private static final String ON_MSG = "1";
    private static final String OFF_MSG = "0";

    //Use the same Client as MainActivity
    private MqttClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_manage);

        // Lấy tham chiếu tới Switch và TextView
        ledSwitch = findViewById(R.id.switch1);
        fanSwitch = findViewById(R.id.switch2);
        pumpSwitch = findViewById(R.id.switch3);
        autoSwitch = findViewById(R.id.switch4);

        ledStatus = findViewById(R.id.status1);
        fanStatus = findViewById(R.id.status2);
        pumpStatus = findViewById(R.id.status3);
        autoStatus = findViewById(R.id.autoStatus);

        //Attempt to connect
//        timerHandler.postDelayed(timerRunnable, 0);

        mqttClient = MainActivity.mqttClient;

        // Thiết lập sự kiện cho Switch 1
        ledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ledStatus.setTextColor(getResources().getColor(R.color.teal_700));
                    ledStatus.setText("Đang hoạt động");
                    sendOnMessage(LED_TOPIC);
                } else {
                    ledStatus.setTextColor(getResources().getColor(R.color.black));
                    ledStatus.setText("Đang tắt");
                    sendOffMessage(LED_TOPIC);
                }
            }
        });

        // Thiết lập sự kiện cho Switch 2
        fanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fanStatus.setTextColor(getResources().getColor(R.color.teal_700));
                    fanStatus.setText("Đang hoạt động");
                    sendOnMessage(FAN_TOPIC);
                } else {
                    fanStatus.setTextColor(getResources().getColor(R.color.black));
                    fanStatus.setText("Đang tắt");
                    sendOffMessage(FAN_TOPIC);
                }
            }
        });

        // Thiết lập sự kiện cho Switch 3
        pumpSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pumpStatus.setTextColor(getResources().getColor(R.color.teal_700));
                    pumpStatus.setText("Đang hoạt động");
                    sendOnMessage(PUMP_TOPIC);
                } else {
                    pumpStatus.setTextColor(getResources().getColor(R.color.black));
                    pumpStatus.setText("Đang tắt");
                    sendOffMessage(PUMP_TOPIC);
                }
            }
        });

        autoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    autoStatus.setTextColor(getResources().getColor(R.color.teal_700));
                    autoStatus.setText("Đang tự động");

                    ledSwitch.setEnabled(false);
                    fanSwitch.setEnabled(false);
                    pumpSwitch.setEnabled(false);

                    sendOnMessage(AUTO_TOPIC);
                } else {
                    autoStatus.setTextColor(getResources().getColor(R.color.black));
                    autoStatus.setText("Đang không tự động");

                    ledSwitch.setEnabled(true);
                    fanSwitch.setEnabled(true);
                    pumpSwitch.setEnabled(true);

                    sendOffMessage(AUTO_TOPIC);
                }
            }
        });
    }
    
    private void sendMessage(String topic, String message) {
        try {
            mqttClient.publish(topic, new MqttMessage(message.getBytes(StandardCharsets.UTF_8)));
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendOnMessage(String topic) {
        try {
            mqttClient.publish(topic, new MqttMessage(ON_MSG.getBytes(StandardCharsets.UTF_8)));
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendOffMessage(String topic) {
        try {
            mqttClient.publish(topic, new MqttMessage(OFF_MSG.getBytes(StandardCharsets.UTF_8)));
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}
