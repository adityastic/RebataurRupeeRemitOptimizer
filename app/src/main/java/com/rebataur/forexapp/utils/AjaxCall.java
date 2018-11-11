package com.rebataur.forexapp.utils;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.rebataur.forexapp.application.ApplicationActivity;
import com.rebataur.forexapp.utils.storage.LocalStorage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class AjaxCall {
    private static final int AUTH = 0;
    private static final int EVENT = 1;

    private static final String appName = "forexapp";

    private static final String serviceHost = "https://rebataur.com";
    private static final String serviceURL = "/rebengine/invokeapi";
    private static final String eventURL = "/rebengine/invokeevent";
    private static final String authURL = "/rebengine/rauth";


    private static void invokeAPI(int type, JSONObject jsonObject, final APICallback callback) {
        String url = "";

        switch (type) {
            case AUTH:
                url = serviceHost + authURL;
                break;
            case EVENT:
                url = serviceHost + eventURL;
                break;
        }

        final String requestBody = jsonObject.toString();

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    callback.apiSuccess(new JSONObject(response));
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.apiFailure(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.apiFailure(error);
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json;";
            }

            @Override
            public byte[] getBody() {
                try {
                    return requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    callback.apiFailure(e);
                }
                return null;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", " Bearer " + LocalStorage.getToken());
                return params;
            }
        };

        ApplicationActivity.mRequestQueue.add(stringRequest);
    }

    public static void login(String email, String pass, APICallback callback) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("auth_type", "login");
            jsonObject.put("username", email);
            jsonObject.put("password", pass);
            jsonObject.put("app_name", appName);

            invokeAPI(AjaxCall.AUTH, jsonObject, callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.apiFailure(e);
        }
    }

    public static void registerUser(String username, String password, String email, APICallback callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("auth_type", "register");
            jsonObject.put("username", username);
            jsonObject.put("password", password);
            jsonObject.put("email", email);
            jsonObject.put("app_name", appName);

            invokeAPI(AjaxCall.AUTH, jsonObject, callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.apiFailure(e);
        }
    }

    public static void resetUser(String email, APICallback callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("auth_type", "reset");
            jsonObject.put("email", email);
            jsonObject.put("app_name", appName);

            invokeAPI(AjaxCall.AUTH, jsonObject, callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.apiFailure(e);
        }
    }

    public static void resetConfirmed(String email,String newPass, int token, APICallback callback) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("auth_type", "reset_password");
            jsonObject.put("email", email);
            jsonObject.put("token", token);
            jsonObject.put("new_password", newPass);
            jsonObject.put("app_name", appName);

            invokeAPI(AjaxCall.AUTH, jsonObject, callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.apiFailure(e);
        }
    }

    public static void confirmRegisterUser(String token, APICallback callback) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("auth_type", "confirm_registration");
            jsonObject.put("token", token);
            jsonObject.put("app_name", appName);

            invokeAPI(AjaxCall.AUTH, jsonObject, callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.apiFailure(e);
        }
    }

    public static void selectCurrency(String currency, APICallback callback) {
        JSONObject selectCurr = new JSONObject();
        try {
            selectCurr.put("event_name", "get_forex_rate");
            selectCurr.put("event_type", "command");
            selectCurr.put("version", 1);

            JSONObject payload = new JSONObject();
            payload.put("user_id", LocalStorage.getUserID());
            payload.put("currency", currency);

            selectCurr.put("payload", payload);

            invokeAPI(AjaxCall.EVENT, selectCurr, callback);

        } catch (JSONException e) {
            e.printStackTrace();
            callback.apiFailure(e);
        }
    }

    public static void queryCurrency(APICallback callback) {
        JSONObject getAll = new JSONObject();
        try {
            getAll.put("event_name", "query_forex_rate");
            getAll.put("event_type", "query");
            getAll.put("version", 1);

            JSONObject payload = new JSONObject();
            payload.put("user_id", LocalStorage.getUserID());
            getAll.put("payload", payload);

            invokeAPI(AjaxCall.EVENT, getAll, callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.apiFailure(e);
        }
    }

    public static void sendUserConfig(String currency,String window, APICallback callback){
        JSONObject userConfig = new JSONObject();
        try {
            userConfig.put("event_name", "store_user_config");
            userConfig.put("event_type", "command");
            userConfig.put("version", 1);

            JSONObject payload = new JSONObject();
            payload.put("user_id", LocalStorage.getUserID());
            payload.put("currency", currency);
            payload.put("window_period_days", window);

            userConfig.put("payload", payload);

            invokeAPI(AjaxCall.EVENT, userConfig, callback);

        } catch (JSONException e) {
            e.printStackTrace();
            callback.apiFailure(e);
        }
    }

    public static void queryWindowCurrency(APICallback callback) {
        JSONObject getAll = new JSONObject();
        try {
            getAll.put("event_name", "query_user_forex_values");
            getAll.put("event_type", "query");
            getAll.put("version", 1);

            JSONObject payload = new JSONObject();
            payload.put("user_id", LocalStorage.getUserID());
            getAll.put("payload", payload);

            invokeAPI(AjaxCall.EVENT, getAll, callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.apiFailure(e);
        }
    }

    public interface APICallback {
        void apiSuccess(JSONObject jsonObject);

        void apiFailure(Exception e);
    }
}
