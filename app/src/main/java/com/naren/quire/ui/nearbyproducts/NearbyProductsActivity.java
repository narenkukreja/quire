package com.naren.quire.ui.nearbyproducts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.naren.quire.R;
import com.naren.quire.util.app.Quire;
import com.naren.quire.ui.chat.ChatFragment;
import com.naren.quire.ui.chat.GroupChannelActivity;
import com.naren.quire.ui.nearbyproducts.fragments.NearbyProductsFragment;
import com.naren.quire.ui.profile.ProfileFragment;
import com.naren.quire.ui.welcome.WelcomeActivity;
import com.naren.quire.util.http.QuireAPI;
import com.naren.quire.util.session.SessionManager;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import org.json.JSONException;
import org.json.JSONObject;

public class NearbyProductsActivity extends AppCompatActivity {

    private final String LOG_TAG = NearbyProductsActivity.this.getClass().getSimpleName();
    private Context mContext;
    private AHBottomNavigation bottomNavigation;

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private SessionManager session;

    private ViewPager mViewPager;
    private ViewPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_products);
        mContext = this;
        setupBottomBar();
        setStatusBarColor();

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        } else {
            session.getUserInfo();
        }

        Log.d(LOG_TAG, "user: ID: " + Quire.userID + "\nToken: " + Quire.access_token
        + "\nPic: " + Quire.userProfilePic);

        SmartLocation.with(mContext).location().oneFix().start(new OnLocationUpdatedListener() {
            @Override
            public void onLocationUpdated(Location location) {

                double lat = location.getLatitude();
                double lng = location.getLongitude();

                Quire.userLat = String.valueOf(lat);
                Quire.userLng = String.valueOf(lng);

                Log.d(LOG_TAG, "lat: " + Quire.userLat +
                        "\nlng: " + Quire.userLng);

                try {
                    updateUserLocation(Quire.userLat, Quire.userLng);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    private void updateUserLocation(String userLat, String userLng) throws JSONException {

        // {host}/users/{user_id}/
        String url = QuireAPI.BASE_URL + QuireAPI.USERS_ENDPOINT + "/" + Quire.userID;

        JSONObject params = new JSONObject();
        params.put("last_location", "POINT(" + userLng + " " + userLat + ")");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT,
                url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(LOG_TAG, "location update success response: " + response.toString());

                mViewPager = (ViewPager) findViewById(R.id.view_pager);
                mAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                mViewPager.setAdapter(mAdapter);

                mViewPager.setCurrentItem(0);
                mViewPager.setOffscreenPageLimit(2);

                final View touchView = mViewPager;
                touchView.setOnTouchListener(new View.OnTouchListener()
                {
                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {
                        return true;
                    }
                });

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG_TAG, "update location error: " + error.toString());
            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Authorization", Quire.access_token);
                return headers;
            }

        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(jsonObjectRequest);

        connect();

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        SendBird.disconnect(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void connect(){

        String sUserId = String.valueOf(Quire.userID);

        final String mNickname = Quire.name;

        SendBird.connect(sUserId, new SendBird.ConnectHandler(){

            @Override
            public void onConnected(User user, SendBirdException e){
                if(e != null){
                    Toasty.error(mContext, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                SendBird.updateCurrentUserInfo(mNickname, null, new SendBird.UserInfoUpdateHandler() {
                    @Override
                    public void onUpdated(SendBirdException e) {
                        if (e != null){
                            Toasty.normal(NearbyProductsActivity.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });

                if (FirebaseInstanceId.getInstance().getToken() == null) return;

                SendBird.registerPushTokenForCurrentUser(FirebaseInstanceId.getInstance().getToken(), true, new SendBird.RegisterPushTokenWithStatusHandler(){
                    @Override
                    public void onRegistered(SendBird.PushTokenRegistrationStatus pushTokenRegistrationStatus, SendBirdException e){
                        if (e != null){
                            Toasty.error(NearbyProductsActivity.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
            }
        });
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private final int NUM_PAGES = 3;

        private ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Fragment mFragment = null;

            switch (position) {

                case 0:
                    mFragment = NearbyProductsFragment.newInstance();
                    break;

                case 1:
                    mFragment = ChatFragment.newInstance("1", "");
                    break;

                case 2:
                    mFragment = ProfileFragment.newInstance("2", "");
                    break;
            }

            return mFragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

//        @Override
//        public CharSequence getPageTitle(int position) {
//
//            return NUM_PAGES[position];
//        }

    }

    private void logoutUser() {

        // {host}/sessions
        String logoutUrl = QuireAPI.BASE_URL + QuireAPI.SESSIONS_ENDPOINT;

        final StringRequest stringRequest = new StringRequest(Request.Method.DELETE,
                logoutUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                LoginManager.getInstance().logOut();
                session.setLogin(false);

                startActivity(new Intent(mContext, WelcomeActivity.class));
                overridePendingTransition(R.anim.left_out, R.anim.right_in);
                finish();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toasty.error(mContext, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", Quire.access_token);

                return headers;

            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(stringRequest);
    }

    private void displayLocationSettingsRequest(Context context) {

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();

                switch (status.getStatusCode()) {

                    case LocationSettingsStatusCodes.SUCCESS:

                        Log.d(LOG_TAG, "All location settings are satisfied.");

                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        Log.d(LOG_TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(NearbyProductsActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {

                            Log.d(LOG_TAG, "PendingIntent unable to execute request.");
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        Log.d(LOG_TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(intent);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {

                    case Activity.RESULT_OK:
                        startActivity(new Intent(mContext, NearbyProductsActivity.class));
                        overridePendingTransition(R.anim.left_out, R.anim.right_in);
                        break;

                    case Activity.RESULT_CANCELED:
                        displayLocationSettingsRequest(mContext);
                        break;

                    default:
                        break;
                }
                break;
        }
    }

    private void setupBottomBar() {

        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottomBar);

        // Create items
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.nearby_bottom_navigation_home, R.drawable.ic_list_black_24dp, R.color.colorAccent);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.nearby_bottom_navigation_chat, R.drawable.ic_chat_bubble_outline_black_24dp, R.color.colorAccent);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.nearby_bottom_navigation_profile, R.drawable.ic_person_outline_black_24dp, R.color.colorAccent);

        // Add items
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);

        bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FEFEFE"));

        bottomNavigation.setCurrentItem(0);

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {

                switch (position) {

                    case 0:
                        mViewPager.setCurrentItem(0);
                        break;

                    case 1:
                        mViewPager.setCurrentItem(1);
                        Intent intent = new Intent(NearbyProductsActivity.this, GroupChannelActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.left_out, R.anim.right_in);
                        break;

                    case 2:
                        mViewPager.setCurrentItem(2);
                        break;
                }

                return true;
            }
        });

        bottomNavigation.setOnNavigationPositionListener(new AHBottomNavigation.OnNavigationPositionListener() {
            @Override
            public void onPositionChange(int y) {
                //Toast.makeText(mContext, "Change: " + y, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setStatusBarColor() {
        Window window = this.getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.BLACK);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        //overridePendingTransition(R.anim.left_out, R.anim.right_in);
    }
}
