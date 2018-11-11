package com.rebataur.forexapp.application;

import android.app.Application;

import android.preference.PreferenceManager;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.rebataur.forexapp.R;
import com.rebataur.forexapp.utils.TypefaceUtil;
import com.rebataur.forexapp.utils.storage.LocalStorage;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class ApplicationActivity extends Application {

    public static RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();

        mRequestQueue = Volley.newRequestQueue(this);

        LocalStorage.setPrefs(PreferenceManager.getDefaultSharedPreferences(this));

        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "GoogleSans-Regular.ttf"); // font from assets: "assets/fonts/GoogleSans-Regular.ttf

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("GoogleSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
