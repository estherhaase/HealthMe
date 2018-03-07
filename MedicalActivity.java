package com.example.android.healthme;

import android.app.Fragment;
import android.content.Context;
import android.location.Location;
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
import butterknife.OnClick;

public class MedicalActivity extends Fragment {

    @BindView(R.id.btn_hospital)
    Button btn_hospital;

    @BindView(R.id.actv_hospitals)
    AutoCompleteTextView actv_hospitals;
    ArrayList<String>hospitals;
    MyDataBaseHelper helper;
    OnHospitalClickListener mCallback;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.activity_medical, container, false);
        ButterKnife.bind(this,view );

        helper = MyDataBaseHelper.getsInstance(getActivity().getApplicationContext());
        hospitals = helper.getAllHospitalNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.list_view, hospitals);
        actv_hospitals.setAdapter(adapter);


        btn_hospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              String name = actv_hospitals.getText().toString();
              mCallback.onHospitalSelected(helper.getHospitalLocation(name));

            }
        });


        return view;


    }

    public interface OnHospitalClickListener{
        void onHospitalSelected(Location selected);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mCallback = (OnHospitalClickListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement OnHospitalClickListener");
        }
    }
}
