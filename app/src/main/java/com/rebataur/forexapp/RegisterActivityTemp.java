package com.rebataur.forexapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.rebataur.forexapp.utils.AjaxCall;
import com.rebataur.forexapp.views.OTPEditText;
import com.scottyab.showhidepasswordedittext.ShowHidePasswordEditText;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivityTemp extends AppCompatActivity {

    public static AlertDialog alert;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText email = findViewById(R.id.textEmail);
        final ShowHidePasswordEditText password = findViewById(R.id.password);
        final EditText username = findViewById(R.id.textusername);

        FrameLayout button = findViewById(R.id.buttonReg);
        FrameLayout backButt = findViewById(R.id.buttonBack);

        backButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
                startActivity(new Intent(RegisterActivityTemp.this,LoginActivityTemp.class));
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AjaxCall.registerUser(
                        username.getText().toString(),
                        password.getText().toString(),
                        email.getText().toString(),
                        new AjaxCall.APICallback() {
                            @Override
                            public void apiSuccess(JSONObject jsonObject) {
                                try {
                                    if(((JSONObject)jsonObject.get("result")).getBoolean("registration"))
                                    {
                                        AlertDialog.Builder sheetDialog = new AlertDialog.Builder(RegisterActivityTemp.this);
                                        View sheetView =
                                                View.inflate(RegisterActivityTemp.this, R.layout.content_dialog_otp, null);

                                        final LinearLayout resendLayout = sheetView.findViewById(R.id.resendlayout);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                resendLayout.setVisibility(View.VISIBLE);
                                            }
                                        }, 15000);
                                        final OTPEditText gototp = sheetView.findViewById(R.id.custom_unique_edittext);

                                        Button check = sheetView.findViewById(R.id.check);
                                        check.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                AjaxCall.confirmRegisterUser(gototp.getEnteredText(), new AjaxCall.APICallback() {
                                                    @Override
                                                    public void apiSuccess(JSONObject jsonObject) {
                                                        try {
                                                            if(((JSONObject)jsonObject.get("result")).getString("code").equals("S")){
                                                                Toast.makeText(RegisterActivityTemp.this, "Registeration Success, Redirecting to Login Screen..", Toast.LENGTH_SHORT).show();
                                                                finishAffinity();
                                                                startActivity(new Intent(RegisterActivityTemp.this,LoginActivityTemp.class));
                                                                alert.dismiss();
                                                            }else
                                                            {
                                                                Toast.makeText(RegisterActivityTemp.this, "Enter Valid OTP", Toast.LENGTH_SHORT).show();
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                    @Override
                                                    public void apiFailure(Exception e) {
                                                        Toast.makeText(RegisterActivityTemp.this, "Wrong OTP", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        });

                                        sheetDialog.setView(sheetView);
                                        sheetDialog.setCancelable(false);
                                        sheetDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {
                                                alert.dismiss();
                                            }
                                        });

                                        alert = sheetDialog.create();
                                        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        alert.show();
                                        Toast.makeText(RegisterActivityTemp.this, "Please Check your email and confirm OTP..", Toast.LENGTH_SHORT).show();

                                    }else
                                    {
                                        Toast.makeText(RegisterActivityTemp.this, "User Already Exists, Use another username", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void apiFailure(Exception e) {
                                Toast.makeText(RegisterActivityTemp.this, "Please Check if details are entered properly", Toast.LENGTH_LONG).show();
                                Log.e("Failure in Register","YES");
                            }
                        }
                );
            }
        });
    }
}
