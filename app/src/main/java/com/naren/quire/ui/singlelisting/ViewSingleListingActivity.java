package com.naren.quire.ui.singlelisting;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.naren.quire.R;
import com.naren.quire.data.Image;
import com.naren.quire.util.app.Quire;
import com.naren.quire.data.Product;
import com.naren.quire.ui.chat.GroupChannelActivity;
import com.bumptech.glide.Glide;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.mzelzoghbi.zgallery.ZGallery;
import com.mzelzoghbi.zgallery.entities.ZColor;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBirdException;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class ViewSingleListingActivity extends AppCompatActivity implements IViewSingleListingView {

    private final String LOG_TAG = ViewSingleListingActivity.this.getClass().getSimpleName();
    private Context mContext;
    private ViewSingleListingPresenter viewSingleListingPresenter;
    private ProgressDialog mProgressDialog;

    private TextView mTextViewProductName, mTextViewProductDescription, mTextViewProductPrice, mTextViewProductSellerName;

    private Product product;

    private int product_id;
    private List<Image> images;
    ArrayList<String> imagesList = new ArrayList<>();

    private CircleImageView mImageSeller;

    private Button mButtonMessageSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_listing);
        mContext = this;
        //setUpToolbar();
        setStatusBarColor();
        initializeViews();

        if (viewSingleListingPresenter == null) {
            viewSingleListingPresenter = new ViewSingleListingPresenter(mContext);
        }

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {

            product = (Product) bundle.getSerializable("product");

            if (product != null) {

                product_id = product.getId();

                String flag = bundle.getString("flag");

                if (flag != null) {
                    mButtonMessageSeller.setVisibility(View.GONE);
                }

                String product_name = product.getName();
                Double product_price_double = Double.parseDouble(product.getPrice());
                String product_description = product.getDescription();
                String seller_name = product.getSeller().getName();
                String seller_profile_picture = product.getSeller().getProfilePicture();
                images = product.getImages();

                Glide.with(mContext).load(seller_profile_picture).into(mImageSeller);

                mTextViewProductName.setText(product_name);
                mTextViewProductSellerName.setText(seller_name);
                mTextViewProductPrice.setText("$" + String.format("%.2f", product_price_double));
                mTextViewProductDescription.setText(product_description);

                String[] seller_name_array = seller_name.split(" ");

                mButtonMessageSeller.setText("Message " + seller_name_array[0]);

            } else {
                Log.d(LOG_TAG, "Product object is null");
            }
        }

        try {
            setUpSlideShow();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ViewSingleListingPresenter getPresenter() {
        return viewSingleListingPresenter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPresenter().onViewAttached(ViewSingleListingActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPresenter().onViewDetached();
    }

    private void initializeViews() {
        mTextViewProductName = (TextView) findViewById(R.id.text_product_name);
        mTextViewProductDescription = (TextView) findViewById(R.id.text_product_description);
        mTextViewProductPrice = (TextView) findViewById(R.id.text_product_price);
        mTextViewProductSellerName = (TextView) findViewById(R.id.text_seller_name);
        mImageSeller = (CircleImageView) findViewById(R.id.image_seller);
        mButtonMessageSeller = (Button) findViewById(R.id.button_message_seller);
    }

    private void setUpSlideShow() throws JSONException {

        SliderLayout sliderShow = (SliderLayout) findViewById(R.id.slider);

        if (images.size() == 1) {

            DefaultSliderView defaultSliderView = new DefaultSliderView(this);

            defaultSliderView
                    .image(images.get(0).getUrl()).setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                @Override
                public void onSliderClick(BaseSliderView slider) {

                    for (Image image: images) {
                        imagesList.add(image.getUrl());
                    }

                    ZGallery.with(ViewSingleListingActivity.this, imagesList)
                            .setToolbarTitleColor(ZColor.WHITE)
                            .setGalleryBackgroundColor(ZColor.BLACK)
                            .setToolbarColorResId(R.color.colorPrimary)
                            .setTitle(product.getName())
                            .show();
                }
            });

            sliderShow.addSlider(defaultSliderView);
            sliderShow.stopAutoCycle();

        } else {

            for (int i = 0; i < images.size(); i++) {

                DefaultSliderView defaultSliderView = new DefaultSliderView(this);

                defaultSliderView
                        .image(images.get(i).getUrl()).setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                    @Override
                    public void onSliderClick(BaseSliderView slider) {

                        for (Image image: images) {
                            imagesList.add(image.getUrl());
                        }

                        ZGallery.with(ViewSingleListingActivity.this, imagesList)
                                .setToolbarTitleColor(ZColor.WHITE)
                                .setGalleryBackgroundColor(ZColor.BLACK)
                                .setToolbarColorResId(R.color.colorPrimary)
                                .setTitle(product.getName())
                                .show();

                    }
                });
                sliderShow.addSlider(defaultSliderView);
            }
        }
    }

    public void onClick(View view) throws JSONException {
        switch (view.getId()) {

            case R.id.button_message_seller:

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
                        product.getImages().get(0).getUrl(),
                        chatExtraData.toString(),
                        new GroupChannel.GroupChannelCreateHandler() {
                            @Override
                            public void onResult(GroupChannel groupChannel, SendBirdException e) {

                                if (e != null) {
                                    Toasty.error(mContext, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                try {
                                    getPresenter().persistChatToAPI(product_id, groupChannel.getUrl());
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        });
                break;
        }
    }

    @Override
    public void showDialog() {
        mProgressDialog = ProgressDialog.show(mContext, "", getString(R.string.all_dialog_loading), true);
    }

    @Override
    public void hideDialog() {
        mProgressDialog.dismiss();
    }

    @Override
    public void startChatActivity(String chatUrl) {
        Intent intent = new Intent(mContext, GroupChannelActivity.class);
        intent.putExtra("channel_url", chatUrl);
        startActivity(intent);
        overridePendingTransition(R.anim.left_out, R.anim.right_in);

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

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Listing");
        mToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
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
}