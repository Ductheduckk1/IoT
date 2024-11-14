package com.example.iot2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.eclipse.paho.client.mqttv3.*;

import java.nio.charset.StandardCharsets;

public class DeviceActivity extends AppCompatActivity {

    private Switch switch1, switch2, switch3;
    private TextView status1, status2, status3;

    private static final String MQTT_BROKER = "tcp://broker.hivemq.com:1883";
    private static final String CLIENT_ID = "AndroidClientSwtich";
    private static final String LED_TOPIC = "PTIT/iot/led";
    private static final String FAN_TOPIC = "PTIT/iot/fan";
    private static final String PUMP_TOPIC = "PTIT/iot/pump";

    private static final String ON_MSG = "1";
    private static final String OFF_MSG = "0";

    private MqttClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_manage);

        // Lấy tham chiếu tới Switch và TextView
        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
        switch3 = findViewById(R.id.switch3);

        status1 = findViewById(R.id.status1);
        status2 = findViewById(R.id.status2);
        status3 = findViewById(R.id.status3);

        //Attempt to connect
        timerHandler.postDelayed(timerRunnable, 0);

        // Thiết lập sự kiện cho Switch 1
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    status1.setTextColor(getResources().getColor(R.color.teal_700));
                    status1.setText("Đang hoạt động");
                    sendOnMessage(LED_TOPIC);
                } else {
                    status1.setTextColor(getResources().getColor(R.color.black));
                    status1.setText("Đang tắt");
                    sendOffMessage(LED_TOPIC);
                }
            }
        });

        // Thiết lập sự kiện cho Switch 2
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    status2.setTextColor(getResources().getColor(R.color.teal_700));
                    status2.setText("Đang hoạt động");
                    sendOnMessage(FAN_TOPIC);
                } else {
                    status2.setTextColor(getResources().getColor(R.color.black));
                    status2.setText("Đang tắt");
                    sendOffMessage(FAN_TOPIC);
                }
            }
        });

        // Thiết lập sự kiện cho Switch 3
        switch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    status3.setTextColor(getResources().getColor(R.color.teal_700));
                    status3.setText("Đang hoạt động");
                    sendOnMessage(PUMP_TOPIC);
                } else {
                    status3.setTextColor(getResources().getColor(R.color.black));
                    status3.setText("Đang tắt");
                    sendOffMessage(PUMP_TOPIC);
                }
            }
        });
    }

    private final Handler timerHandler = new Handler(Looper.myLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i("mqtt", "Trying to connect...");
            connectToMQTT();
            if (!mqttClient.isConnected()) {
                //Nếu vẫn chưa kết nối được thì thử lại
                timerHandler.postDelayed(this, 4000);
            } else {
                timerHandler.removeCallbacks(this);
                Log.i("mqtt", "Connection Success");

            }
        }
    };

    private void connectToMQTT() {
        try {
            mqttClient = new MqttClient(MQTT_BROKER, CLIENT_ID, null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            mqttClient.connect(options);

            mqttClient.subscribe(LED_TOPIC);
            mqttClient.subscribe(FAN_TOPIC);
            mqttClient.subscribe(PUMP_TOPIC);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    // Xử lý khi mất kết nối
                    Log.w("ConnectionStatus", "Connection lost, reconnecting...");
                    timerHandler.postDelayed(timerRunnable, 0);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
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
