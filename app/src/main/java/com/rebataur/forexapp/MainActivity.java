package com.rebataur.forexapp;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rebataur.forexapp.data.GraphPlotData;
import com.rebataur.forexapp.firebasemessaging.MyFirebaseMessagingService;
import com.rebataur.forexapp.utils.AjaxCall;
import com.rebataur.forexapp.utils.GraphUtil;
import com.rebataur.forexapp.utils.storage.LocalStorage;
import com.rebataur.forexapp.views.graph.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.rebataur.forexapp.firebasemessaging.MyFirebaseMessagingService.CHANNEL_1_ID;

public class MainActivity extends AppCompatActivity {

    boolean backPressed = true;

    Toolbar mToolbar;
    Spinner spinner;
    Spinner cuspinner;

    private final Handler mHandler = new Handler();
    private Runnable mTimer;
    private LineGraphSeries<DataPoint> mSeries;

    GraphView graph;
    ProgressBar pg;
    LinearLayout calc;
    boolean firstTimeGraph;

    AppCompatTextView title1, subtitle1, title2, subtitle2;

    @Override
    public void onBackPressed() {
        if (backPressed) {
            backPressed = false;
            Toast.makeText(this, "Press Back Again to Exit..", Toast.LENGTH_SHORT).show();
        } else
            finishAffinity();
    }

    public void initGraph(GraphView graph) {
        graph.getGridLabelRenderer().setGridColor(getResources().getColor(R.color.gridColor));
        graph.getGridLabelRenderer().setHighlightZeroLines(false);

        mSeries.setThickness(5);
//        series.setAnimated(true);
        mSeries.setDrawBackground(true);

        Paint mPaint = new Paint();
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(15);
        mPaint.setColor(getResources().getColor(R.color.colorAccent));

        mSeries.setCustomPaint(mPaint);

        graph.getViewport().setScrollable(true);
        graph.addSeries(mSeries);
    }


    @Override
    protected void onResume() {
        super.onResume();
        resumeGraph();
    }

    int index = 0;
    boolean onLast = true;
    boolean block = false;

    public void resumeGraph() {
        if (list != null) {
            mTimer = new Runnable() {
                @Override
                public void run() {
                    if (!block) {
                        if (firstTimeGraph) {
                            firstTimeGraph = false;
                            graph.setVisibility(View.VISIBLE);
                            pg.setVisibility(View.INVISIBLE);
                            calc.setVisibility(View.VISIBLE);
                        }
                        if (index == list.size() - 1) {
                            onLast = true;
                            index = 0;
                            mHandler.removeCallbacks(mTimer);
                        } else {
                            if (onLast) {
                                onLast = false;
                                mSeries.resetData(new DataPoint[]{
                                        new DataPoint(
                                                list.get(0).getIndex(),
                                                list.get(0).getCurrency()
                                        )
                                });
                            }
                            index++;

                            mSeries.appendData(new DataPoint(
                                    list.get(index).getIndex(),
                                    list.get(index).getCurrency()
                            ), false, list.size());

                            mHandler.postDelayed(this, 16);
                        }
                    } else {
                        onLast = true;
                        index = 0;
                        mHandler.removeCallbacks(mTimer);
                    }
                }
            };
            mHandler.postDelayed(mTimer, 700);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTimer != null)
            mHandler.removeCallbacks(mTimer);
    }

