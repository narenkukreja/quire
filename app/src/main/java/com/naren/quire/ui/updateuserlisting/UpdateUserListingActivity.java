package com.naren.quire.ui.updateuserlisting;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.naren.quire.R;
import com.naren.quire.data.Image;
import com.naren.quire.data.Product;
import com.naren.quire.ui.nearbyproducts.NearbyProductsActivity;
import com.bumptech.glide.Glide;
import com.kosalgeek.android.imagebase64encoder.ImageBase64;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class UpdateUserListingActivity extends AppCompatActivity implements IUpdateUserListingView {

    private final String LOG_TAG = UpdateUserListingActivity.this.getClass().getSimpleName();
    private Context mContext;
    private UpdateUserListingPresenter editUserListingPresenter;

    private ImageView mImageOne, mImageTwo, mImageThree;
    private EditText mEditTextProductName, mEditTextProductDescription, mEditTextProductPrice;

    private Product product;
    private int product_id, image_flag;
    private String product_name, product_description, product_price_string;
    private Double product_price_double;
    private List<Image> images;
    private Button mButtonUpdateListing;

    private Toolbar mToolbar;

    private File imgFile;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath, encodedBase64Image;

    boolean changeImageBtnClicked = false;

    private ProgressDialog mProgressDialog;

    private MaterialDialog.Builder builder;
    private MaterialDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_listing);
        mContext = this;
        //setUpToolbar();
        setStatusBarColor();
        initializeViews();

        if (editUserListingPresenter == null) {
            editUserListingPresenter = new UpdateUserListingPresenter(mContext);
        }

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {

            product = (Product) bundle.getSerializable("product");

            if (product != null) {

                product_id = product.getId();
                product_name = product.getName();
                product_price_string = product.getPrice();
                product_price_double = Double.parseDouble(product.getPrice());
                product_description = product.getDescription();

                images = product.getImages();

                int length = images.size();

                Log.d(LOG_TAG, "Length: " + length);

                switch (length) {

                    case 1:
                        Glide.with(mContext).load(images.get(0).getUrl()).into(mImageOne);
                        break;

                    case 2:
                        Glide.with(mContext).load(images.get(0).getUrl()).into(mImageOne);
                        Glide.with(mContext).load(images.get(1).getUrl()).into(mImageTwo);
                        break;

                    case 3:
                        Glide.with(mContext).load(images.get(0).getUrl()).into(mImageOne);
                        Glide.with(mContext).load(images.get(1).getUrl()).into(mImageTwo);
                        Glide.with(mContext).load(images.get(2).getUrl()).into(mImageThree);
                        break;

                }

                mEditTextProductName.setText(product_name);
                mEditTextProductDescription.setText(product_description);
                mEditTextProductPrice.setText(String.format("%.2f", product_price_double));

            } else {
                Log.d(LOG_TAG, "Product object is null");
            }
        }

        checkFieldForEmptyValues();
    }

    private UpdateUserListingPresenter getPresenter() {
        return editUserListingPresenter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPresenter().onViewAttached(UpdateUserListingActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPresenter().onViewDetached();
    }

    private void initializeViews() {
        mImageOne = (ImageView) findViewById(R.id.image_one);
        mImageTwo = (ImageView) findViewById(R.id.image_two);
        mImageThree = (ImageView) findViewById(R.id.image_three);
        mEditTextProductName = (EditText) findViewById(R.id.edit_text_product_name);
        mEditTextProductDescription = (EditText) findViewById(R.id.edit_text_product_description);
        mEditTextProductPrice = (EditText) findViewById(R.id.edit_text_product_price);
        mButtonUpdateListing = (Button) findViewById(R.id.button_update_listing);
    }

    private void checkFieldForEmptyValues() {

        mButtonUpdateListing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                product_name = mEditTextProductName.getText().toString();
                product_description = mEditTextProductDescription.getText().toString();
                product_price_string = mEditTextProductPrice.getText().toString();

                try {
                    getPresenter().updateItem(product_id, product_name, product_description, product_price_string,
                            images);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void showDialog() {
        mProgressDialog = ProgressDialog.show(mContext, "Updating Listing", "Loading...", true);
    }

    @Override
    public void hideDialog() {
        mProgressDialog.dismiss();
    }

    @Override
    public void showSuccessDialog() {
        builder = new MaterialDialog.Builder(mContext)
                .title("Success")
                .content("Your listing has been updated!")
                .positiveText("Continue").onPositive(new MaterialDialog.SingleButtonCallback() {
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

    @Override
    public void showTimeOutError() {
        builder = new MaterialDialog.Builder(mContext)
                .title("Timeout Error")
                .content("Please check your internet connection and try again.")
                .positiveText("Aceptar").onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        dialog.dismiss();

                    }
                });

        dialog = builder.build();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void show500ServerError() {
        builder = new MaterialDialog.Builder(mContext)
                .title(R.string.all_dialog_server_error)
                .content(R.string.all_dialog_try_again)
                .positiveText(R.string.all_dialog_positive).onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        dialog.dismiss();

                    }
                });

        dialog = builder.build();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void showNetworkError() {
        builder = new MaterialDialog.Builder(mContext)
                .title(R.string.all_dialog_network_error_title)
                .content(R.string.all_dialog_try_again)
                .positiveText(R.string.all_dialog_positive).onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });

        dialog = builder.build();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    public void onClick(View view) throws IOException {

        Intent intent;
        Bundle bundle;
        int options = 0;

        switch (view.getId()) {

            case R.id.image_one:

//                if (images.size() == 1) {
//                    options = R.array.image_option_single;
//                } else if (images.size() == 2) {
//                    options = R.array.image_option;
//                }

                options = R.array.image_option_single;

                Log.d(LOG_TAG, "Details 1: " + images.get(0).toString());

                builder = new MaterialDialog.Builder(mContext)
                        .items(options)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int whichItem, CharSequence text) {

                                switch (whichItem) {

                                    case 0:
                                        image_flag = 1;
                                        try {
                                            dispatchTakePictureIntent();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case 1:
                                        mImageOne.invalidate();
                                        Glide.with(mContext).load(R.drawable.ic_add_white_24dp).into(mImageOne);
                                        images.get(0).setDeleted(true);
                                        images.remove(0);
                                        break;
                                }
                            }
                        });
                dialog = builder.build();
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);

                break;

            case R.id.image_two:

