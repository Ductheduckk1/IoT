package com.example.iot2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import org.eclipse.paho.client.mqttv3.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LineChart lineChart1, lineChart2, lineChart3;
    private LineData lineData1, lineData2, lineData3;
    private LineDataSet lineDataSet1, lineDataSet2, lineDataSet3;
    private ArrayList<Entry> dataEntries1 = new ArrayList<>();
    private ArrayList<Entry> dataEntries2 = new ArrayList<>();
    private ArrayList<Entry> dataEntries3 = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());

    private static final String MQTT_BROKER = "tcp://broker.hivemq.com:1883";
    private static final String CLIENT_ID = "AndroidClientChart";
    private static final String TOPIC1 = "/PTIT_Test/p/temp1";
    private static final String TOPIC2 = "/PTIT_Test/p/hum1";
    private static final String TOPIC3 = "iot/sensor/field3";

//    private static final String TOPIC1 = "iot/sensor/field1";
//    private static final String TOPIC2 = "iot/sensor/field2";
//    private static final String TOPIC3 = "iot/sensor/field3";

    private MqttClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo ba LineChart
        lineChart1 = findViewById(R.id.lineChart1);
        lineChart2 = findViewById(R.id.lineChart2);
        lineChart3 = findViewById(R.id.lineChart3);

        // Thiết lập ba đồ thị
        setupLineChart(lineChart1, dataEntries1);
        setupLineChart(lineChart2, dataEntries2);
        setupLineChart(lineChart3, dataEntries3);

        // Bắt đầu yêu cầu kết nối, 4s một lần, có thể cho vào hàm riêng nhưng lười quá
        timerHandler.postDelayed(timerRunnable, 0);
        // Lấy tham chiếu tới Button trong MainActivity
        Button goToDeviceButton = findViewById(R.id.btnControlDevice);

        // Thiết lập sự kiện khi nhấn nút
        goToDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Intent để chuyển tới DeviceActivity
                Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupLineChart(LineChart lineChart, ArrayList<Entry> dataEntries) {
        LineDataSet lineDataSet = new LineDataSet(dataEntries, "Data from HiveMQ");
        lineDataSet.setLineWidth(2f);
        lineDataSet.setColor(getResources().getColor(android.R.color.holo_blue_light));
        lineDataSet.setCircleRadius(4f);
        lineDataSet.setCircleColor(getResources().getColor(android.R.color.holo_blue_light));
        lineDataSet.setValueTextSize(10f);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = lineChart.getAxisLeft();
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false); // Ẩn trục Y bên phải
    }

    private void connectToMQTT() {
        try {
            mqttClient = new MqttClient(MQTT_BROKER, CLIENT_ID, null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            mqttClient.connect(options);

            mqttClient.subscribe(TOPIC1);
            mqttClient.subscribe(TOPIC2);
            mqttClient.subscribe(TOPIC3);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    // Xử lý khi mất kết nối
                    Log.w("ConnectionStatus", "Connection lost, reconnecting...");
                    timerHandler.postDelayed(timerRunnable, 0);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload());
                    Log.i("PayloadMessage", payload);
                    processIncomingData(topic, payload);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Xử lý khi hoàn tất gửi tin
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //Connecting Timer
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

    private void processIncomingData(String topic, String payload) {
        try {
//            Không có json ở đây
//            JSONObject jsonObject = new JSONObject(payload);
//            float value = (float) jsonObject.getDouble("value"); // Dữ liệu từ JSON
            float value = Float.parseFloat(payload);

            ArrayList<Entry> newEntries = new ArrayList<>();
            if (topic.equals(TOPIC1)) {
                dataEntries1.add(new Entry(dataEntries1.size(), value));
                newEntries = dataEntries1;
            } else if (topic.equals(TOPIC2)) {
                dataEntries2.add(new Entry(dataEntries2.size(), value));
                newEntries = dataEntries2;
            } else if (topic.equals(TOPIC3)) {
                dataEntries3.add(new Entry(dataEntries3.size(), value));
                newEntries = dataEntries3;
            }

            final ArrayList<Entry> finalNewEntries = newEntries;
            handler.post(() -> updateChart(finalNewEntries, topic));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private ArrayList<String> timeLabels = new ArrayList<>();
    private void updateChart(ArrayList<Entry> newEntries, String topic) {
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        timeLabels.add(currentTime); // Thêm nhãn thời gian mới
        // Giới hạn số điểm dữ liệu, ví dụ giữ lại tối đa 20 điểm
        int maxDataPoints = 5;
        if (newEntries.size() > maxDataPoints) {
            newEntries = new ArrayList<>(newEntries.subList(newEntries.size() - maxDataPoints, newEntries.size()));
        }

        switch (topic) {
            case TOPIC1:
                LineDataSet dataSet1 = new LineDataSet(newEntries, "Nhiệt độ");
                lineChart1.getXAxis().setValueFormatter(new IndexAxisValueFormatter(timeLabels));
                lineData1 = new LineData(dataSet1);
                lineChart1.setData(lineData1);
                lineChart1.invalidate();
                break;
            case TOPIC2:
                LineDataSet dataSet2 = new LineDataSet(newEntries, "Độ ẩm không khí");
                lineChart2.getXAxis().setValueFormatter(new IndexAxisValueFormatter(timeLabels));
                lineData2 = new LineData(dataSet2);
                lineChart2.setData(lineData2);
                lineChart2.invalidate();
                break;
            case TOPIC3:
                LineDataSet dataSet3 = new LineDataSet(newEntries, "Độ ẩm đất");
                lineChart3.getXAxis().setValueFormatter(new IndexAxisValueFormatter(timeLabels));
                lineData3 = new LineData(dataSet3);
                lineChart3.setData(lineData3);
                lineChart3.invalidate();
                break;
        }
    }
}
