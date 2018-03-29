package com.example.android.healthme;

import android.app.Fragment;
import android.app.LauncherActivity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MedicalActivity extends Fragment {

   /* @BindView(R.id.btn_hospital)
    Button btn_hospital;

    @BindView(R.id.actv_hospitals)
    AutoCompleteTextView actv_hospitals;*/
    @BindView(R.id.list_hospitals)
    ListView list_hospital;
    ArrayList<String>hospitals;
    MyDataBaseHelper helper;
    OnHospitalClickListener mCallback;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //View view =  inflater.inflate(R.layout.activity_medical, container, false);
        View view = inflater.inflate(R.layout.activity_medical2, container, false);
        ButterKnife.bind(this,view );

        helper = MyDataBaseHelper.getsInstance(getActivity().getApplicationContext());
        hospitals = helper.getAllHospitalNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.list_view, hospitals);
        //actv_hospitals.setAdapter(adapter);
        list_hospital.setAdapter(adapter);

        list_hospital.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name = ((TextView)view).getText().toString();
                if(helper.hospitalExists(name)){
                    mCallback.onHospitalSelected(helper.getHospitalLocation(name), name);
                }
                else {
                    Toast.makeText(getActivity(), "Hospital doesn't exist, please choose from list!", Toast.LENGTH_LONG).show();
                }
            }
        });


       /* btn_hospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              String name = actv_hospitals.getText().toString();
              if(helper.hospitalExists(name)){
                  mCallback.onHospitalSelected(helper.getHospitalLocation(name));
              }
              else {
                  Toast.makeText(getActivity(), "Hospital doesn't exist, please choose from list!", Toast.LENGTH_LONG).show();
              }

            }
        });*/


        return view;


    }

    public interface OnHospitalClickListener{
        void onHospitalSelected(Location selected, String name);

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
