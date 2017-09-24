package com.example.skarwa.articlesearch.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by skarwa on 9/9/17.
 */

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    private DatePickerFragmentListener mDatePickerListener;

    interface DatePickerFragmentListener {
       void onDateSet(Date date);
    }

    public DatePickerFragment(){
        //empty constructor
    }

    public static DatePickerFragment newInstance(DatePickerFragmentListener listener) {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setDatePickerListener(listener);
        return fragment;
    }

    public void setDatePickerListener(DatePickerFragmentListener listener) {
        this.mDatePickerListener = listener;
    }

    protected void notifyDatePickerListener(Date date) {
        if (this.mDatePickerListener == null) {
            return;
        }
        this.mDatePickerListener.onDateSet(date);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
       Calendar c = Calendar.getInstance();
        c.set(year, month, day);

        Date date  = c.getTime();
        // Here we call the listener and pass the date back to it.
        notifyDatePickerListener(date);
    }
}