package com.example.android.healthme;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StationActivity extends Fragment {

    @BindView(R.id.btn_station)
    Button btn_station;
    @BindView(R.id.actv_stations)
    AutoCompleteTextView actv_stations;
    ArrayList<String> stations;
    ArrayList<Integer> rbls;
    MyDataBaseHelper helper;
    OnStationClickListener mCallback;

    /**
     * populates dropdown list of autocompletetextview with station data from SQLite database
     * sets onClickListener on OK button and checks input*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_station, container, false);
        ButterKnife.bind(this, view);
        helper = MyDataBaseHelper.getsInstance(getActivity().getApplicationContext());
        stations = helper.getAllStationNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.list_view, stations);
        actv_stations.setAdapter(adapter);

        btn_station.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = actv_stations.getText().toString();
                int id = helper.getStationId(name);
                rbls = helper.getRBLs(id);

                if (id == 0) {

                    Toast.makeText(getActivity(), "Station does not exist!", Toast.LENGTH_SHORT).show();

                    actv_stations.getText().clear();
                } else if (rbls != null) {
                    mCallback.onStationSelected(rbls);
                } else {

                    Toast.makeText(getActivity(), "Data is incomplete for this station", Toast.LENGTH_SHORT).show();
                    actv_stations.getText().clear();
                }



            }
        });

        return view;
    }

    public interface OnStationClickListener {
        void onStationSelected(ArrayList<Integer> rbls2);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnStationClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnStationClickListener");
        }

    }
}
