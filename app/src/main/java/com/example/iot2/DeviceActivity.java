package com.example.iot2;

import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.CompoundButton;

public class DeviceActivity extends AppCompatActivity {

    private Switch switch1, switch2, switch3;
    private TextView status1, status2, status3;

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

        // Thiết lập sự kiện cho Switch 1
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    status1.setTextColor(getResources().getColor(R.color.teal_700));
                    status1.setText("Đang hoạt động");
                } else {
                    status1.setTextColor(getResources().getColor(R.color.black));
                    status1.setText("Đang tắt");
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
                } else {
                    status2.setTextColor(getResources().getColor(R.color.black));
                    status2.setText("Đang tắt");
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
                } else {
                    status3.setTextColor(getResources().getColor(R.color.black));
                    status3.setText("Đang tắt");
                }
            }
        });
    }
}
