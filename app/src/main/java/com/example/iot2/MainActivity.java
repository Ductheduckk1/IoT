package com.example.iot2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

    private LineChart lineChart1, lineChart2, lineChart3, lineChart4;
    private LineData lineData1, lineData2, lineData3, lineData4;
    private LineDataSet lineDataSet1, lineDataSet2, lineDataSet3, lineDataset4;
    private final ArrayList<Entry> dataEntries1 = new ArrayList<>();
    private final ArrayList<Entry> dataEntries2 = new ArrayList<>();
    private final ArrayList<Entry> dataEntries3 = new ArrayList<>();
    private final ArrayList<Entry> dataEntries4 = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private static final String MQTT_BROKER = "tcp://broker.hivemq.com:1883";
    private static final String CLIENT_ID = "PtitIotCharClient";
    private static final String TEMP_TOPIC = "PTIT/iot/temp1";
    private static final String HUM_TOPIC = "PTIT/iot/hum1";
    private static final String SOIL_HUM_TOPIC = "PTIT/iot/soilMois";
    private static final String LUM_TOPIC = "PTIT/iot/lum1";

    private TextView conStatus ;

    public static MqttClient mqttClient;
    private MqttConnectOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        conStatus = findViewById(R.id.conStatus);
        conStatus.setText("X");
        // Khởi tạo ba LineChart
        lineChart1 = findViewById(R.id.lineChart1);
        lineChart2 = findViewById(R.id.lineChart2);
        lineChart3 = findViewById(R.id.lineChart3);
        lineChart4 = findViewById(R.id.lineChart4);

        // Thiết lập ba đồ thị
        setupLineChart(lineChart1, dataEntries1);
        setupLineChart(lineChart2, dataEntries2);
        setupLineChart(lineChart3, dataEntries3);
        setupLineChart(lineChart4, dataEntries4);

        // Bắt đầu yêu cầu kết nối, 4s một lần, có thể cho vào hàm riêng nhưng lười quá
        mqttClient = initClient();
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
        Button goToPlantButton = findViewById(R.id.btnPlantIdentify);

        // Thiết lập sự kiện khi nhấn nút
        goToPlantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Intent để chuyển tới DeviceActivity
                Intent intent = new Intent(MainActivity.this, PlantActivity.class);
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
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false); // Ẩn trục Y bên phải
    }

    private MqttClient initClient() {
        try {
            mqttClient = new MqttClient(MQTT_BROKER, CLIENT_ID, null);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

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
                Log.i("PayloadMessage", "PayloadMsg: "+payload+", From Topic: "+topic);
                processIncomingData(topic, payload);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Xử lý khi hoàn tất gửi tin
            }
        });

        options = new MqttConnectOptions();
        options.setCleanSession(true);

        return mqttClient;
    }

    private void connectToMQTT() {
        try {
            mqttClient.connect(options);

            if (!mqttClient.isConnected()) return;
            mqttClient.subscribe(TEMP_TOPIC);
            mqttClient.subscribe(HUM_TOPIC);
            mqttClient.subscribe(SOIL_HUM_TOPIC);
            mqttClient.subscribe(LUM_TOPIC);
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
            conStatus.setText("Đang kết nối");
            connectToMQTT();
            if (!mqttClient.isConnected()) {
                //Nếu vẫn chưa kết nối được thì thử lại
                conStatus.setText("Kết nối thất bại. Đang thử lại... ");
                timerHandler.postDelayed(this, 4000);
            } else {
                timerHandler.removeCallbacks(this);
                conStatus.setText("Kết nối thành công!");
                Log.i("mqtt", "Connection Success");

            }
        }
    };

    private void processIncomingData(String topic, String payload) {
        try {
            float value = Float.parseFloat(payload);

            ArrayList<Entry> newEntries = new ArrayList<>();
            if (topic.equals(TEMP_TOPIC)) {
                dataEntries1.add(new Entry(dataEntries1.size(), value));
                newEntries = dataEntries1;
            } else if (topic.equals(HUM_TOPIC)) {
                dataEntries2.add(new Entry(dataEntries2.size(), value));
                newEntries = dataEntries2;
            } else if (topic.equals(SOIL_HUM_TOPIC)) {
                dataEntries3.add(new Entry(dataEntries3.size(), value));
                newEntries = dataEntries3;
            } else if (topic.equals(LUM_TOPIC)) {
                dataEntries4.add(new Entry(dataEntries3.size(), value));
                newEntries = dataEntries4;
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
            case TEMP_TOPIC:
                LineDataSet dataSet1 = new LineDataSet(newEntries, "Nhiệt độ");
                lineChart1.getXAxis().setValueFormatter(new IndexAxisValueFormatter(timeLabels));
//                lineChart1.getXAxis().setValueFormatter(new XAxisTimeValueFormatter());
                lineData1 = new LineData(dataSet1);
                lineChart1.setData(lineData1);
                lineChart1.invalidate();
                break;
            case HUM_TOPIC:
                LineDataSet dataSet2 = new LineDataSet(newEntries, "Độ ẩm không khí");
                lineChart2.getXAxis().setValueFormatter(new IndexAxisValueFormatter(timeLabels));
                lineData2 = new LineData(dataSet2);
                lineChart2.setData(lineData2);
                lineChart2.invalidate();
                break;
            case SOIL_HUM_TOPIC:
                LineDataSet dataSet3 = new LineDataSet(newEntries, "Độ ẩm đất");
                lineChart3.getXAxis().setValueFormatter(new IndexAxisValueFormatter(timeLabels));
                lineData3 = new LineData(dataSet3);
                lineChart3.setData(lineData3);
                lineChart3.invalidate();
                break;
            case LUM_TOPIC:
                LineDataSet dataSet4 = new LineDataSet(newEntries, "Độ sáng");
                lineChart4.getXAxis().setValueFormatter(new IndexAxisValueFormatter(timeLabels));
                lineData4 = new LineData(dataSet4);
                lineChart4.setData(lineData4);
                lineChart4.invalidate();
                break;
        }
    }
}
