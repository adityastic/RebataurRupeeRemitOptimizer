package com.rebataur.forexapp.utils.storage;

import android.content.SharedPreferences;

public class LocalStorage {
    private static SharedPreferences prefs;

    public static SharedPreferences getPrefs() {
        return prefs;
    }

    public static void setPrefs(SharedPreferences prefs) {
        LocalStorage.prefs = prefs;
    }

    public static String getToken(){
        return prefs.getString("token","");
    }

    public static void setToken(String token){
        prefs.edit().putString("token",token).apply();
    }

    public static String getUserID(){
        return prefs.getString("user_id","");
    }

    public static void setUserID(String userid){
        prefs.edit().putString("user_id",userid).apply();
    }

    public static String getCurrentCurrency(){
        return prefs.getString("currency","");
    }

    public static void setCurrentCurrency(String currency){
        prefs.edit().putString("currency",currency).apply();
    }

    public static String getWindow(){
        return prefs.getString("window","");
    }

    public static void setWindow(String window){
        prefs.edit().putString("window",window).apply();
    }

    public static void resetToken(){
        prefs.edit().remove("token").apply();
        prefs.edit().remove("user_id").apply();
        prefs.edit().remove("currency").apply();
        prefs.edit().remove("window").apply();
    }
}
