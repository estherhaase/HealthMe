package com.example.android.healthme;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SplashActivity extends Fragment{

    OnStartAppListener mCallback;

    public interface OnStartAppListener{
        void onStartApp();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_splash, container, false);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCallback.onStartApp();
            }
        }, 3000);



        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnStartAppListener) context;
        } catch (ClassCastException e) {
           throw new ClassCastException(context.toString() + " must implement OnStartAppListener");
        }
    }
}