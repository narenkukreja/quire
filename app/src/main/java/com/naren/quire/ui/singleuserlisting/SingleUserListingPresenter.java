package com.naren.quire.ui.singleuserlisting;

import android.content.Context;
import android.util.Log;

import com.naren.quire.util.app.Quire;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.naren.quire.util.http.QuireAPI;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class SingleUserListingPresenter {

    private final String LOG_TAG = SingleUserListingPresenter.this.getClass().getSimpleName();
    private Context mContext;

    private IViewSingleUserListingView mViewSingleUserListingView;

    public SingleUserListingPresenter(Context mContext) {
        this.mContext = mContext;
    }

    public void onViewAttached(IViewSingleUserListingView view) {
        mViewSingleUserListingView = view;
    }

    public void onViewDetached() {
        mViewSingleUserListingView = null;
    }

    private IViewSingleUserListingView getView() {
        return mViewSingleUserListingView;
    }

    public void deleteListing(int product_id) {

        getView().showDialog();

        String url = QuireAPI.BASE_URL + QuireAPI.USERS_ENDPOINT + "/" + Quire.userID + "/products/" + product_id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                getView().hideDialog();

                Log.d(LOG_TAG, "Delete item response: " + response);

                try {
                    boolean success = response.getBoolean("success");
                    if (success) {
                        getView().showSuccessDialog();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getView().hideDialog();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", Quire.access_token);
                return headers;
            }

        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(jsonObjectRequest);
    }
}