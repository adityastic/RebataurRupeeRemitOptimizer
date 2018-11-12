package com.rebataur.forexapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.rebataur.forexapp.utils.AjaxCall;
import com.rebataur.forexapp.utils.storage.LocalStorage;
import com.rebataur.forexapp.views.OTPEditText;
import com.scottyab.showhidepasswordedittext.ShowHidePasswordEditText;
import org.json.JSONException;
import org.json.JSONObject;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class TermsActivityTemp extends AppCompatActivity {

    public static AlertDialog alert;

    CheckBox terms, policy, allagree;
    FrameLayout button;

    public void checkShouldEnableButton() {
        button.setVisibility((terms.isChecked() && policy.isChecked()) ? VISIBLE : GONE);
        allagree.setChecked(terms.isChecked() && policy.isChecked());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        button = findViewById(R.id.buttonReg);
        button.setVisibility(GONE);
        FrameLayout backButt = findViewById(R.id.buttonBack);

        terms = findViewById(R.id.termscheck);
        policy = findViewById(R.id.policheck);
        allagree = findViewById(R.id.allcheck);

        final String user_id = getIntent().getExtras().getString("user_id");
        final String token = getIntent().getExtras().getString("token");

        terms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkShouldEnableButton();
            }
        });
        policy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkShouldEnableButton();
            }
        });
        allagree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                terms.setChecked(allagree.isChecked());
                policy.setChecked(allagree.isChecked());
                checkShouldEnableButton();
            }
        });

        TextView termstext = findViewById(R.id.termstext);
        TextView policytext = findViewById(R.id.policytext);

        termstext.setPaintFlags(termstext.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        policytext.setPaintFlags(policytext.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        termstext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder sheetDialog = new AlertDialog.Builder(TermsActivityTemp.this);
                sheetDialog.setTitle("Terms of Service");
                sheetDialog.setMessage("You agree that you will use the SERVICE in accordance with the applicable law and will not use the SERVICE for any illegal or fraudulent purposes.\n\nPerson under the age of majority in their resident jurisdictions may not use the SERVICE unless their parent or legal guardian has reviewed and agreed to TOS.");
                sheetDialog.setCancelable(false);
                sheetDialog.setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        terms.setChecked(true);
                    }
                });
                sheetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alert.dismiss();
                    }
                });
                sheetDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        alert.dismiss();
                    }
                });

                alert = sheetDialog.create();
                alert.show();
            }
        });

        policytext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder sheetDialog = new AlertDialog.Builder(TermsActivityTemp.this);
                sheetDialog.setTitle("Privacy Policies");
                sheetDialog.setMessage("That information provided is only for informative purpose only and Rebataur does not vouch for its accuracy.\n\nRebataur will not be held accountable for any loss or damages arising for usage of this software.");
                sheetDialog.setCancelable(false);
                sheetDialog.setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        policy.setChecked(true);
                    }
                });
                sheetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alert.dismiss();
                    }
                });
                sheetDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        alert.dismiss();
                    }
                });

                alert = sheetDialog.create();
                alert.show();
            }
        });

        backButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
                startActivity(new Intent(TermsActivityTemp.this, LoginActivityTemp.class));
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalStorage.setUserID(user_id);
                LocalStorage.setToken(token);
                startActivity(new Intent(TermsActivityTemp.this,MainActivity.class));
            }
        });
    }
}
