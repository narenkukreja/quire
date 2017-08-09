package com.naren.quire.ui.nearbyproducts.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.afollestad.materialdialogs.MaterialDialog;
import com.naren.quire.data.Image;
import com.naren.quire.ui.chat.GroupChannelActivity;
import com.naren.quire.ui.nearbyproducts.NearbyProductsActivity;
import com.naren.quire.R;
import com.naren.quire.ui.singlelisting.ViewSingleListingActivity;
import com.naren.quire.data.Product;
import com.naren.quire.util.http.QuireAPI;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import link.fls.swipestack.SwipeStack;

import com.naren.quire.util.app.Quire;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBirdException;

import org.json.JSONException;
import org.json.JSONObject;

public class NearbyProductsAdapter extends BaseAdapter {

    private final String LOG_TAG = NearbyProductsAdapter.this.getClass().getSimpleName();

    private ArrayList<Product> productArrayList;
    private Context mContext;
    private SwipeStack mSwipeStack;
    private ImageView refreshImageView;
    private LinearLayout noProductsLayout;
    private ProgressDialog mProgressDialog;

    public NearbyProductsAdapter(ArrayList<Product> data, Context mContext, SwipeStack mSwipeStack,
                                 LinearLayout noProductsLayout,
                                 ImageView refreshImageView) {
        this.productArrayList = data;
        this.mContext = mContext;
        this.mSwipeStack = mSwipeStack;
        this.noProductsLayout = noProductsLayout;
        this.refreshImageView = refreshImageView;
    }

    @Override
    public int getCount() {
        return productArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return productArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Product product = productArrayList.get(position);

        final String product_name = product.getName();
        final String product_description = product.getDescription();
        final Double product_price = Double.parseDouble(product.getPrice());
        final List<Image> product_images = product.getImages();
        final String seller_profile_image = product.getSeller().getProfilePicture();

        View rootView = convertView;

        if (rootView == null) {

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rootView = inflater.inflate(R.layout.card_layout, parent, false);
        }

        TextView mTextProductName = (TextView) rootView.findViewById(R.id.text_product_name);
        TextView mTextProductDescription = (TextView) rootView.findViewById(R.id.text_product_description);
        TextView mTextProductPrice = (TextView) rootView.findViewById(R.id.text_product_price);
        ImageView mImageProduct = (ImageView) rootView.findViewById(R.id.image_product);
        CircleImageView mSellerImage = (CircleImageView) rootView.findViewById(R.id.image_seller_profile);

        Glide.with(mContext).load(product_images.get(0).getUrl()).into(mImageProduct);

        mTextProductName.setText(product_name + "...");
        mTextProductDescription.setText(product_description + "...");
        mTextProductPrice.setText("$" + String.format("%.2f", product_price));

        Glide.with(mContext).load(seller_profile_image).into(mSellerImage);

        mSwipeStack.setListener(new SwipeStack.SwipeStackListener() {

            @Override
            public void onViewSwipedToLeft(int position) {

                Product product = productArrayList.get(position);

                Log.d(LOG_TAG, "Skipped id: " + product.getId()
                        + "\nname: " + product.getName());

                // Send request here to hide the product
                try {
                    hideProduct(product.getId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onViewSwipedToRight(final int position) {

                MaterialDialog.Builder builder;
                MaterialDialog dialog;

                builder = new MaterialDialog.Builder(mContext)
                        .title("I want this!")
                        .items(R.array.product_option)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int whichItem, CharSequence text) {

                                Intent intent;
                                Bundle bundle;

                                switch (whichItem) {

                                    case 0:

                                        List<String> userIds = new ArrayList<String>();
                                        userIds.add(String.valueOf(Quire.userID));
                                        userIds.add(String.valueOf(product.getSeller().getId()));

                                        JSONObject chatExtraData = new JSONObject();
                                        try {
                                            chatExtraData.put("product_id", product.getId());
                                            chatExtraData.put("seller_id", product.getSeller().getId());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        GroupChannel.createChannelWithUserIds(
                                                userIds,
                                                false,
                                                product.getName(),
                                                "product_images.get(0).getUrl()",
                                                chatExtraData.toString(),
                                                new GroupChannel.GroupChannelCreateHandler() {
                                                    @Override

                                                    public void onResult(GroupChannel groupChannel, SendBirdException e) {
                                                        if (e != null) {
                                                            Toast.makeText(mContext, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }

                                                        try {
                                                            persistChatToAPI(product.getId(), groupChannel.getUrl());
                                                        } catch (JSONException e1) {
                                                            e1.printStackTrace();
                                                        }

                                                    }
                                                });
                                        break;

                                    case 1:
                                        Product product2 = productArrayList.get(position);
                                        intent = new Intent(mContext, ViewSingleListingActivity.class);
                                        bundle = new Bundle();
                                        bundle.putSerializable("product", product2);
                                        //intent.putExtra("product", product2.toString());
                                        intent.putExtras(bundle);
                                        mContext.startActivity(intent);
                                        Activity activity = (Activity) mContext;
                                        activity.overridePendingTransition(R.anim.left_out, R.anim.right_in);
                                        break;

                                    case 2:

                                        Product products = productArrayList.get(position);
                                        try {
                                            hideProduct(products.getId());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        break;
                                }
                            }
                        });
                dialog = builder.build();
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);
            }

            @Override
            public void onStackEmpty() {

                noProductsLayout.setVisibility(View.VISIBLE);

                refreshImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mContext.startActivity(new Intent(mContext, NearbyProductsActivity.class));
                        Activity activity = (Activity) mContext;
                        activity.overridePendingTransition(R.anim.left_out, R.anim.right_in);
                        productArrayList.clear();

                    }
                });
            }
        });

        return rootView;
    }

    private void persistChatToAPI(final Integer productId, final String chatUrl) throws JSONException {

        mProgressDialog = ProgressDialog.show(mContext, "", mContext.getString(R.string.all_dialog_loading), true);

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
                        mProgressDialog.dismiss();
                        Intent intent = new Intent(mContext, GroupChannelActivity.class);
                        intent.putExtra("channel_url", chatUrl);
                        mContext.startActivity(intent);
                        Activity activity = (Activity) mContext;
                        activity.overridePendingTransition(R.anim.left_out, R.anim.right_in);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressDialog.dismiss();
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

    private void hideProduct(int id) throws JSONException {

        // {host}/me/skipped_products
        String url = QuireAPI.BASE_URL + "/me" + QuireAPI.SKIPPED_PRODUCTS_ENPOINT;

        JSONObject productObj = new JSONObject();
        productObj.put("product_id", id);

        JSONObject skippedObj = new JSONObject();
        skippedObj.put("skipped_product", productObj);

        Log.d(LOG_TAG, skippedObj.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, skippedObj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(LOG_TAG, "Skipped res: " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG_TAG, "Skipped res: " + error.toString());
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

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(jsonObjectRequest);
    }
}
