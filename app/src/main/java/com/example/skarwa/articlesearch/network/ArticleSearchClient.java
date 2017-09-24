package com.example.skarwa.articlesearch.network;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.NYT_SEARCH_API_URL;

/**
 *  Client class used for Network calls
 */
public class ArticleSearchClient {
    private AsyncHttpClient client;

    public ArticleSearchClient() {
        this.client = new AsyncHttpClient();
    }

    // Method for accessing the search API
    public void getArticles(final RequestParams params, JsonHttpResponseHandler handler) {
        client.get(NYT_SEARCH_API_URL,params,handler);
    }
}
