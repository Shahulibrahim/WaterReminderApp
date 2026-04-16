package com.example.waterreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Button btnStart, btnStop, btnDrink, btnReset;
    TextView tvStatus, tvCount;
    ProgressBar progressBar;
    BarChart barChart;
    int count = 0;
    final int GOAL = 8;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        tvStatus = findViewById(R.id.tvStatus);
        btnDrink = findViewById(R.id.btnDrink);
        btnReset = findViewById(R.id.btnReset);
        tvCount = findViewById(R.id.tvCount);
        progressBar = findViewById(R.id.progressBar);

        barChart = findViewById(R.id.barChart);
        setupChart();
        prefs = getSharedPreferences("water_app", MODE_PRIVATE);

        count = prefs.getInt("count", 0);
        updateUI();

        // ✅ Button Clicks
        btnStart.setOnClickListener(v -> startReminder());
        btnStop.setOnClickListener(v -> stopReminder());

        btnDrink.setOnClickListener(v -> {
            count++;
            updateUI();
            saveData();
        });

        btnReset.setOnClickListener(v -> {
            count = 0;
            updateUI();
            saveData();
        });

        // ✅ Notification permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        createNotificationChannel();
    }

    // 📊 Chart Setup
    private void setupChart() {

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 5));
        entries.add(new BarEntry(1, 7));
        entries.add(new BarEntry(2, 3));
        entries.add(new BarEntry(3, 6));
        entries.add(new BarEntry(4, 8));
        entries.add(new BarEntry(5, 4));
        entries.add(new BarEntry(6, 2));

        BarDataSet dataSet = new BarDataSet(entries, "Water Intake");
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(days));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    // 🔁 Start Reminder
    private void startReminder() {
        try {
            Intent intent = new Intent(this, ReminderReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            long triggerTime = System.currentTimeMillis() + 10000;

            if (alarmManager != null) {
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }

            tvStatus.setText(getString(R.string.reminder_on));

        } catch (Exception e) {
            tvStatus.setText("Error");
        }
    }

    // 🛑 Stop Reminder
    private void stopReminder() {
        Intent intent = new Intent(this, ReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        tvStatus.setText(getString(R.string.reminder_off));
    }

    // 🔔 Notification Channel
    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel =
                    new android.app.NotificationChannel(
                            "WATER_CHANNEL",
                            "Water Reminder",
                            android.app.NotificationManager.IMPORTANCE_HIGH
                    );

            android.app.NotificationManager manager =
                    getSystemService(android.app.NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // 📊 Update UI
    private void updateUI() {
        progressBar.setProgress(count);

        if (count >= GOAL) {
            tvCount.setText("Goal Completed 🎉 (" + count + ")");
        } else {
            tvCount.setText(count + " Glasses");
        }
    }

    // 💾 Save Data
    private void saveData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("count", count);
        editor.apply();
    }
}