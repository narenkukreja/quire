package com.naren.quire.ui.profile;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.naren.quire.R;
import com.naren.quire.ui.about.AboutActivity;
import com.naren.quire.ui.newitem.ListNewItemActivity;
import com.naren.quire.ui.profile.adapters.UserListingsAdapter;
import com.naren.quire.util.app.Quire;
import com.naren.quire.data.Product;
import com.naren.quire.ui.welcome.WelcomeActivity;
import com.naren.quire.util.http.QuireAPI;
import com.naren.quire.util.session.SessionManager;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class ProfileFragment extends Fragment implements View.OnClickListener, Toolbar.OnMenuItemClickListener {

    private final String LOG_TAG = ProfileFragment.this.getClass().getSimpleName();
    private Toolbar mToolbar;

    private Button mButtonListNewItem;
    private CircleImageView mImageProfile;
    private TextView mTextViewFullName;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private UserListingsAdapter userListingsAdapter;
    private ArrayList<Product> productListingArrayList = new ArrayList<>();
    private Context mContext;
    private ImageView mImageUpload;

    private OnFragmentInteractionListener mListener;

    private SessionManager sessionManager;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        sessionManager = new SessionManager(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mContext = getContext();

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.inflateMenu(R.menu.menu_profile);
        mToolbar.setOnMenuItemClickListener(this);

        mImageProfile = (CircleImageView) rootView.findViewById(R.id.image_profile);
        mTextViewFullName = (TextView) rootView.findViewById(R.id.text_fullname);

        Glide.with(mContext).load(Quire.userProfilePic).into(mImageProfile);
        mTextViewFullName.setText(Quire.name);

        mImageUpload = (ImageView) rootView.findViewById(R.id.image_one);
        mImageUpload.setOnClickListener(this);

        mButtonListNewItem = (Button) rootView.findViewById(R.id.button_list_new_item);
        mButtonListNewItem.setOnClickListener(this);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);

        userListingsAdapter = new UserListingsAdapter(mContext, userListingsData());
        mRecyclerView.setAdapter(userListingsAdapter);

        mLayoutManager = new GridLayoutManager(mContext, 3);
//        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);

        return rootView;
    }

    private ArrayList<Product> userListingsData() {

        String url = QuireAPI.BASE_URL + QuireAPI.USERS_ENDPOINT + "/" + Quire.userID + "/products";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        Product product;

                        Log.d(LOG_TAG, "Res: " + response);

                        if (response.length() == 0) {

                            mButtonListNewItem.setVisibility(View.VISIBLE);
                            mRecyclerView.setVisibility(View.GONE);
                            mImageUpload.setVisibility(View.GONE);

                        } else {

                            mButtonListNewItem.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mImageUpload.setVisibility(View.VISIBLE);

                            Product[] products = new Gson().fromJson(response.toString(), Product[].class);
                            Collections.addAll(productListingArrayList, products);
                            Collections.reverse(productListingArrayList);

                            userListingsAdapter.notifyDataSetChanged();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toasty.error(mContext, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", Quire.access_token);
                return headers;
            }

        };

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(jsonArrayRequest);

        return productListingArrayList;

    }

    @Override
    public void onClick(View v) {

        Intent intent;
        Bundle bundle;

        switch (v.getId()) {

            case R.id.button_list_new_item:
                intent = new Intent(mContext, ListNewItemActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.left_out, R.anim.right_in);
                break;

            case R.id.image_one:
                intent = new Intent(mContext, ListNewItemActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.left_out, R.anim.right_in);
                break;
        }
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_share:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,
                        "Hey check out my app at: https://play.google.com/store/apps/details?id=" + getActivity().getPackageName());
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, "Share app using"));
                break;

            case R.id.menu_rate_app:
                Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
                }
                break;

            case R.id.menu_about:
                startActivity(new Intent(mContext, AboutActivity.class));
                getActivity().overridePendingTransition(R.anim.left_out, R.anim.right_in);
                break;

            case R.id.menu_logout:
                LoginManager.getInstance().logOut();
                sessionManager.setLogin(false);
                startActivity(new Intent(mContext, WelcomeActivity.class));
                getActivity().overridePendingTransition(R.anim.left_out, R.anim.right_in);
                getActivity().finish();
                break;
        }

        return false;
    }

    private interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
