package com.lukekorth.httpebble;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Random;

public class Settings {

    private static final String EMAIL = "email";
    private static final String TOKEN = "token";
    private static final String NEED_TO_REGISTER = "need_to_register";
    private static final String REGISTRATION_ID = "gcm_id";

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

    public static String getGCMRegistrationId(Context context) {
        return getPreferences(context).getString(REGISTRATION_ID, null);
    }

    public static void setGCMRegistrationId(Context context, String id) {
        getPreferences(context).edit().putString(REGISTRATION_ID, id).apply();
    }

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
