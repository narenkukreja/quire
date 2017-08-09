package com.naren.quire.ui.updateuserlisting;

public interface IUpdateUserListingView {

    void showDialog();

    void hideDialog();

    void showSuccessDialog();

    void showTimeOutError();

    void show500ServerError();

    void showNetworkError();
}
