package com.naren.quire.ui.newitem;

import android.content.Context;
import android.util.Log;

import com.naren.quire.util.app.Quire;
import com.naren.quire.util.http.QuireAPI;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ListNewItemPresenter {

    private final String LOG_TAG = ListNewItemPresenter.this.getClass().getSimpleName();
    private Context mContext;

    private IListNewItemView mListNewItemView;

    public ListNewItemPresenter(Context mContext) {
        this.mContext = mContext;
    }

    public void onViewAttached(IListNewItemView view) {
        mListNewItemView = view;
    }

    public void onViewDetached() {
        mListNewItemView = null;
    }

    private IListNewItemView getView() {
        return mListNewItemView;
    }

    public void createListing(String product_name, String product_description, String product_price, ArrayList<String> imagesArrayList) throws JSONException {

        Log.d(LOG_TAG, "\nLen: " + imagesArrayList.size());
        Log.d(LOG_TAG, "List: " + Arrays.toString(imagesArrayList.toArray()));

        if (imagesArrayList.size() != 3) {

            getView().showRequiredImagesDialog();

        } else {

            getView().showDialog();

            Double product_price_double = Double.parseDouble(product_price);

            JSONObject productObject = new JSONObject();

            JSONObject productDetailsObject = new JSONObject();
            productDetailsObject.put("name", product_name);
            productDetailsObject.put("description", product_description);
            productDetailsObject.put("price", product_price_double);

            JSONArray jsonArray = new JSONArray();

            JSONObject productImageAttributesObject = null;

            for (int i = 0; i < imagesArrayList.size(); i++) {
                productImageAttributesObject = new JSONObject();
                productImageAttributesObject.put("img_base", "data:image/jpeg;base64," + imagesArrayList.get(i));
                jsonArray.put(productImageAttributesObject);
            }

            productDetailsObject.put("images_attributes", jsonArray);

            productObject.put("product", productDetailsObject);

            Log.d(LOG_TAG, "Product object" + productObject.toString());

            // {host}/users/{user_id}/products
            String url = QuireAPI.BASE_URL + QuireAPI.USERS_ENDPOINT + "/" + Quire.userID + QuireAPI.PRODUCTS_ENDPOINT;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    url,
                    productObject, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    getView().hideDialog();
                    //mProgressDialog.dismiss();
                    Log.d(LOG_TAG, "Res: " + response);
                    getView().showSuccessDialog();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    getView().hideDialog();
                    Log.d(LOG_TAG, "Error: " + error.toString());
                    handleErrors(error);
                }
            }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {

                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    headers.put("Authorization", Quire.access_token);

                    return headers;

                }
            };

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            RequestQueue queue = Volley.newRequestQueue(mContext);
            queue.add(jsonObjectRequest);
        }
    }

    private void handleErrors(VolleyError error) {
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            getView().showTimeOutError();
        } else if (error instanceof ServerError) {
            getView().show500ServerError();
        } else if (error instanceof NetworkError) {
            getView().showNetworkError();
        }
    }
}