package com.naren.quire.ui.nearbyproducts.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.naren.quire.R;
import com.naren.quire.ui.nearbyproducts.NearbyProductsActivity;
import com.naren.quire.ui.nearbyproducts.adapters.NearbyProductsAdapter;
import com.naren.quire.ui.welcome.WelcomeActivity;
import com.naren.quire.util.app.Quire;
import com.naren.quire.data.Product;
import com.naren.quire.ui.newitem.ListNewItemActivity;
import com.naren.quire.util.http.QuireAPI;
import com.naren.quire.util.session.SessionManager;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.login.LoginManager;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import link.fls.swipestack.SwipeStack;

public class NearbyProductsFragment extends Fragment {

    private final String LOG_TAG = NearbyProductsFragment.this.getClass().getSimpleName();

    private SwipeStack swipeStack;
    private NearbyProductsAdapter nearbyProductsAdapter;
    private ProgressDialog mProgressDialog;

    private ImageView refreshImageView;
    private LinearLayout noProductsLayout;
    private ArrayList<Product> nearbyProductArrayList = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    private ImageView mImageViewUpload;

    public NearbyProductsFragment() {
        // Required empty public constructor
    }

    public static NearbyProductsFragment newInstance() {
        NearbyProductsFragment fragment = new NearbyProductsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_nearby_products, container, false);

        noProductsLayout = (LinearLayout) rootView.findViewById(R.id.no_products_layout);
        refreshImageView = (ImageView) rootView.findViewById(R.id.refresh_imageView);
        mImageViewUpload = (ImageView) rootView.findViewById(R.id.image_one);

        mImageViewUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ListNewItemActivity.class));
                getActivity().overridePendingTransition(R.anim.left_out, R.anim.right_in);

            }
        });

        swipeStack = (SwipeStack) rootView.findViewById(R.id.swipeStack);
        nearbyProductsAdapter = new NearbyProductsAdapter(getNearbyProducts(), getContext(), swipeStack,
                noProductsLayout, refreshImageView);
        swipeStack.setAdapter(nearbyProductsAdapter);

        // Inflate the layout for this fragment
        return rootView;
    }

    private ArrayList<Product> getNearbyProducts() {

        mProgressDialog = ProgressDialog.show(getContext(), "", getString(R.string.all_dialog_loading), true);

        nearbyProductArrayList.clear();

        // {host}/users/{user_id}/products/nearby
        String url = QuireAPI.BASE_URL + QuireAPI.USERS_ENDPOINT + "/" + Quire.userID + QuireAPI.PRODUCTS_ENDPOINT + QuireAPI.NEARBY_ENDPOINT;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {

                mProgressDialog.dismiss();

                Log.d(LOG_TAG, "res: " + response);

                if (response.length() == 0) {

                    noProductsLayout.setVisibility(View.VISIBLE);

                    refreshImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            nearbyProductArrayList.clear();
                            getActivity().startActivity(new Intent(getActivity(), NearbyProductsActivity.class));
                            getActivity().overridePendingTransition(R.anim.left_out, R.anim.right_in);
                        }
                    });

                } else {

                    Product[] products = new Gson().fromJson(response.toString(), Product[].class);
                    Collections.addAll(nearbyProductArrayList, products);
                    nearbyProductsAdapter.notifyDataSetChanged();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressDialog.dismiss();
                handleErrors(error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Authorization", Quire.access_token);
                return headers;
            }
        };

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(jsonArrayRequest);

        return nearbyProductArrayList;
    }

    private void handleErrors(VolleyError error) {
        NetworkResponse response = error.networkResponse;

        if (response != null && response.data != null) {

            Log.d(LOG_TAG, "Error response: " + response.statusCode);

            switch (response.statusCode) {

                case 404:
                    logoutUser();
                    break;

            }
        }
    }

    private void logoutUser() {

        SessionManager session = new SessionManager(getContext());
        LoginManager.getInstance().logOut();
        session.setLogin(false);

        startActivity(new Intent(getContext(), WelcomeActivity.class));
        getActivity().overridePendingTransition(R.anim.left_out, R.anim.right_in);
        getActivity().finish();

    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
