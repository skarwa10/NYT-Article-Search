package com.example.skarwa.articlesearch.activities;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.skarwa.articlesearch.R;
import com.example.skarwa.articlesearch.adapters.ArticleAdapter;
import com.example.skarwa.articlesearch.decorators.SpacesItemDecoration;
import com.example.skarwa.articlesearch.fragments.FilterSettingsDialogFragment;
import com.example.skarwa.articlesearch.listeners.EndlessRecyclerViewScrollListener;
import com.example.skarwa.articlesearch.model.Article;
import com.example.skarwa.articlesearch.network.ArticleSearchClient;
import com.example.skarwa.articlesearch.wrapper.WrapStaggeredGridLayoutManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import static android.support.v4.view.MenuItemCompat.getActionView;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.API_KEY;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.BEGIN_DATE;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.FILTER_FRAGMENT_TITLE;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.FILTER_QUERY;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.FILTER_SETTINGS;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.MY_API_KEY;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.NEWS_DESK;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.PAGE;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.QUERY;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.SHARE_DESCRIPTION;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.SORT;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.TEXT_PLAIN_TYPE;


public class ArticleSearchActivity extends AppCompatActivity implements FilterSettingsDialogFragment.SaveDialogListener {
    @BindView(R.id.rvResults)
    RecyclerView rvResults;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    SharedPreferences filterPrefs;
    String mBeginDate;
    String mSortOrder;
    HashSet<String> mNewsDeskValues;
    ArrayList<Article> mArticles;
    ArticleAdapter mArticleAdapter;
    ArticleSearchClient mClient;
    String queryString;

    Handler mHandler;
    Runnable fetchArticlesThread;

    MenuItem miActionProgressItem;
    SearchView mSearchView;

