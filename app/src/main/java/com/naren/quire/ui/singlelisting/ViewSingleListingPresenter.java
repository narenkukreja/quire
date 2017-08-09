package com.naren.quire.ui.singlelisting;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.naren.quire.util.app.Quire;
import com.naren.quire.util.http.QuireAPI;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import es.dmoral.toasty.Toasty;

public class ViewSingleListingPresenter {

    private final String LOG_TAG = ViewSingleListingPresenter.this.getClass().getSimpleName();
    private Context mContext;

    private IViewSingleListingView mSingleListingView;

    public ViewSingleListingPresenter(Context mContext) {
        this.mContext = mContext;
    }

    public void onViewAttached(IViewSingleListingView view) {
        mSingleListingView = view;
    }

    public void onViewDetached() {
        mSingleListingView = null;
    }

    private IViewSingleListingView getView() {
        return mSingleListingView;
    }

    public void persistChatToAPI(Integer productId, final String chatUrl) throws JSONException {

        getView().showDialog();

        // {host}/me/skipped_products
        String url = QuireAPI.BASE_URL + QuireAPI.ME_ENDPOINT + QuireAPI.CHAT_ENDPOINT;

        JSONObject productObj = new JSONObject();
        productObj.put("product_id", productId);
        productObj.put("url", chatUrl);

        JSONObject chatObject = new JSONObject();
        chatObject.put("chat", productObj);

        Log.d(LOG_TAG, chatObject.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url,
                chatObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        getView().hideDialog();
                        getView().startChatActivity(chatUrl);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toasty.error(mContext, "chat creation error! :( - " + error, Toast.LENGTH_LONG).show();
            }
        }
        ) {
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

        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(jsonObjectRequest);
    }
}
