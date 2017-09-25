package com.example.skarwa.articlesearch.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;

import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.HEADLINE;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.MAIN;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.MULTIMEDIA;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.MULTIMEDIA_URL;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.NEW_DESK;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.NYT_IMAGE_PREFIX_URL;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.SUB_TYPE;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.THUMBNAIL;
import static com.example.skarwa.articlesearch.utils.ArticleSearchConstants.WEB_URL;

/**
 * Created by skarwa on 9/19/17.
 *
 * Article model to save data from API
 */
@Parcel
public class Article {
    String mWebURL;
    String mHeadline;
    String mThumbnail;
    ArticleTypeEnum mArticleType;
    String mNewDesk;

    /**
     * Created by skarwa on 9/24/17.
     */

    public static enum ArticleTypeEnum {
        HAS_IMAGE,
        NO_IMAGE
    }

    Article(){

    }

    private Article(JSONObject jsonObject)  {
        try {
            this.mWebURL = jsonObject.getString(WEB_URL);
            this.mHeadline = jsonObject.getJSONObject(HEADLINE).getString(MAIN);
            if(jsonObject.has(NEW_DESK)){
                this.mNewDesk = jsonObject.getString(NEW_DESK);
            } else{
                this.mNewDesk = "No Category";
            }
            JSONArray multimedia = jsonObject.getJSONArray(MULTIMEDIA);

            if(multimedia.length() > 0){
                JSONObject multimediaJSONObject = multimedia.getJSONObject(0);
                this.mArticleType = ArticleTypeEnum.HAS_IMAGE;
                this.mThumbnail = NYT_IMAGE_PREFIX_URL + multimediaJSONObject.getString(MULTIMEDIA_URL);
                //loop and find subtype = 'thumbnail'
                for (int i=0;i<multimedia.length();i++){
                    String subtype = multimedia.getJSONObject(i).getString(SUB_TYPE);
                    if(subtype.equalsIgnoreCase(THUMBNAIL)){
                        this.mThumbnail = NYT_IMAGE_PREFIX_URL + multimediaJSONObject.getString(MULTIMEDIA_URL);
                    }
                }
            } else {
                //no media..put some placeholder here maybe
                this.mThumbnail = "";
                this.mArticleType = ArticleTypeEnum.NO_IMAGE;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Article> fromJsonArray(JSONArray array){
        ArrayList<Article> articles = new ArrayList<>();

        for(int i = 0;i < array.length() ; i++) {
            try {
                articles.add(new Article(array.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return articles;
    }

    public String getWebURL() {
        return mWebURL;
    }

    public String getHeadline() {
        return mHeadline;
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    public ArticleTypeEnum getArticleType() {
        return mArticleType;
    }

    public String getNewDesk() {
        return mNewDesk;
    }
}
