package com.example.skarwa.articlesearch.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.skarwa.articlesearch.R;
import com.example.skarwa.articlesearch.utils.NewsDeskFilterQuery;
import com.example.skarwa.articlesearch.utils.SortOrder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.BEGIN_DATE;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.DATE_FORMAT;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.FILTER_FRAGMENT_TITLE;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.NEWS_DESK;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.SORT;

import android.widget.TextView;

/**
 * Created by skarwa on 9/20/17.
 */

public class FilterSettingsDialogFragment extends DialogFragment implements TextView.OnEditorActionListener,DatePickerFragment.DatePickerFragmentListener {
    @BindView(R.id.etBeginDateField)
    EditText etBeginDate;

    @BindView(R.id.spSortOrder)
    Spinner spSortOrder;

    @BindView(R.id.cbArts)
    CheckBox cbArts;

    @BindView(R.id.cbFashingStyle)
    CheckBox cbFashionStyle;

    @BindView(R.id.cbSports)
    CheckBox cbSports;

    @BindView(R.id.btnSave)
    Button btnSave;

    @BindString(R.string.arts)
    String arts;

    @BindString(R.string.fashion_style)
    String fashionAndStyle;

    @BindString(R.string.sports)
    String sports;

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        return false;
    }

    @Override
    public void onDateSet(Date date) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(
                DATE_FORMAT);
        String strDate = dateFormatter.format(date);

        etBeginDate.setText(strDate);
    }

    public interface SaveDialogListener {
        void onFinishEditDialog(String beginDate, SortOrder sortOrder, NewsDeskFilterQuery query);
    }

    public FilterSettingsDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static FilterSettingsDialogFragment newInstance(String date, SortOrder sortOrder, NewsDeskFilterQuery filterQuery) {
        FilterSettingsDialogFragment frag = new FilterSettingsDialogFragment();

        Bundle args = new Bundle();
        args.putString(BEGIN_DATE, date);
        args.putSerializable(SORT, sortOrder);
        args.putSerializable(NEWS_DESK,filterQuery.getValueSet());
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_filter, null);
        ButterKnife.bind(this, view);

        String beginDate = getArguments().getString(BEGIN_DATE);
        SortOrder sort = (SortOrder)getArguments().getSerializable(SORT);
        HashSet<String> newsDeskSelectedOptions = (HashSet<String>)getArguments().getSerializable(NEWS_DESK);

        if(beginDate!=null){
            etBeginDate.setText(beginDate);
        }
        if(sort!= null) {
            spSortOrder.setSelection(getIndex(spSortOrder,sort.name()));
        }

        etBeginDate.requestFocus();
        etBeginDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }

        });

        cbArts.setChecked(false);
        cbFashionStyle.setChecked(false);
        cbSports.setChecked(false);

        if(newsDeskSelectedOptions !=  null){
            if(newsDeskSelectedOptions.contains(fashionAndStyle)) {
                cbFashionStyle.setChecked(true);
            }

            if(newsDeskSelectedOptions.contains(arts)){
                cbArts.setChecked(true);
            }

            if(newsDeskSelectedOptions.contains(sports)){
                cbSports.setChecked(true);
            }
        }

        getDialog().setTitle(FILTER_FRAGMENT_TITLE);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String beginDate = etBeginDate.getText().toString();
                String  sort = spSortOrder.getSelectedItem().toString();
                NewsDeskFilterQuery queryFilter = new NewsDeskFilterQuery();
                if(cbArts.isChecked()){
                    queryFilter.addValue(arts);
                }
                if(cbFashionStyle.isChecked()){
                    queryFilter.addValue(fashionAndStyle);
                }
                if(cbSports.isChecked()){
                    queryFilter.addValue(sports);
                }
                SaveDialogListener listener = (SaveDialogListener) getActivity();
                listener.onFinishEditDialog(beginDate,SortOrder.valueOf(sort.toUpperCase()),queryFilter);
                dismiss();
            }
        });
        return view;
    }

    public void showDatePickerDialog(View v) {
        DatePickerFragment fragment = DatePickerFragment.newInstance(this);
        fragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    //private method of your class
    private int getIndex(Spinner spinner, String item)
    {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(item)){
                index = i;
                break;
            }
        }
        return index;
    }


}
