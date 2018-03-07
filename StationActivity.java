package com.example.android.healthme;

import android.app.Fragment;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StationActivity extends Fragment {

    @BindView(R.id.btn_station)
    Button btn_station;
    @BindView(R.id.actv_stations)
    AutoCompleteTextView actv_stations;
    ArrayList<String>stations;
    MyDataBaseHelper helper;
    OnStationClickListener mCallback;

    public interface OnStationClickListener{
        void onStationSelected(ArrayList<Integer> rbls2);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_station, container, false);
        ButterKnife.bind(this, view);
        helper = MyDataBaseHelper.getsInstance(getActivity().getApplicationContext());
        stations =  helper.getAllStationNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.list_view, stations);
        actv_stations.setAdapter(adapter);

        btn_station.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = actv_stations.getText().toString();
                int id = helper.getStationId(name);
                mCallback.onStationSelected(helper.getRBLs(id));
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mCallback = (OnStationClickListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement OnStationClickListener");
        }

    }
}
