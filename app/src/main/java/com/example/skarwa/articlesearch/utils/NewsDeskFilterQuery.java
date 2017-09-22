package com.example.skarwa.articlesearch.utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by skarwa on 9/21/17.
 */

public class NewsDeskFilterQuery {
    String mKey;
    HashSet<String> mValues;

    public NewsDeskFilterQuery() {
        this.mKey = "news_desk";
        mValues = new HashSet<>();
    }

    public void addValue(String value){
        this.mValues.add(value);
    }

    public String getKey() {
        return mKey;
    }

   /* public String[] getValuesArray() {
        return mValues.toArray(new String[0]);
    }*/

    public HashSet<String> getValueSet(){
        return mValues;
    }

    public String getQueryString(){
        String values = TextUtils.join(", ", getValueSet());
        System.out.print(values); //TODO :remove for debug only
        return new String(getKey() + ":" +"(" + values +")");
    }
}
