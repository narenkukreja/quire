package com.naren.quire.ui.profile.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.naren.quire.R;
import com.naren.quire.data.Image;
import com.naren.quire.ui.singleuserlisting.ViewSingleUserListingActivity;
import com.naren.quire.data.Product;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class UserListingsAdapter extends RecyclerView.Adapter<UserListingsAdapter.CustomViewHolder> {

    private final String LOG_TAG = UserListingsAdapter.this.getClass().getSimpleName();
    private View view;
    private Context mContext;
    private ArrayList<Product> productArrayList;

    public UserListingsAdapter(Context mContext, ArrayList<Product> productArrayList) {
        this.mContext = mContext;
        this.productArrayList = productArrayList;
    }

    @Override
    public UserListingsAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        view = LayoutInflater.from(mContext).inflate(R.layout.single_listing, parent, false);

        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserListingsAdapter.CustomViewHolder holder, int position) {

        final Product product = productArrayList.get(position);

        List<Image> product_images = product.getImages();

        Glide.with(mContext).load(product_images.get(0).getUrl()).into(holder.mainProductImageView);

        holder.mainProductImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ViewSingleUserListingActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("product", product);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
                Activity activity = (Activity) mContext;
                activity.overridePendingTransition(R.anim.left_out, R.anim.right_in);

            }
        });

    }

    @Override
    public int getItemCount() {

        return productArrayList.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        private ImageView mainProductImageView;

        public CustomViewHolder(View itemView) {
            super(itemView);

            mainProductImageView = (ImageView) itemView.findViewById(R.id.image_product);
        }
    }
}