package com.example.skarwa.articlesearch.utils;

import java.text.SimpleDateFormat;

/**
 * Created by skarwa on 9/21/17.
 */

public interface ArticleSearchConstants {
    String MY_API_KEY = "2db4291298b44a99b50c6636a9f49964";
    String BEGIN_DATE = "begin_date";
    String NEWS_DESK = "news_desk";
    String FILTER_QUERY = "fq";
    String SORT = "sort";
    String FILTER_FRAGMENT_TITLE = "Search Filter";
    String API_KEY = "api-key";
    String PAGE = "page";
    String QUERY = "query";
    String NYT_SEARCH_API_URL = "http://api.nytimes.com/svc/search/v2/articlesearch.json";
    String NYT_IMAGE_PREFIX_URL = "http://www.nytimes.com/";
    String DATE_FORMAT_ON_DATE_PICKER = "MM/dd/yyyy";
    String DATE_FORMAT_FOR_QUERY = "yyyyMMdd";
    String WEB_URL = "web_url";
    String MULTIMEDIA = "multimedia";
    String SUB_TYPE = "subtype";
    String MULTIMEDIA_URL = "url";
    String HEADLINE = "headline";
    String THUMBNAIL = "thumbnail";
    String MAIN = "main";
    String FILTER_SETTINGS = "MyFilterPrefsFile";
    String TEXT_PLAIN_TYPE = "text/plain";
    String SHARE_DESCRIPTION = "Share Link";
    SimpleDateFormat datePickerFormatter = new SimpleDateFormat(
            DATE_FORMAT_ON_DATE_PICKER);
    SimpleDateFormat queryDateFormatter = new SimpleDateFormat(
            DATE_FORMAT_FOR_QUERY);
}
