package com.example.iot2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private LineChart lineChart1, lineChart2, lineChart3;
    private LineData lineData1, lineData2, lineData3;
    private LineDataSet lineDataSet1, lineDataSet2, lineDataSet3;
    private ArrayList<Entry> dataEntries1 = new ArrayList<>();
    private ArrayList<Entry> dataEntries2 = new ArrayList<>();
    private ArrayList<Entry> dataEntries3 = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());

    private static final String CHANNEL_ID = "2740590";
    private static final String READ_API_KEY = "THW9FA19QUUTN846";
    private static final String THINGSPEAK_URL = "https://api.thingspeak.com/channels/" + CHANNEL_ID + "/fields/1.json?api_key=" + READ_API_KEY + "&results=10";
    private static final String THINGSPEAK_URL_2 = "https://api.thingspeak.com/channels/" + CHANNEL_ID + "/fields/2.json?api_key=" + READ_API_KEY + "&results=10";
    private static final String THINGSPEAK_URL_3 = "https://api.thingspeak.com/channels/" + CHANNEL_ID + "/fields/3.json?api_key=" + READ_API_KEY + "&results=10";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo ba LineChart
        lineChart1 = findViewById(R.id.lineChart1);
        lineChart2 = findViewById(R.id.lineChart2);
        lineChart3 = findViewById(R.id.lineChart3);

        // Thiết lập ba đồ thị
        setupLineChart(lineChart1, lineDataSet1, dataEntries1);
        setupLineChart(lineChart2, lineDataSet2, dataEntries2);
        setupLineChart(lineChart3, lineDataSet3, dataEntries3);

        // Tạo Timer để tự động cập nhật dữ liệu mỗi 10 giây
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchDataFromThingSpeak(THINGSPEAK_URL, 1);
                fetchDataFromThingSpeak(THINGSPEAK_URL_2, 2);
                fetchDataFromThingSpeak(THINGSPEAK_URL_3, 3);
            }
        }, 0, 10000); // Cập nhật mỗi 10 giây
    }

    private void setupLineChart(LineChart lineChart, LineDataSet lineDataSet, ArrayList<Entry> dataEntries) {
        lineDataSet = new LineDataSet(dataEntries, "Data from ThingSpeak");
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

    private void fetchDataFromThingSpeak(String urlStr, int chartNumber) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    reader.close();
                    urlConnection.disconnect();

                    ArrayList<Entry> newEntries = parseJSONData(result.toString());

                    // Cập nhật UI trên luồng chính
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateChart(newEntries, chartNumber);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private ArrayList<Entry> parseJSONData(String jsonString) {
        ArrayList<Entry> entries = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray feedsArray = jsonObject.getJSONArray("feeds");

            for (int i = 0; i < feedsArray.length(); i++) {
                JSONObject feed = feedsArray.getJSONObject(i);
                float value = (float) feed.getDouble("field1");
                entries.add(new Entry(i, value)); // Sử dụng i làm trục X
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entries;
    }

    private void updateChart(ArrayList<Entry> newEntries, int chartNumber) {
        switch (chartNumber) {
            case 1:
            dataEntries1.clear();
            dataEntries1.addAll(newEntries);
            lineDataSet1.notifyDataSetChanged();
            lineChart1.notifyDataSetChanged();
            lineChart1.invalidate();
            break;
            case 2:
            dataEntries2.clear();
            dataEntries2.addAll(newEntries);
            lineDataSet2.notifyDataSetChanged();
            lineChart2.notifyDataSetChanged();
            lineChart2.invalidate();
            break;
            case 3:
            dataEntries3.clear();
            dataEntries3.addAll(newEntries);
            lineDataSet3.notifyDataSetChanged();
            lineChart3.notifyDataSetChanged();
            lineChart3.invalidate();
            break;
        }
    }
}
