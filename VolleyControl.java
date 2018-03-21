package com.example.android.healthme;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleyControl {

    private static VolleyControl mInstance;
    private RequestQueue mRequestQueue;
    private static Context mContext;

    private VolleyControl(Context context){
        mContext = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleyControl getInstance(Context context){
        if(mInstance == null){
            mInstance = new VolleyControl(context);
        }return mInstance;
    }

    public RequestQueue getRequestQueue(){

        // getApplicationContext() is key. It should not be activity context,
        // or else RequestQueue won't last for the lifetime of your app

        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }return mRequestQueue;
    }

    public void addToRequestQueue(Request request){
        getRequestQueue().add(request);
    }

}
