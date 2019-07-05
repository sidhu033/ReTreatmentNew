package com.redox.localdb.models;

import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

/**
 * Created by Sumit on 5/23/2016.
 */
public class TreatmentLog
{

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private int leftHandSystolic;

    @DatabaseField
    private int leftHandDiastolic;

    @DatabaseField
    private int leftHandPulse;

    @DatabaseField
    private int rightHandSystolic;

    @DatabaseField
    private int rightHandDiastolic;

    @DatabaseField
    private int rightHandPulse;

    @DatabaseField
    private Date treatmentStartTimeStamp;

    @DatabaseField
    private Date treatmentEndTimeStamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLeftHandSystolic() {
        return leftHandSystolic;
    }

    public void setLeftHandSystolic(int leftHandSystolic) {
        this.leftHandSystolic = leftHandSystolic;
    }

    public int getLeftHandDiastolic() {
        return leftHandDiastolic;
    }

    public void setLeftHandDiastolic(int leftHandDiastolic) {
        this.leftHandDiastolic = leftHandDiastolic;
    }

    public int getLeftHandPulse() {
        return leftHandPulse;
    }

    public void setLeftHandPulse(int leftHandPulse) {
        this.leftHandPulse = leftHandPulse;
    }

    public int getRightHandSystolic() {
        return rightHandSystolic;
    }

    public void setRightHandSystolic(int rightHandSystolic) {
        this.rightHandSystolic = rightHandSystolic;
    }

    public int getRightHandDiastolic() {
        return rightHandDiastolic;
    }

    public void setRightHandDiastolic(int rightHandDiastolic) {
        this.rightHandDiastolic = rightHandDiastolic;
    }

    public int getRightHandPulse() {
        return rightHandPulse;
    }

    public void setRightHandPulse(int rightHandPulse) {
        this.rightHandPulse = rightHandPulse;
    }

    public Date getTreatmentStartTimeStamp() {
        return treatmentStartTimeStamp;
    }

    public void setTreatmentStartTimeStamp(Date treatmentStartTimeStamp) {
        this.treatmentStartTimeStamp = treatmentStartTimeStamp;
    }

    public Date getTreatmentEndTimeStamp() {
        return treatmentEndTimeStamp;
    }

    public void setTreatmentEndTimeStamp(Date treatmentEndTimeStamp) {
        this.treatmentEndTimeStamp = treatmentEndTimeStamp;
    }

    @Override
    public String toString() {
        return "TreatmentLog{" +
                "id=" + id +
                ", leftHandSystolic=" + leftHandSystolic +
                ", leftHandDiastolic=" + leftHandDiastolic +
                ", leftHandPulse=" + leftHandPulse +
                ", rightHandSystolic=" + rightHandSystolic +
                ", rightHandDiastolic=" + rightHandDiastolic +
                ", rightHandPulse=" + rightHandPulse +
                ", treatmentStartTimeStamp=" + treatmentStartTimeStamp +
                ", treatmentEndTimeStamp=" + treatmentEndTimeStamp +
                '}';
    }
}
