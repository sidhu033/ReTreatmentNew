package com.redox.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.redox.R;
import com.redox.localdb.DatabaseHelper;
import com.redox.localdb.DbHandler;
import com.redox.localdb.models.Log;
import com.redox.ui.adapters.TreatmentLogsAdapter;

import java.util.ArrayList;


public class SummaryFragment extends Fragment {

    public SummaryFragment() {
        // Required empty public constructor
    }

    private RecyclerView rvTreatmentLogs;
    private TextView tvMessage;
    private ArrayList<Log> mTreatmentLogs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView= inflater.inflate(R.layout.fragment_summary, container, false);

        tvMessage= rootView.findViewById(R.id.tv_logs_message);

        rvTreatmentLogs= rootView.findViewById(R.id.rv_logs);

        rvTreatmentLogs.setLayoutManager(new LinearLayoutManager(getActivity()));
        populateTreatmentLogs();
        return rootView;

    }

    @Override
    public void onResume() {
        super.onResume();
        populateTreatmentLogs();
    }

    private void populateTreatmentLogs() {
        try {
          //  DbHandler db = new DbHandler(getContext());
            //data from database is returned to m
            //final ArrayList<Log> m = (ArrayList<Log>) db.getAllUsers();
          //  android.util.Log.w("SIZe", "" + m.size());
           // db.close();

            mTreatmentLogs= (ArrayList<Log>) DbHandler.getInstance(getActivity()).getAllUsers();

            if (mTreatmentLogs.isEmpty()){
                tvMessage.setVisibility(View.VISIBLE);
                rvTreatmentLogs.setVisibility(View.GONE);
            }else{
                tvMessage.setVisibility(View.GONE);
                rvTreatmentLogs.setVisibility(View.VISIBLE);
                rvTreatmentLogs.setAdapter(new TreatmentLogsAdapter(mTreatmentLogs, getActivity()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SummaryFragment newInstance() {
        return new SummaryFragment();
    }
}
