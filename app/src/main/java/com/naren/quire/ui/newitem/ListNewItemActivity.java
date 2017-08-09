package com.naren.quire.ui.newitem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.naren.quire.R;
import com.naren.quire.ui.nearbyproducts.NearbyProductsActivity;
import com.bumptech.glide.Glide;
import com.kosalgeek.android.imagebase64encoder.ImageBase64;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ListNewItemActivity extends AppCompatActivity implements IListNewItemView {

    private final String LOG_TAG = ListNewItemActivity.this.getClass().getSimpleName();
    private Context mContext;
    private Toolbar mToolbar;
    private ListNewItemPresenter listNewItemPresenter;

    private ImageView mImageUploadOne, mImageUploadTwo, mImageUploadThree;
    private EditText mEditTextProductName, mEditTextProductDescription, mEditTextProductPrice;

    private File imgFile;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath, encodedBase64Image, product_name, product_description, product_price;
    private Button confirmListingBtn;
    private ProgressDialog mProgressDialog;

    private MaterialDialog.Builder builder;
    private MaterialDialog dialog;

    private int image_flag = 0;

    private ArrayList<String> imagesArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_new_item);
        mContext = this;
        setUpToolbar();
        initializeViews();

        if (listNewItemPresenter == null) {
            listNewItemPresenter = new ListNewItemPresenter(mContext);
        }

    }

    private ListNewItemPresenter getPresenter() {
        return listNewItemPresenter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPresenter().onViewAttached(ListNewItemActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPresenter().onViewDetached();
    }

    private void initializeViews() {

        mImageUploadOne = (ImageView) findViewById(R.id.image_one);
        mImageUploadTwo = (ImageView) findViewById(R.id.image_two);
        mImageUploadThree = (ImageView) findViewById(R.id.image_three);

        mEditTextProductName = (EditText) findViewById(R.id.edit_text_product_name);
        mEditTextProductDescription = (EditText) findViewById(R.id.edit_text_product_description);
        mEditTextProductPrice = (EditText) findViewById(R.id.edit_text_product_price);
        confirmListingBtn = (Button) findViewById(R.id.button_confirm);
        confirmListingBtn.setEnabled(false);

        mEditTextProductName.addTextChangedListener(mTextWatcher);
        mEditTextProductDescription.addTextChangedListener(mTextWatcher);
        mEditTextProductPrice.addTextChangedListener(mTextWatcher);

    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

            // check Fields For Empty Values
            checkFieldForEmptyValues();
        }
    };

    private void checkFieldForEmptyValues() {

        product_name = mEditTextProductName.getText().toString().trim();
        product_description = mEditTextProductDescription.getText().toString().trim();
        product_price = mEditTextProductPrice.getText().toString().trim();

        Log.d(LOG_TAG, "Name: " + product_name
                + "\nDesc: " + product_description
                + "\nprice: " + product_price);

        if (product_name.isEmpty() || product_description.isEmpty() || product_price.isEmpty()) {
            confirmListingBtn.setEnabled(false);
        } else {
            confirmListingBtn.setEnabled(true);
        }

        confirmListingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    getPresenter().createListing(product_name, product_description, product_price, imagesArrayList);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void showDialog() {
        mProgressDialog = ProgressDialog.show(mContext, getString(R.string.new_item_dialog_success), getString(R.string.all_dialog_loading), true);
    }

    @Override
    public void hideDialog() {
        mProgressDialog.dismiss();
    }

    @Override
    public void showSuccessDialog() {
        builder = new MaterialDialog.Builder(mContext)
                .title(R.string.view_single_dialog_title_success)
                .content(R.string.nearby_dialog_listing_created)
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

    @Override
    public void showRequiredImagesDialog() {
        builder = new MaterialDialog.Builder(mContext)
                .title(R.string.new_item_dialog_title_missing_image)
                .content(R.string.new_item_dialog_content_missing_image)
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
    public void showTimeOutError() {
        builder = new MaterialDialog.Builder(mContext)
                .title(R.string.all_dialog_timeout_error_title)
                .content(R.string.all_dialog_timeout_error_content)
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
                .content(R.string.all_dialog_timeout_error_content)
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpToolbar() {

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Add A New Listing");
        mToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        setSupportActionBar(mToolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    public void onClick(View view) throws IOException {

        switch (view.getId()) {

            case R.id.image_one:
                image_flag = 1;
                dispatchTakePictureIntent();
                break;

            case R.id.image_two:
                image_flag = 2;
                dispatchTakePictureIntent();
                break;

            case R.id.image_three:
                image_flag = 3;
                dispatchTakePictureIntent();
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
                    Glide.with(mContext).load(imgFile.getAbsoluteFile()).into(mImageUploadOne);
                    imagesArrayList.add(0, encodedBase64Image);
                } else if (image_flag == 2) {
                    Glide.with(mContext).load(imgFile.getAbsoluteFile()).into(mImageUploadTwo);
                    imagesArrayList.add(1, encodedBase64Image);
                } else {
                    Glide.with(mContext).load(imgFile.getAbsoluteFile()).into(mImageUploadThree);
                    imagesArrayList.add(2, encodedBase64Image);
                }
            }
        }
    }

}