package com.rebataur.forexapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;
import com.rebataur.forexapp.utils.AjaxCall;
import com.rebataur.forexapp.utils.storage.LocalStorage;
import com.scottyab.showhidepasswordedittext.ShowHidePasswordEditText;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivityTemp extends AppCompatActivity {

    boolean backPressed = true;
    @Override
    public void onBackPressed() {
        if (backPressed) {
            backPressed = false;
            Toast.makeText(this, "Press Back Again to Exit..", Toast.LENGTH_SHORT).show();
        } else
            finishAffinity();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        final EditText email = findViewById(R.id.textEmail);
        final ShowHidePasswordEditText password = findViewById(R.id.password);
        final TextView createAccc = findViewById(R.id.createAccount);
        final TextView forgotAccc = findViewById(R.id.forgotAccount);

        FrameLayout button = findViewById(R.id.buttonLogin);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AjaxCall.login(
                        email.getText().toString(),
                        password.getText().toString(),
                        new AjaxCall.APICallback() {
                            @Override
                            public void apiSuccess(JSONObject jsonObject) {
                                if (jsonObject.has("user_id")) {
                                    try {
                                        LocalStorage.setUserID(jsonObject.getString("user_id"));
                                        LocalStorage.setToken(jsonObject.getString("token"));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    startActivity(new Intent(LoginActivityTemp.this, MainActivity.class));
                                } else {
                                    Toast.makeText(LoginActivityTemp.this, "Enter Correct Login ID / Password", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void apiFailure(Exception e) {
                                Log.e("Error in API", e.toString());
                            }
                        });
            }
        });

        createAccc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
                startActivity(new Intent(LoginActivityTemp.this, RegisterActivityTemp.class));
            }
        });

        forgotAccc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
                startActivity(new Intent(LoginActivityTemp.this, ResetActivityTemp.class));
            }
        });

    }
}
