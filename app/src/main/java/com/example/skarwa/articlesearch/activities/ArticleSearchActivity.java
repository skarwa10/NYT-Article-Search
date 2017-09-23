package com.example.skarwa.articlesearch.activities;

import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.skarwa.articlesearch.R;
import com.example.skarwa.articlesearch.adapters.ArticleAdapter;
import com.example.skarwa.articlesearch.decorators.SpacesItemDecoration;
import com.example.skarwa.articlesearch.fragments.FilterSettingsDialogFragment;
import com.example.skarwa.articlesearch.model.Article;
import com.example.skarwa.articlesearch.network.ArticleSearchClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

import android.support.v7.widget.SearchView;


import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.API_KEY;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.BEGIN_DATE;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.FILTER_FRAGMENT_TITLE;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.FILTER_QUERY;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.FILTER_SETTINGS;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.MY_API_KEY;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.NEWS_DESK;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.PAGE;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.QUERY;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.SORT;


public class ArticleSearchActivity extends AppCompatActivity implements FilterSettingsDialogFragment.SaveDialogListener {
    @BindView(R.id.rvResults)
    RecyclerView rvResults;

    SharedPreferences filterPrefs;
    String mBeginDate;
    String mSortOrder;
    HashSet<String> mNewsDeskValues;
    ArrayList<Article> mArticles;
    ArticleAdapter mArticleAdapter;
    ArticleSearchClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_search);

        ButterKnife.bind(this);

        // Restore filter settings from Shared Preferences
        filterPrefs = getSharedPreferences(FILTER_SETTINGS, 0);
        mSortOrder = filterPrefs.getString(SORT,null);
        mBeginDate = filterPrefs.getString(BEGIN_DATE,null);
        mNewsDeskValues = (HashSet<String>) filterPrefs.getStringSet(NEWS_DESK,null);

        mArticles = new ArrayList<>();
        mArticleAdapter = new ArticleAdapter(this,mArticles);

        //Adapter config
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,LinearLayoutManager.VERTICAL);
        staggeredGridLayoutManager.scrollToPosition(0);
        SpacesItemDecoration decoration = new SpacesItemDecoration(16);
        rvResults.addItemDecoration(decoration);
        rvResults.setLayoutManager(staggeredGridLayoutManager);
        rvResults.setAdapter(mArticleAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here

                //TODO : remove
                //Toast.makeText(getApplicationContext(),"searching for+"+query,Toast.LENGTH_LONG).show();

                mClient = new ArticleSearchClient();


                RequestParams params = new RequestParams();
                params.put(API_KEY,MY_API_KEY);
                params.put(PAGE,0);
                params.put(QUERY,query);
                if(mBeginDate != null){
                    params.put(BEGIN_DATE,mBeginDate);
                }

                if(mSortOrder != null){
                    params.put(SORT,mSortOrder);
                }

                if(mNewsDeskValues != null && mNewsDeskValues.size() >= 1){
                   params.put(FILTER_QUERY,getNewsDeskFilterQuery());
                }

                mClient.getArticles(params,new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        Log.d("DEBUG",response.toString());
                        JSONArray articleJsonResults = null;

                        try{
                            articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                            Log.d("DEBUG",articleJsonResults.toString());

                            int curSize = mArticleAdapter.getItemCount();
                            mArticles.addAll(Article.fromJsonArray(articleJsonResults));

                            mArticleAdapter.notifyItemRangeInserted(curSize, articleJsonResults.length());
                            Log.d("DEBUG",mArticles.toString());
                        } catch(JSONException e){
                            Log.d("ERROR",e.getMessage(),e);
                        }
                    }
                });

                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //TODO : should this be commented ?
        searchItem.expandActionView();
        searchView.requestFocus();


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_filter){
            launchFilterSettingDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    public void launchFilterSettingDialog(){
        FragmentManager fm = getSupportFragmentManager();
        FilterSettingsDialogFragment filterSettingsDialogFragment = FilterSettingsDialogFragment.newInstance(mBeginDate,mSortOrder, mNewsDeskValues);
        filterSettingsDialogFragment.show(fm,FILTER_FRAGMENT_TITLE);
    }


    @Override
    public void onFinishEditDialog(String beginDate, String sortOrder, HashSet<String> newsDeskValues) {
        mBeginDate = beginDate;
        mSortOrder = sortOrder;
        mNewsDeskValues =  newsDeskValues;

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences.Editor editor = filterPrefs.edit();
        editor.putString(SORT, sortOrder);
        editor.putString(BEGIN_DATE,beginDate);
        editor.putStringSet(NEWS_DESK,newsDeskValues);

        // Commit the edits!
        editor.commit();

        //TODO :remove
        String filterString = beginDate + sortOrder + newsDeskValues.toString();
        Toast.makeText(this,filterString,Toast.LENGTH_LONG).show();
    }

    public String getNewsDeskFilterQuery(){
        String values = "";
        for(String value:mNewsDeskValues){
            values = values.concat("\""+value+"\"").concat(" ");
        }

        System.out.print(values); //TODO :remove for debug only
        return new String(NEWS_DESK +":("+values+")");
    }
}