//                if (images.size() == 1) {
//                    options = R.array.image_option_single;
//                } else if (images.size() == 2) {
//                    options = R.array.image_option;
//                }

                Log.d(LOG_TAG, "Details 2: " + images.get(0).toString());

                options = R.array.image_option_single;

                builder = new MaterialDialog.Builder(mContext)
                        .items(options)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int whichItem, CharSequence text) {

                                switch (whichItem) {

                                    case 0:
                                        image_flag = 2;
                                        try {
                                            dispatchTakePictureIntent();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case 1:
                                        mImageOne.invalidate();
                                        Glide.with(mContext).load(R.drawable.ic_add_white_24dp).into(mImageTwo);
                                        images.get(1).setDeleted(true);
                                        images.remove(1);
                                        break;
                                }
                            }
                        });
                dialog = builder.build();
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);

                break;

            case R.id.image_three:

//                if (images.size() == 1) {
//                    options = R.array.image_option_single;
//                } else if (images.size() == 2) {
//                    options = R.array.image_option;
//                }

                Log.d(LOG_TAG, "Details 3: " + images.get(0).toString());

                options = R.array.image_option_single;

                builder = new MaterialDialog.Builder(mContext)
                        .items(options)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int whichItem, CharSequence text) {

                                switch (whichItem) {

                                    case 0:

                                        image_flag = 3;

                                        try {
                                            dispatchTakePictureIntent();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        break;
                                    case 1:
                                        mImageOne.invalidate();
                                        Glide.with(mContext).load(R.drawable.ic_add_white_24dp).into(mImageThree);
                                        images.get(2).setDeleted(true);
                                        images.remove(1);
                                        break;
                                }
                            }
                        });
                dialog = builder.build();
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);

                break;

        }
    }

    private void dispatchTakePictureIntent() throws IOException {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.naren.quire.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imgFile = new File(mCurrentPhotoPath);

            if (imgFile.exists()) {

                Log.d(LOG_TAG, "\nimage file path: " + imgFile.getAbsolutePath());

                try {
                    encodedBase64Image = ImageBase64.with(getApplicationContext())
                            .requestSize(512, 512)
                            .encodeFile(imgFile.getAbsolutePath());

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (image_flag == 1) {
                    Glide.with(mContext).load(imgFile.getAbsoluteFile()).into(mImageOne);
                    images.get(0).setUrl(encodedBase64Image);
                    images.get(0).setImage_changed(true);
                    //images.get(0).setDeleted(false);
                } else if (image_flag == 2) {
                    Glide.with(mContext).load(imgFile.getAbsoluteFile()).into(mImageTwo);
                    images.get(1).setUrl(encodedBase64Image);
                    images.get(1).setImage_changed(true);
                    //images.get(1).setDeleted(false);
                } else {
                    Glide.with(mContext).load(imgFile.getAbsoluteFile()).into(mImageThree);
                    images.get(2).setUrl(encodedBase64Image);
                    images.get(2).setImage_changed(true);
                    //images.get(2).setDeleted(false);
                }
            }
        }
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
        mToolbar.setTitle("Edit Listing");
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