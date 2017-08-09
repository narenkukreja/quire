package com.naren.quire.ui.welcome;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.naren.quire.util.http.QuireAPI;
import com.naren.quire.util.session.SessionManager;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class WelcomePresenter {

    private final String LOG_TAG = WelcomePresenter.this.getClass().getSimpleName();
    private Context mContext;

    private IWelcomeView mWelcomeView;

    private SessionManager sessionManager;

    private LocationManager mLocationManager;

    private boolean enabled;

    private double lat, lng;

    public WelcomePresenter(Context mContext) {
        this.mContext = mContext;
    }

    public void onViewAttached(IWelcomeView view) {

        mWelcomeView = view;

        sessionManager = new SessionManager(mContext);

        if (sessionManager.isLoggedIn()) {
            getView().startNearbyProductsActivity();
        }

        getView().getPermissions();

        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled) {
            getView().displayLocationSettingsRequest(mContext);
        }

        SmartLocation.with(mContext).location().oneFix().start(new OnLocationUpdatedListener() {
            @Override
            public void onLocationUpdated(Location location) {

                lat = location.getLatitude();
                lng = location.getLongitude();

                Log.d(LOG_TAG, "LatLng: " + lat + ", lng: " + lng);
            }
        });

    }

    public void onViewDetached() {
        mWelcomeView = null;
    }

    private IWelcomeView getView() {
        return mWelcomeView;
    }

    public void createSession(String name, String fbStringToken, final String picture) {

        getView().showDialog();

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("full_name", name);
        params.put("last_location", "POINT(" + lng + " " + lat + ")");

        JSONObject userJsonObject = new JSONObject();

        try {
            userJsonObject.put("user", new JSONObject(params));
            userJsonObject.put("fb_access_token", fbStringToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(LOG_TAG, userJsonObject.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                QuireAPI.BASE_URL + QuireAPI.SESSIONS_ENDPOINT, userJsonObject, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                getView().hideDialog();

                if (response != null) {

                    try {

                        int id = response.getInt("id");
                        String username = response.getString("username");
                        String email = response.getString("email");
                        String name = response.getString("name");

                        JSONObject lastLocationObject = response.getJSONObject("last_loc");

                        String lat = lastLocationObject.getString("lat");
                        String lng = lastLocationObject.getString("lng");

                        String access_token = response.getString("access_token");
                        long preference_radius = response.getLong("preference_radius");

                        sessionManager.setLogin(true);
                        sessionManager.setUserInfo(id, username,
                                name, email, access_token, lat, lng, preference_radius, picture);

                        Log.d(LOG_TAG, response.toString());

                        getView().startNearbyProductsActivity();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {

                    MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext)
                            .title("Error")
                            .positiveText("Ok").onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();

                                }
                            });

                    MaterialDialog dialog = builder.build();
                    dialog.show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                getView().hideDialog();

                if (error instanceof ServerError) {
                    getView().show500ServerError();
                }

                if (error instanceof TimeoutError || error instanceof NoConnectionError
                        || error instanceof NetworkError) {

                    getView().showNoInternetConnectionError();
                }
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(jsonObjectRequest);
    }
}