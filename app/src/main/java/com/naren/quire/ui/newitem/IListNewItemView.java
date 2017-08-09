package com.naren.quire.ui.newitem;

public interface IListNewItemView {

    void showDialog();

    void hideDialog();

    void showRequiredImagesDialog();

    void showSuccessDialog();

    void showTimeOutError();

    void show500ServerError();

    void showNetworkError();

}
