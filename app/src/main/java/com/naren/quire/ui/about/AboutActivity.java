package com.naren.quire.ui.about;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import com.naren.quire.BuildConfig;
import com.naren.quire.R;

public class AboutActivity extends AppCompatActivity {

    private final String LOG_TAG = AboutActivity.this.getClass().getSimpleName();
    private Context mContext;
    private Toolbar mToolbar;

    private TextView mTextViewAbout, mTextViewAppVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        mContext = this;
        setUpToolbar();
        initializeViews();

    }

    private void initializeViews() {
        mTextViewAbout = (TextView) findViewById(R.id.text_about);
        mTextViewAbout.setText(fromHtml(getString(R.string.about_text_open_source_libraries)));
        mTextViewAbout.setMovementMethod(LinkMovementMethod.getInstance());

        String version = BuildConfig.VERSION_NAME;
        mTextViewAppVersion = (TextView) findViewById(R.id.text_app_version);
        mTextViewAppVersion.setText("Version: " + version);
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
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
        mToolbar.setTitle("About");
        setSupportActionBar(mToolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
}
