package com.lukekorth.httpebble;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.Random;

public class Settings {

    private static final String PURCHASED = "purchased";
    private static final String EMAIL = "email";
    private static final String TOKEN = "token";
    private static final String NEED_TO_REGISTER = "need_to_register";

    public static void upgradeVersion(Context context) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        String userId = preferences.getString("userId", null);
        if (!TextUtils.isEmpty(userId)) {
            editor.remove("userId")
                    .putString(EMAIL, userId);
        }

        String userToken = preferences.getString("userToken", null);
        if (!TextUtils.isEmpty(userToken)) {
            editor.remove("userToken")
                    .putString(TOKEN, userToken);
        }

        editor.apply();
    }

    public static boolean hasPurchased(Context context) {
        return getPreferences(context).getBoolean(PURCHASED, false);
    }

    public static void setPurchased(Context context, boolean purchased) {
        getPreferences(context).edit().putBoolean(PURCHASED, purchased).apply();
    }

    public static String getEmail(Context context) {
        return getPreferences(context).getString(EMAIL, null);
    }

    public static void setEmail(Context context, String email) {
        getPreferences(context).edit().putString(EMAIL, email).apply();
    }

    public static String getToken(Context context) {
        SharedPreferences preferences = getPreferences(context);
        String token = preferences.getString(TOKEN, null);
        if (token == null) {
            Random random = new Random(System.currentTimeMillis());
            String chars = "abcdefghkmnprstwxz348";
            StringBuilder builder = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                builder.append(chars.charAt(random.nextInt(chars.length())));
            }

            token = builder.toString();
            preferences.edit().putString(TOKEN, token).apply();
        }

        return token;
    }

    public static void clearToken(Context context) {
        getPreferences(context).edit().putString(TOKEN, null).apply();
    }

    public static boolean needToRegister(Context context) {
        return getPreferences(context).getBoolean(NEED_TO_REGISTER, true);
    }

    public static void setNeedToRegister(Context context, boolean shouldRegister) {
        getPreferences(context).edit().putBoolean(NEED_TO_REGISTER, shouldRegister).apply();
    }

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
