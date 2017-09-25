package com.example.skarwa.articlesearch.wrapper;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by skarwa on 9/24/17.
 */

public class WrapStaggeredGridLayoutManager extends StaggeredGridLayoutManager {

    public WrapStaggeredGridLayoutManager(int spanCount, int orientation) {
        super(spanCount, orientation);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        }catch(IndexOutOfBoundsException e){
            Log.e("ERROR",e.getMessage());
        }
    }
}
