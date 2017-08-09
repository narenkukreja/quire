package com.naren.quire.util.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.naren.quire.util.app.Quire;

public class SessionManager {

    // LogCat tag
    private String LOG_TAG = SessionManager.this.getClass().getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    Editor editor;
    Context mContext;

    // Shared preferences file name
    private static final String PREF_NAME = "QuireApp";

    private static final String DEFAULT_STRING = "";
    private static final int DEFAULT_INT = 0;
    private static final int DEFAULT_LONG = 0;

    private static final String KEY_USER_ID = "userID",
            KEY_USERNAME = "username",
            KEY_NAME = "name",
            KEY_USER_EMAIL = "userEmail",
            KEY_USER_ACCESS_TOKEN = "access_token",
            KEY_USER_LAT = "lat",
            KEY_USER_LNG = "lng",
            KEY_USER_PREFERENCE_RADIUS = "preferences_radius",
            KEY_IS_LOGGEDIN = "isLoggedIn",
            KEY_PROFILE_PIC = "profilePic";

    public SessionManager(Context context) {
        this.mContext = context;
        pref = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.commit();
        Log.d(LOG_TAG, "User login session modified!");
    }

    public void setUserInfo(int user_id, String username, String name,
                            String email, String access_token, String lat,
                            String lng, long preference_radius, String profilePic) {

        editor.putInt(KEY_USER_ID, user_id);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ACCESS_TOKEN, access_token);
        editor.putString(KEY_USER_LAT, lat);
        editor.putString(KEY_USER_LNG, lng);
        editor.putLong(KEY_USER_PREFERENCE_RADIUS, preference_radius);
        editor.putString(KEY_PROFILE_PIC, profilePic);

        editor.commit();

    }

    public void getUserInfo() {

        int user_id = pref.getInt(KEY_USER_ID, DEFAULT_INT);
        String username = pref.getString(KEY_USERNAME, DEFAULT_STRING);
        String name = pref.getString(KEY_NAME, DEFAULT_STRING);
        String email = pref.getString(KEY_USER_EMAIL, DEFAULT_STRING);
        String access_token = pref.getString(KEY_USER_ACCESS_TOKEN, DEFAULT_STRING);
        String lat = pref.getString(KEY_USER_LAT, DEFAULT_STRING);
        String lng = pref.getString(KEY_USER_LNG, DEFAULT_STRING);
        long preference_radius = pref.getLong(KEY_USER_PREFERENCE_RADIUS, DEFAULT_LONG);
        String profile_pic = pref.getString(KEY_PROFILE_PIC, DEFAULT_STRING);

        if (user_id == DEFAULT_INT
                || username.equals(DEFAULT_STRING)
                || name.equals(DEFAULT_STRING)
                || email.equals(DEFAULT_STRING)
                || access_token.equals(DEFAULT_STRING)
                || lat.equals(DEFAULT_STRING)
                || lng.equals(DEFAULT_STRING)
                || preference_radius == DEFAULT_LONG
                || profile_pic.equals(DEFAULT_STRING)) {

            Log.d(LOG_TAG, "No data");

        } else {

            Log.d(LOG_TAG, "data exists");
            Quire.userID = user_id;
            Quire.username = username;
            Quire.name = name;
            Quire.userEmail = email;
            Quire.access_token = access_token;
            Quire.userLat = lat;
            Quire.userLng = lng;
            Quire.preference_radius = preference_radius;
            Quire.userProfilePic = profile_pic;
        }
    }
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }
}
