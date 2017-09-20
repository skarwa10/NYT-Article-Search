package com.example.skarwa.articlesearch.activities;

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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.example.skarwa.articlesearch.R;
import com.example.skarwa.articlesearch.adapters.ArticleAdapter;
import com.example.skarwa.articlesearch.decorators.SpacesItemDecoration;
import com.example.skarwa.articlesearch.model.Article;
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


public class ArticleSearchActivity extends AppCompatActivity {
    @BindView(R.id.rvResults)
    RecyclerView rvResults;

    ArrayList<Article> articles;
    ArticleAdapter articleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_search);

        ButterKnife.bind(this);
        articles = new ArrayList<>();
        articleAdapter = new ArticleAdapter(this,articles);

        //Adapter config
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,LinearLayoutManager.VERTICAL);
        staggeredGridLayoutManager.scrollToPosition(0);
        SpacesItemDecoration decoration = new SpacesItemDecoration(16);
        rvResults.addItemDecoration(decoration);
        rvResults.setLayoutManager(staggeredGridLayoutManager);
        rvResults.setAdapter(articleAdapter);

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

                Toast.makeText(getApplicationContext(),"searching for+"+query,Toast.LENGTH_LONG).show();

                AsyncHttpClient client = new AsyncHttpClient();
                String nyTimesSearchAPIURL = "http://api.nytimes.com/svc/search/v2/articlesearch.json";

                RequestParams params = new RequestParams();
                params.put("api-key","2db4291298b44a99b50c6636a9f49964");
                params.put("page",0);
                params.put("query",query);

                client.get(nyTimesSearchAPIURL,params,new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        Log.d("DEBUG",response.toString());
                        JSONArray articleJsonResults = null;

                        try{
                            articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                            Log.d("DEBUG",articleJsonResults.toString());

                            int curSize = articleAdapter.getItemCount();
                            articles.addAll(Article.fromJsonArray(articleJsonResults));
                            //articleAdapter.notifyDataSetChanged();

                            articleAdapter.notifyItemRangeInserted(curSize, articleJsonResults.length());
                            Log.d("DEBUG",articles.toString());
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
            launchFilterView();
        }
        return super.onOptionsItemSelected(item);
    }

    public void launchFilterView(){
        //Handler Filter here
    }


}
