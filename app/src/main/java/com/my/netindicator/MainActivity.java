package com.my.netindicator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.graphics.Color;
import android.view.View;

public class MainActivity extends Activity {
    private TextView tvNetworkType, tvSignal, tvTime, tvHistory;
    private Handler handler = new Handler();
    private Runnable updater;
    private StringBuilder history = new StringBuilder();
    private String lastNetwork = "";
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(Color.parseColor("#111111"));
        main.setPadding(30, 60, 30, 30);
        main.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("TRUE NETWORK");
        title.setTextColor(Color.WHITE);
        title.setTextSize(28);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        main.addView(title);

        View line1 = new View(this);
        line1.setBackgroundColor(Color.parseColor("#FFD700"));
        line1.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2));
        main.addView(line1);

        tvNetworkType = new TextView(this);
        tvNetworkType.setText("...");
        tvNetworkType.setTextSize(80);
        tvNetworkType.setTypeface(null, android.graphics.Typeface.BOLD);
        tvNetworkType.setGravity(Gravity.CENTER);
        tvNetworkType.setPadding(0, 20, 0, 20);
        main.addView(tvNetworkType);

        View line2 = new View(this);
        line2.setBackgroundColor(Color.parseColor("#FFD700"));
        line2.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2));
        main.addView(line2);

        tvSignal = new TextView(this);
        tvSignal.setText("নেটওয়ার্ক: --");
        tvSignal.setTextColor(Color.parseColor("#00CC44"));
        tvSignal.setTextSize(18);
        tvSignal.setGravity(Gravity.CENTER);
        tvSignal.setPadding(0, 20, 0, 10);
        main.addView(tvSignal);

        tvTime = new TextView(this);
        tvTime.setText("App চলছে: 0 সেকেন্ড");
        tvTime.setTextColor(Color.parseColor("#AAAAAA"));
        tvTime.setTextSize(14);
        tvTime.setGravity(Gravity.CENTER);
        tvTime.setPadding(0, 0, 0, 20);
        main.addView(tvTime);

        View line3 = new View(this);
        line3.setBackgroundColor(Color.parseColor("#E63329"));
        line3.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2));
        main.addView(line3);

        TextView histTitle = new TextView(this);
        histTitle.setText("নেটওয়ার্ক ইতিহাস");
        histTitle.setTextColor(Color.parseColor("#FFD700"));
        histTitle.setTextSize(16);
        histTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        histTitle.setPadding(0, 15, 0, 5);
        main.addView(histTitle);

        tvHistory = new TextView(this);
        tvHistory.setText("--");
        tvHistory.setTextColor(Color.parseColor("#CCCCCC"));
        tvHistory.setTextSize(13);
        tvHistory.setPadding(0, 5, 0, 0);
        main.addView(tvHistory);

        setContentView(main);

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 0);
        } else {
            startService(new Intent(this, FloatingService.class));
        }

        updater = new Runnable() {
            public void run() {
                updateUI();
                handler.postDelayed(this, 2000);
            }
        };
        handler.post(updater);
    }

    private void updateUI() {
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        int type = tm.getDataNetworkType();
        String network = getNetworkName(type);
        int color = getNetworkColor(type);

        tvNetworkType.setText(network);
        tvNetworkType.setTextColor(color);

        if (!network.equals(lastNetwork) && !network.equals("?")) {
            String time = new java.text.SimpleDateFormat("HH:mm:ss",
                java.util.Locale.getDefault()).format(new java.util.Date());
            history.insert(0, time + " → " + network + "\n");
            lastNetwork = network;
            tvHistory.setText(history.toString());
        }

        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        long min = elapsed / 60;
        long sec = elapsed % 60;
        tvTime.setText("App চলছে: " + min + " মিনিট " + sec + " সেকেন্ড");

        tvSignal.setText("নেটওয়ার্ক: " + tm.getNetworkOperatorName());
    }

    private String getNetworkName(int type) {
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_NR: return "5G";
            case TelephonyManager.NETWORK_TYPE_LTE: return "4G";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_UMTS: return "3G";
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_GPRS: return "2G";
            default: return "?";
        }
    }

    private int getNetworkColor(int type) {
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_NR: return Color.parseColor("#00CC44");
            case TelephonyManager.NETWORK_TYPE_LTE: return Color.parseColor("#FFD700");
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_UMTS: return Color.parseColor("#FF8800");
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_GPRS: return Color.parseColor("#E63329");
            default: return Color.WHITE;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updater);
    }
}
