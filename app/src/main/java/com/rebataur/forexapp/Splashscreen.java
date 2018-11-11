package com.rebataur.forexapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.rebataur.forexapp.utils.storage.LocalStorage;

public class Splashscreen extends Activity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);

        int SPLASH_DISPLAY_LENGTH = 100;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!LocalStorage.getPrefs().getString("user_id", "hello").equals("hello")) {
                    startActivity(new Intent(Splashscreen.this, MainActivity.class));
                } else {
                    startActivity(new Intent(Splashscreen.this, LoginActivityTemp.class));
                }
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}