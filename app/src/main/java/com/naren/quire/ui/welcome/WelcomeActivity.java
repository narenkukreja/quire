package com.naren.quire.ui.welcome;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.naren.quire.R;
import com.naren.quire.ui.nearbyproducts.NearbyProductsActivity;
import com.naren.quire.ui.welcome.fragments.WelcomeFragment;
import com.naren.quire.util.service.QuireService;
import com.facebook.*;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.*;

import es.dmoral.toasty.Toasty;
import me.relex.circleindicator.CircleIndicator;
import org.json.JSONException;
import org.json.JSONObject;

public class WelcomeActivity extends AppCompatActivity implements IWelcomeView {

    private final String LOG_TAG = WelcomeActivity.this.getClass().getSimpleName();
    private Context mContext;
    private WelcomePresenter welcomePresenter;

    private ProgressDialog mProgressDialog;

    private MaterialDialog.Builder builder;
    private MaterialDialog dialog;

    private ViewPager mViewPager;
    private ViewPagerAdapter mAdapter;
    private CircleIndicator mCircleIndicator;

    private LoginButton fbLoginButton;
    private CallbackManager mCallbackManager;

    private String name, picture;

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private static final int READ_LOCATION_PERMISSIONS_REQUEST = 1;

    //private static final String sendbirdAppId = "D28B2F73-8E0A-4DB2-B568-96D6033855B6";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        setStatusBarColor();
        mContext = this;
        initializeViews();

        if (welcomePresenter == null) {
            welcomePresenter = new WelcomePresenter(mContext);
        }

        // Initialize service
        Intent intent = new Intent(mContext, QuireService.class);
        startService(intent);

        Button customButtonFB = (Button) findViewById(R.id.fb_login_button);
        fbLoginButton = (LoginButton) findViewById(R.id.button_fb_login);
        mCallbackManager = CallbackManager.Factory.create();

        customButtonFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbLoginButton.performClick();
            }
        });

        fbLoginButton.setReadPermissions("name");
        fbLoginButton.setReadPermissions("email");
        fbLoginButton.setReadPermissions("public_profile");

        fbLoginButton.registerCallback(mCallbackManager, mCallback);
    }

    @Override
    public void startNearbyProductsActivity() {
        startActivity(new Intent(mContext, NearbyProductsActivity.class));
        overridePendingTransition(R.anim.left_out, R.anim.right_in);
    }

    FacebookCallback<LoginResult> mCallback = new FacebookCallback<LoginResult>() {

        @Override
        public void onSuccess(LoginResult loginResult) {

            final String fbStringToken = loginResult.getAccessToken().getToken();

            AccessToken accessToken = loginResult.getAccessToken();

            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {

                            try {

                                JSONObject data = response.getJSONObject();

                                name = object.getString("name");
                                picture = data.getJSONObject("picture").getJSONObject("data").getString("url");

                                Log.d("object", "token: " + fbStringToken + "\n" +
                                        object.toString());

                                getPresenter().createSession(name, fbStringToken, picture);

                            } catch (JSONException e) {
                                e.printStackTrace();

                            }
                        }
                    });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "name, email, picture.type(large)");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public void onCancel() {
            Toasty.normal(WelcomeActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(FacebookException error) {
            Toasty.error(WelcomeActivity.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void showDialog() {
        mProgressDialog = ProgressDialog.show(mContext, "", getString(R.string.welcome_dialog_login), true);
    }

    @Override
    public void hideDialog() {
        mProgressDialog.dismiss();
    }

    private static class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private final int NUM_PAGES = 4;

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Fragment mFragment = null;

            switch (position) {

                case 0:
                    mFragment = WelcomeFragment.newInstance("one");
                    break;

                case 1:
                    mFragment = WelcomeFragment.newInstance("two");
                    break;

                case 2:
                    mFragment = WelcomeFragment.newInstance("three");
                    break;

                case 3:
                    mFragment = WelcomeFragment.newInstance("four");
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

    @Override
    public void getPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, READ_LOCATION_PERMISSIONS_REQUEST);
            }
        }
    }

    @Override
    public void displayLocationSettingsRequest(Context context) {

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
                            status.startResolutionForResult(WelcomeActivity.this, REQUEST_CHECK_SETTINGS);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(intent);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:

                switch (resultCode) {
                    case Activity.RESULT_CANCELED:
                        displayLocationSettingsRequest(mContext);
                        break;
                    default:
                        break;
                }
                break;
        }
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
    }

    public WelcomePresenter getPresenter() {
        return welcomePresenter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //SendBird.init(sendbirdAppId, mContext);
        getPresenter().onViewAttached(WelcomeActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPresenter().onViewDetached();
    }

    private void initializeViews() {
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mCircleIndicator = (CircleIndicator) findViewById(R.id.viewPagerIndicator);
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mCircleIndicator.setViewPager(mViewPager);
    }

    @Override
    public void show500ServerError() {

        builder = new MaterialDialog.Builder(mContext)
                .title(R.string.all_dialog_server_error)
                .content(R.string.all_dialog_try_again)
                .positiveText(R.string.all_dialog_positive).onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        dialog.dismiss();

                    }
                });

        dialog = builder.build();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void showNoInternetConnectionError() {

        builder = new MaterialDialog.Builder(mContext)
                .title(R.string.all_dialog_connection_error_title)
                .content(R.string.all_dialog_connection_error_content)
                .positiveText(R.string.all_dialog_positive).onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });

        dialog = builder.build();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
    }
}