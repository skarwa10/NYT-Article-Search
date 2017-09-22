package com.example.skarwa.articlesearch.activities;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
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
import com.example.skarwa.articlesearch.utils.NewsDeskFilterQuery;
import com.example.skarwa.articlesearch.utils.SortOrder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import android.support.v7.widget.SearchView;


import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.API_KEY;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.FILTER_FRAGMENT_TITLE;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.PAGE;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.QUERY;


public class ArticleSearchActivity extends AppCompatActivity implements FilterSettingsDialogFragment.SaveDialogListener {
    @BindView(R.id.rvResults)
    RecyclerView rvResults;

    NewsDeskFilterQuery mFilterQuery;
    String mBeginDate;
    SortOrder mSortOrder;
    ArrayList<Article> mArticles;
    ArticleAdapter mArticleAdapter;
    ArticleSearchClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_search);

        ButterKnife.bind(this);
        mFilterQuery = new NewsDeskFilterQuery();
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
                params.put(API_KEY,"2db4291298b44a99b50c6636a9f49964");
                params.put(PAGE,0);
                params.put(QUERY,query);
                params.put("begin_date","20170120"); //TODO: change hardcoded values
                params.put("sort",mSortOrder.name().toLowerCase());
                params.put("fq",mFilterQuery.getQueryString());

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
                            //mArticlesAdapter.notifyDataSetChanged();

                            mArticleAdapter.notifyItemRangeInserted(curSize, articleJsonResults.length());
                            Log.d("DEBUG",mArticles.toString());
                        } catch(JSONException e){
                            e.printStackTrace();
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
        FilterSettingsDialogFragment filterSettingsDialogFragment = FilterSettingsDialogFragment.newInstance(mBeginDate,mSortOrder, mFilterQuery);
        filterSettingsDialogFragment.show(fm,FILTER_FRAGMENT_TITLE);
    }


    @Override
    public void onFinishEditDialog(String beginDate, SortOrder sortOrder, NewsDeskFilterQuery query) {
        this.mFilterQuery = query;
        this.mBeginDate = beginDate;
        this.mSortOrder = sortOrder;

        String filterString = beginDate + sortOrder + mFilterQuery.getValueSet().toString();
        Toast.makeText(this,filterString,Toast.LENGTH_LONG).show(); //TODO :remove
    }
}
