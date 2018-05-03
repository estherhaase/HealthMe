package com.example.android.healthme;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import com.example.android.healthme.DataModel.Leg;

import java.util.ArrayList;

public class RouteDetail extends Fragment {

    RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    ArrayList<Leg> footPath, ridePath, wholePath;
    NumberPicker np;
    FloatingActionButton fab;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_route_detail, container, false);
        recyclerView = view.findViewById(R.id.my_recycler);
        np = container.findViewById(R.id.number_picker);

        footPath = getArguments().getParcelableArrayList("footpath");
        ridePath = getArguments().getParcelableArrayList("ridepath");
        wholePath = new ArrayList<>();
        wholePath.add(footPath.get(0));
        wholePath.addAll(ridePath);
        wholePath.add(footPath.get(1));
        linearLayoutManager = new LinearLayoutManager(view.getContext(), 1, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        CustomAdapter adapter = new CustomAdapter(getActivity().getApplicationContext(), wholePath);
        recyclerView.setAdapter(adapter);

        return view;


    }

    @Override
    public void onStop() {
        np.bringToFront();

        super.onStop();
    }
}
