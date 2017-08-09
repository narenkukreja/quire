package com.naren.quire.ui.singleuserlisting;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.naren.quire.data.Image;
import com.naren.quire.ui.updateuserlisting.UpdateUserListingActivity;
import com.naren.quire.ui.nearbyproducts.NearbyProductsActivity;
import com.naren.quire.R;
import com.naren.quire.data.Product;
import com.bumptech.glide.Glide;
import com.mzelzoghbi.zgallery.ZGallery;
import com.mzelzoghbi.zgallery.entities.ZColor;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class ViewSingleUserListingActivity extends AppCompatActivity implements IViewSingleUserListingView {

    private final String LOG_TAG = ViewSingleUserListingActivity.this.getClass().getSimpleName();
    private Context mContext;

    private SingleUserListingPresenter singleUserListingPresenter;

    private List<Image> images;
    private Product product;
    private Toolbar mToolbar;
    private ImageView mImageViewProduct;
    private TextView mTextViewProductName, mTextViewProductPrice, mTextViewProductDescription;

    private int product_id;
    private String product_name, product_description;
    private Double product_price;

    private MaterialDialog.Builder builder;
    private MaterialDialog dialog;

    private ProgressDialog mProgressDialog;

    private ArrayList<String> imagesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_user_listing);
        mContext = this;
        setUpToolbar();
        //setStatusBarColor();
        initializeView();

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {

            product = (Product) bundle.getSerializable("product");

            if (product != null) {

                product_id = product.getId();
                product_name = product.getName();
                product_price = Double.parseDouble(product.getPrice());
                product_description = product.getDescription();

                images = product.getImages();
                Glide.with(mContext).load(images.get(0).getUrl()).into(mImageViewProduct);

                mTextViewProductName.setText(product_name);
                mTextViewProductPrice.setText("$" + String.format("%.2f", product_price));
                mTextViewProductDescription.setText(product_description);

                for (Image image : images) {
                    imagesList.add(image.getUrl());
                }

                mImageViewProduct.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ZGallery.with(ViewSingleUserListingActivity.this, imagesList)
                                .setToolbarTitleColor(ZColor.WHITE)
                                .setGalleryBackgroundColor(ZColor.BLACK)
                                .setToolbarColorResId(R.color.colorPrimary)
                                .setTitle(product_name)
                                .show();
                    }
                });

            } else {
                Log.d(LOG_TAG, "Product object is null");
            }
        }

        if (singleUserListingPresenter == null) {
            singleUserListingPresenter = new SingleUserListingPresenter(mContext);
        }
    }

    public SingleUserListingPresenter getPresenter() {
        return singleUserListingPresenter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPresenter().onViewAttached(ViewSingleUserListingActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPresenter().onViewDetached();
    }

    @Override
    public void showDialog() {
        mProgressDialog = ProgressDialog.show(mContext, getString(R.string.view_single_dialog_deleting_product), getString(R.string.all_dialog_loading), true);
    }

    @Override
    public void hideDialog() {
        mProgressDialog.dismiss();
    }

    @Override
    public void showSuccessDialog() {
        builder = new MaterialDialog.Builder(mContext)
                .title(R.string.view_single_dialog_title_success)
                .content(R.string.view_single_dialog_content_success)
                .positiveText(R.string.view_single_dialog_positive_continue).onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        dialog.dismiss();
                        startActivity(new Intent(mContext, NearbyProductsActivity.class));
                        overridePendingTransition(R.anim.left_out, R.anim.right_in);

                    }
                });

        dialog = builder.build();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    private void initializeView() {
        mImageViewProduct = (ImageView) findViewById(R.id.image_product);
        mTextViewProductName = (TextView) findViewById(R.id.text_product_name);
        mTextViewProductPrice = (TextView) findViewById(R.id.text_product_price);
        mTextViewProductDescription = (TextView) findViewById(R.id.text_product_description);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpToolbar() {

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Listing");
        setSupportActionBar(mToolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
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

    public void onClick(View view) {

        Intent intent;
        Bundle bundle;

        switch (view.getId()) {

            case R.id.image_edit:

                intent = new Intent(mContext, UpdateUserListingActivity.class);
                bundle = new Bundle();
                bundle.putSerializable("product", product);
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(R.anim.left_out, R.anim.right_in);

                break;

            case R.id.image_delete:

                builder = new MaterialDialog.Builder(mContext)
                        .title("Delete Listing")
                        .content("Are you sure you want to delete this listing?")
                        .positiveText("Yes").onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                dialog.dismiss();
                                getPresenter().deleteListing(product_id);

                            }
                        }).negativeText("No").onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        });

                dialog = builder.build();
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);

                break;

        }
    }
}