    // Store a member variable for the listener
    EndlessRecyclerViewScrollListener mScrollListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_search);

        ButterKnife.bind(this);

        // Restore filter settings from Shared Preferences
        initFilterFromSharedPreferences();

        //init member variables
        initVariables();

        //initialize Recycler View
        initRecyclerView();

        setSupportActionBar(toolbar);
    }

    public void initFilterFromSharedPreferences() {
        filterPrefs = getSharedPreferences(FILTER_SETTINGS, 0);
        mSortOrder = filterPrefs.getString(SORT, null);
        mBeginDate = filterPrefs.getString(BEGIN_DATE, null);
        mNewsDeskValues = (HashSet<String>) filterPrefs.getStringSet(NEWS_DESK, null);
    }

    public void initVariables() {
        mArticles = new ArrayList<>();
        mArticleAdapter = new ArticleAdapter(this, mArticles);
        mArticleAdapter.setOnArticleClickListener(article -> {
            int requestCode = 100;
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_share);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(TEXT_PLAIN_TYPE);
            intent.putExtra(Intent.EXTRA_TEXT, article.getWebURL());

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(Color.rgb(3, 169, 244)); //same as tab color
            builder.setActionButton(bitmap, SHARE_DESCRIPTION, pendingIntent, true);
            CustomTabsIntent customTabsIntent = builder.build();

            customTabsIntent.launchUrl(getApplicationContext(), Uri.parse(article.getWebURL()));
        });
        mClient = new ArticleSearchClient();
        mHandler = new Handler();
    }

    public void initRecyclerView() {
        StaggeredGridLayoutManager staggeredGridLayoutManager = new WrapStaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        staggeredGridLayoutManager.scrollToPosition(0);
        SpacesItemDecoration decoration = new SpacesItemDecoration(16);
        mScrollListener = new EndlessRecyclerViewScrollListener(staggeredGridLayoutManager) {
            public void onLoadMore(final int page, int totalItemsCount, final RecyclerView view) {
                // Define the code block to be executed
               fetchArticlesThread = () -> {
                    int curSize = mArticleAdapter.getItemCount();

                    fetchArticles(page);

                    // Delay before notifying the adapter since the scroll listeners
                    // can be called while RecyclerView data cannot be changed.
                    view.post(() -> {
                        // Notify adapter with appropriate notify methods
                        if(mArticles.size() > curSize){
                            mArticleAdapter.notifyItemRangeInserted(curSize, mArticles.size() - 1);
                        }
                    });
                    // Do something here on the main thread
                    Log.d("Handlers", "Called on main thread");
                };
                // Run the above code block on the main thread after 2 seconds
                mHandler.postDelayed(fetchArticlesThread,2000);
            }
        };
        rvResults.addOnScrollListener(mScrollListener);
        rvResults.addItemDecoration(decoration);
        rvResults.setLayoutManager(staggeredGridLayoutManager);
        rvResults.setAdapter(mArticleAdapter);
    }


    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the activity_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        mSearchView = (SearchView) getActionView(searchItem);
        mSearchView.setOnQueryTextListener(createQueryTextListener());
        mSearchView.setOnCloseListener(createOnCloseListener());

        searchItem.expandActionView();
        mSearchView.requestFocus();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        // Extract the action-view from the menu item
        ProgressBar v = (ProgressBar) getActionView(miActionProgressItem);
        // Return to finish
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_filter:
                launchFilterSettingDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void launchFilterSettingDialog() {
        FragmentManager fm = getSupportFragmentManager();
        FilterSettingsDialogFragment filterSettingsDialogFragment = FilterSettingsDialogFragment.newInstance(mBeginDate, mSortOrder, mNewsDeskValues);
        filterSettingsDialogFragment.show(fm, FILTER_FRAGMENT_TITLE);
    }

    public void fetchArticles(int page) {
        Log.d("DEBUG", " Loading Page:" + page);

        mClient.getArticles(getParams(page), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("DEBUG", response.toString());
                JSONArray articleJsonResults;

                try {
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    Log.d("DEBUG", articleJsonResults.toString());

                    mArticles.addAll(Article.fromJsonArray(articleJsonResults));

                    if (mArticles.size() > 0) {
                        mArticleAdapter.notifyItemRangeInserted(0, mArticles.size() - 1);
                    }

                    Log.d("DEBUG", mArticles.toString());
                } catch (JSONException e) {
                    Log.e("ERROR", e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // Handle the failure and alert the user to retry
                if (statusCode == 429) {
                    Log.e("ERROR", "API Limit Exceeded ...." + errorResponse.toString(), throwable);
                    mHandler.postDelayed(fetchArticlesThread,2000);
                }
                Log.e("ERROR", errorResponse.toString(), throwable);
            }
        });
    }



    @Override
    public void onFinishEditDialog(String beginDate, String sortOrder, HashSet<String> newsDeskValues) {
        mBeginDate = beginDate;
        mSortOrder = sortOrder;
        mNewsDeskValues = newsDeskValues;

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences.Editor editor = filterPrefs.edit();
        editor.putString(SORT, sortOrder);
        editor.putString(BEGIN_DATE, beginDate);
        editor.putStringSet(NEWS_DESK, newsDeskValues);

        // Commit the edits!
        editor.apply();

        //Do query again using filters
        mSearchView.setQuery(queryString, true);
    }

    public String getNewsDeskFilterQuery() {
        String values = "";
        for (String value : mNewsDeskValues) {
            values = values.concat("\"" + value + "\"").concat(" ");
        }
        return new String(NEWS_DESK + ":(" + values + ")");
    }

    public SearchView.OnQueryTextListener createQueryTextListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here

                showProgressBar();

                //clear Adapter
                mArticleAdapter.clear();
                // 3. Reset endless scroll listener when performing a new search
                mScrollListener.resetState();

                queryString = query;

                fetchArticles(0);

                hideProgressBar();
                mSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };
    }

    public SearchView.OnCloseListener createOnCloseListener() {
        return () -> {
            Log.i("SearchView:", "onClose");
            mSearchView.onActionViewCollapsed();
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            return false;
        };
    }

    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        miActionProgressItem.setVisible(false);
    }

    private RequestParams getParams(int page) {

        RequestParams params = new RequestParams();
        params.put(API_KEY, MY_API_KEY);
        params.put(PAGE, page);
        params.put(QUERY, queryString);
        if (mBeginDate != null) {
            params.put(BEGIN_DATE, mBeginDate);
        }
        if (mSortOrder != null) {
            params.put(SORT, mSortOrder);
        }

        if (mNewsDeskValues != null && mNewsDeskValues.size() >= 1) {
            params.put(FILTER_QUERY, getNewsDeskFilterQuery());
        }
        return params;
    }

}

