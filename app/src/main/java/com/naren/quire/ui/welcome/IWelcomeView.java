package com.naren.quire.ui.welcome;

import android.content.Context;

public interface IWelcomeView {

    void startNearbyProductsActivity();

    void getPermissions();

    void displayLocationSettingsRequest(Context mContext);

    void showDialog();

    void hideDialog();

    void show500ServerError();

    void showNoInternetConnectionError();

}
