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
import retrofit2.http.POST;

public class MedicalActivity extends Fragment {

    @BindView(R.id.list_hospitals)
    ListView list_hospital;
    ArrayList<String>hospitals;
    MyDataBaseHelper helper;
    OnHospitalClickListener mCallback;

    /**
     * retrieves all hospitals from SQLite database
     * populates listview via adapter
     * sets onClickListener and passes name of clicked hospital*/
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_medical2, container, false);
        ButterKnife.bind(this,view );

        helper = MyDataBaseHelper.getsInstance(getActivity().getApplicationContext());
        hospitals = helper.getAllHospitalNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.list_view, hospitals);
        if(hospitals != null){
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
        }
        return view;
    }

    public interface OnHospitalClickListener{

        void onHospitalSelected(Location selected, String name);

    }

    /**
     * gets called when fragment is attached to a activity
     * ensures that activity implements OnHospitalClickListener and onHospitalSelected() method*/
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
