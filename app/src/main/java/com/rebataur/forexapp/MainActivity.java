package com.rebataur.forexapp;

import android.graphics.*;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.rebataur.forexapp.data.GraphPlotData;
import com.rebataur.forexapp.utils.AjaxCall;
import com.rebataur.forexapp.utils.GraphUtil;
import com.rebataur.forexapp.views.graph.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    boolean firstTimeGraph;

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

    public void makeGraph(){
        String window = (spinner.getSelectedItemPosition() == 0) ? "7" : (spinner.getSelectedItemPosition() == 1) ? "30" : "90";
        String currency = (String) cuspinner.getSelectedItem();
        block = true;

        graph.reset();
        graph.setVisibility(View.INVISIBLE);
        pg.setVisibility(View.VISIBLE);

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

    boolean firstSpin = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        spinner = findViewById(R.id.spinnerSelect);
        cuspinner = findViewById(R.id.spinnerLast);

        setUpToolbar();

        graph = findViewById(R.id.graph);
        pg = findViewById(R.id.pgbar);


        ArrayList<String> curr = new ArrayList<>();
        curr.add("USD");
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
        curr.add("EUR");
        curr.add("ZAR");
        curr.add("JPY");
        curr.add("IDR");
        curr.add("CNY");
        curr.add("INR");
        curr.add("MYR");
        curr.add("PLN");
        curr.add("SGD");
        curr.add("HRK");
        curr.add("RON");
        curr.add("ILS");
        curr.add("KRW");
        curr.add("AUD");
        curr.add("BGN");
        curr.add("HKD");
        curr.add("THB");
        curr.add("PHP");
        curr.add("CHF");
        curr.add("CAD");
        curr.add("HUF");
        curr.add("GBP");
        Collections.sort(curr);
        ArrayAdapter<String> adap = new ArrayAdapter<>(this, R.layout.spinner_item, curr);
        adap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cuspinner.setAdapter(adap);

        cuspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!firstSpin) {
                    makeGraph();
                }else
                    firstSpin = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayList<String> strs = new ArrayList<>();
        strs.add("Last Week");
        strs.add("Last Month");
        strs.add("Last Quater");
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, strs));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                makeGraph();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    ArrayList<GraphPlotData> list;

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
        resumeGraph();
    }

    private void setUpToolbar() {
        setTitle("Currency Info");
        setSupportActionBar(mToolbar);
    }
}
