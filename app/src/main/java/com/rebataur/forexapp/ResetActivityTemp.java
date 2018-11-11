package com.rebataur.forexapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.rebataur.forexapp.fragments.ResetEmailFragment;
import com.rebataur.forexapp.fragments.ResetPasswordFragment;
import com.rebataur.forexapp.utils.AjaxCall;
import com.rebataur.forexapp.views.OTPEditText;
import com.scottyab.showhidepasswordedittext.ShowHidePasswordEditText;
import org.json.JSONException;
import org.json.JSONObject;

public class ResetActivityTemp extends AppCompatActivity {

    public static AlertDialog alert;
    ViewPager viewPager;
    ResetPasswordFragment passfrag;
    ResetEmailFragment emaifrag;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        final FrameLayout button = findViewById(R.id.buttonReg);
        FrameLayout backButt = findViewById(R.id.buttonBack);

        viewPager = findViewById(R.id.viewpager);

        passfrag = (ResetPasswordFragment) ResetPasswordFragment.newInstance();
        emaifrag = (ResetEmailFragment) ResetEmailFragment.newInstance();


        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        backButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
                startActivity(new Intent(ResetActivityTemp.this, LoginActivityTemp.class));
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AjaxCall.resetUser(
                        emaifrag.email.getText().toString(),
                        new AjaxCall.APICallback() {
                            @Override
                            public void apiSuccess(JSONObject jsonObject) {
                                try {
                                    if (((JSONObject) jsonObject.get("result")).getBoolean("reset_confirmed")) {
                                        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);

                                        button.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (!passfrag.pass.getText().toString().trim().equals("")) {

                                                    AlertDialog.Builder sheetDialog = new AlertDialog.Builder(ResetActivityTemp.this);
                                                    View sheetView =
                                                            View.inflate(ResetActivityTemp.this, R.layout.content_dialog_otp, null);

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
                                                            AjaxCall.resetConfirmed(
                                                                    emaifrag.email.getText().toString(),
                                                                    passfrag.pass.getText().toString(),
                                                                    Integer.parseInt(gototp.getEnteredText()),
                                                                    new AjaxCall.APICallback() {
                                                                        @Override
                                                                        public void apiSuccess(JSONObject jsonObject) {
                                                                            try {
                                                                                if (((JSONObject) jsonObject.get("result")).getBoolean("reset_new_password")) {
                                                                                    Toast.makeText(ResetActivityTemp.this, "Reset Success, Redirecting to Login Screen..", Toast.LENGTH_SHORT).show();
                                                                                    finishAffinity();
                                                                                    startActivity(new Intent(ResetActivityTemp.this, LoginActivityTemp.class));
                                                                                    alert.dismiss();
                                                                                } else {
                                                                                    Toast.makeText(ResetActivityTemp.this, "Enter Valid OTP", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            } catch (JSONException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void apiFailure(Exception e) {
                                                                            Toast.makeText(ResetActivityTemp.this, "Wrong OTP", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                            );
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
                                                    Toast.makeText(ResetActivityTemp.this, "Please Check your email and confirm OTP..", Toast.LENGTH_SHORT).show();

                                                }else{
                                                    Toast.makeText(ResetActivityTemp.this, "Enter Password", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(ResetActivityTemp.this, "No User Found with that email..", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void apiFailure(Exception e) {
                                Toast.makeText(ResetActivityTemp.this, "No User Found with that email..", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override

        public Fragment getItem(int pos) {
            switch (pos) {
                case 0:
                    return emaifrag;
                case 1:
                    return passfrag;
                default:
                    return emaifrag;

            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
