package com.naren.quire.ui.welcome.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.naren.quire.R;

public class WelcomeFragment extends Fragment {

    private final String LOG_TAG = WelcomeFragment.this.getClass().getSimpleName();

    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private OnFragmentInteractionListener mListener;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    public static WelcomeFragment newInstance(String param1) {
        WelcomeFragment fragment = new WelcomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = null;

        switch (mParam1){

            case "one":
                view = inflater.inflate(R.layout.fragment_welcome_1, container, false);
                break;

            case "two":
                view = inflater.inflate(R.layout.welcome_2, container, false);
                break;

            case "three":
                view = inflater.inflate(R.layout.welcome_3, container, false);
                break;

            case "four":
                view = inflater.inflate(R.layout.welcome_4, container, false);
                break;

        }

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}