    public void makeGraph() {
        String window = (spinner.getSelectedItemPosition() == 0) ? "7" : (spinner.getSelectedItemPosition() == 1) ? "30" : "90";
        String currency = (String) cuspinner.getSelectedItem();
        block = true;

        graph.reset();
        graph.setVisibility(View.INVISIBLE);
        pg.setVisibility(View.VISIBLE);
        calc.setVisibility(View.INVISIBLE);

        firstTimeGraph = true;

        AjaxCall.sendUserConfig(currency, window, new AjaxCall.APICallback() {
            @Override
            public void apiSuccess(JSONObject jsonObject) {
                if (jsonObject.has("code")) {
                    try {
                        if (jsonObject.getString("code").equals("S")) {
                            AjaxCall.queryWindowCurrency(new AjaxCall.APICallback() {
                                @Override
                                public void apiSuccess(JSONObject jsonObject) {
                                    try {
                                        refreshGraph(jsonObject);
                                    } catch (JSONException | ParseException e) {
                                        Log.e("Failure", "in refreshQuery");
                                        Toast.makeText(MainActivity.this, "Failure in refreshQuery..", Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void apiFailure(Exception e) {
                                    Log.e("Failure", "in queryCurrency");
                                    Toast.makeText(MainActivity.this, "Failure in queryCurrency..", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Response Error in sendConfig...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failure in sendConfig..", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void apiFailure(Exception e) {
                Log.e("Failure", "in sendConfig");
                Toast.makeText(MainActivity.this, "Failure in sendConfig..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createFirebaseMessagingInit();

        mToolbar = findViewById(R.id.toolbar);
        spinner = findViewById(R.id.spinnerSelect);
        cuspinner = findViewById(R.id.spinnerLast);

        setUpToolbar();

        graph = findViewById(R.id.graph);
        pg = findViewById(R.id.pgbar);
        calc = findViewById(R.id.calculations);
        title1 = findViewById(R.id.labeled_info_title);
        title2 = findViewById(R.id.labeled_info_title2);
        subtitle1 = findViewById(R.id.labeled_info_subtitle);
        subtitle2 = findViewById(R.id.labeled_info_subtitle2);

        createMap();

        final ArrayList<String> curr = new ArrayList<>();
        curr.add("DKK");
        curr.add("TRY");
        curr.add("ISK");
        curr.add("MXN");
        curr.add("NZD");
        curr.add("NOK");
        curr.add("CZK");
        curr.add("RUB");
        curr.add("SEK");
        curr.add("BRL");
        curr.add("ZAR");
        curr.add("JPY");
        curr.add("IDR");
        curr.add("CNY");
        curr.add("MYR");
        curr.add("PLN");
        curr.add("SGD");
        curr.add("HRK");
        curr.add("RON");
        curr.add("ILS");
        curr.add("KRW");
        curr.add("BGN");
        curr.add("HKD");
        curr.add("THB");
        curr.add("PHP");
        curr.add("CHF");
        curr.add("CAD");
        curr.add("HUF");
        Collections.sort(curr);
        curr.add(0, "USD");
        curr.add(1, "EUR");
        curr.add(2, "GBP");
        curr.add(3, "AUD");

        ArrayAdapter<String> adap = new ArrayAdapter<>(this, R.layout.spinner_item, curr);
        adap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cuspinner.setAdapter(adap);

        if (!LocalStorage.getPrefs().getString("currency", "hello").equals("hello")) {
            cuspinner.setSelection(curr.indexOf(LocalStorage.getCurrentCurrency()), false);
        }
        cuspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                makeGraph();
                LocalStorage.setCurrentCurrency(curr.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final ArrayList<String> strs = new ArrayList<>();
        strs.add("Last Week");
        strs.add("Last Month");
        strs.add("Last Quater");
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, strs));

        if (!LocalStorage.getPrefs().getString("window", "hello").equals("hello")) {
            spinner.setSelection(strs.indexOf(LocalStorage.getWindow()), false);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                makeGraph();
                LocalStorage.setWindow(strs.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        makeGraph();
    }

    Map<String, String> curList;

    private void createMap() {
        curList = new HashMap<>();
        curList.put("DKK", "kr");
        curList.put("TRY", "₺");
        curList.put("ISK", "kr");
        curList.put("MXN", "$");
        curList.put("NZD", "$");
        curList.put("NOK", "kr");
        curList.put("CZK", "Kč");
        curList.put("RUB", "Br");
        curList.put("SEK", "kr");
        curList.put("BRL", "R$");
        curList.put("ZAR", "R");
        curList.put("JPY", "¥");
        curList.put("IDR", "Rp");
        curList.put("CNY", "¥");
        curList.put("MYR", "RM");
        curList.put("PLN", "zł");
        curList.put("SGD", "$");
        curList.put("HRK", "kn");
        curList.put("RON", "lei");
        curList.put("ILS", "₪");
        curList.put("KRW", "₩");
        curList.put("BGN", "лв");
        curList.put("HKD", "$");
        curList.put("THB", "฿");
        curList.put("PHP", "₱");
        curList.put("CHF", "CHF");
        curList.put("CAD", "CAD");
        curList.put("HUF", "Ft");
        curList.put("USD", "$");
        curList.put("EUR", "€");
        curList.put("GBP", "£");
        curList.put("AUD", "$");
    }


    ArrayList<GraphPlotData> list;

    @SuppressLint("SetTextI18n")
    public void refreshGraph(JSONObject json) throws JSONException, ParseException {
        list = new ArrayList<>();

        JSONArray windowValues = json.getJSONArray("inr_window_values");
        for (int i = 0; i < windowValues.length(); i++) {
            JSONObject value = windowValues.getJSONObject(i);
            list.add(
                    new GraphPlotData(
                            new SimpleDateFormat("yyyy-MM-dd").parse(value.getString("last_update")),
                            Double.parseDouble(value.getString("inr_forex_value")),
                            i + 1
                    )
            );
        }

        list = new GraphUtil().graphMultiply(list);

        mSeries = new LineGraphSeries<>(this);

        double min, max;
        min = list.get(0).getCurrency();
        max = list.get(0).getCurrency();
        for (int i = 0; i < list.size(); i++) {
            min = (list.get(i).getCurrency() < min) ? list.get(i).getCurrency() : min;
            max = (list.get(i).getCurrency() > max) ? list.get(i).getCurrency() : max;
        }

        initGraph(graph);

        graph.getGridLabelRenderer().setHorizontalAxisTitle(
                "From " + new SimpleDateFormat("MMM dd").format(list.get(0).getDate()) + " To " + new SimpleDateFormat("MMM dd").format(list.get(list.size() - 1).getDate()));
        graph.getGridLabelRenderer().setHorizontalAxisTitleTextSize(50);

        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);


        graph.getGridLabelRenderer().setHighlightZeroLines(false);
        graph.getGridLabelRenderer().setVerticalLabelsAlign(Paint.Align.LEFT);
        graph.getGridLabelRenderer().setLabelVerticalWidth(100);
        graph.getGridLabelRenderer().reloadStyles();

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(list.size() + 2);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMaxY(max + 0.5);
        graph.getViewport().setMinY(min - 0.5);

        block = false;

        title1.setText(curList.get(cuspinner.getSelectedItem()) + " " + String.format("%.3f", list.get(list.size() - 1).getCurrency()));
        subtitle1.setText(new SimpleDateFormat("MMM dd").format(list.get(list.size() - 1).getDate()));

        double perc = ((list.get(list.size() - 1).getCurrency() - max) / max) * 100;
        title2.setText(String.format("%.3f", perc) + " %");

        resumeGraph();
    }

    private void createFirebaseMessagingInit() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.e("Token", "Token: " + token);

        AjaxCall.storeFCMToken(token, new AjaxCall.APICallback() {
            @Override
            public void apiSuccess(JSONObject jsonObject) {
                try {
                    if (jsonObject.has("code") && jsonObject.getString("code").equals("S")) {
                        MyFirebaseMessagingService.notificationManager = NotificationManagerCompat.from(MainActivity.this);
                        createNotificationChannels();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void apiFailure(Exception e) {
                Toast.makeText(MainActivity.this, "FCM Issue...", Toast.LENGTH_SHORT).show();
            }
        });

//        FirebaseMessaging.getInstance().subscribeToTopic("garudaNotifications");
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Rupee Remit Optimizer",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Rupee Remit Channel");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }

    private void setUpToolbar() {
        setTitle("₹ Remit Optimizer");
        setSupportActionBar(mToolbar);
    }
}
