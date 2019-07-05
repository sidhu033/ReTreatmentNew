package com.redox.ui.adapters

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.redox.R
import com.redox.localdb.models.Log
import com.redox.localdb.models.TreatmentLog
//import com.redox.localdb.models.Log
import kotlinx.android.synthetic.main.template_treatment_log_item.view.*
import java.text.SimpleDateFormat
import java.util.ArrayList


class TreatmentLogsAdapter(val items: ArrayList<Log>, val activity: Activity) : RecyclerView.Adapter<TreatmentLogsAdapter.ViewHolder>() {

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.template_treatment_log_item, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val cursorOnTreatmentLog: Log= items.get(position)

        val format = SimpleDateFormat("dd/MM/yyy hh:mm a")

        holder.tvStartTime.setText(format.format(cursorOnTreatmentLog?.treatmentStartTimeStamp))
        //holder.tvEndTime.setText(format.format(cursorOnTreatmentLog?.treatmentEndTimeStamp))
        holder.tvLeftHandDia.setText(cursorOnTreatmentLog?.leftHandDiastolic?.toString());
        holder.tvLeftHandSys.setText(cursorOnTreatmentLog?.leftHandSystolic?.toString());
        holder.tvLeftHandPulse.setText(cursorOnTreatmentLog?.leftHandPulse?.toString());
        holder.tvRightHandDia.setText(cursorOnTreatmentLog?.rightHandDiastolic?.toString());
        holder.tvRightHandSys.setText(cursorOnTreatmentLog?.rightHandSystolic?.toString());
        holder.tvRightHandPulse.setText(cursorOnTreatmentLog?.rightHandPulse?.toString());
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvStartTime = view.tv_start_time;
        //val tvEndTime = view.tv_end_time
        val tvLeftHandDia = view.tv_left_hand_dia
        val tvLeftHandSys = view.tv_left_hand_sys
        val tvLeftHandPulse = view.tv_left_hand_pulse
        val tvRightHandDia = view.tv_right_hand_dia
        val tvRightHandSys = view.tv_right_hand_sys
        val tvRightHandPulse = view.tv_right_hand_pulse
    }

}