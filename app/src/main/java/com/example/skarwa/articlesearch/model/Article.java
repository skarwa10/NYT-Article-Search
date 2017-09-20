package com.example.skarwa.articlesearch.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by skarwa on 9/19/17.
 */

public class Article {
    static final String IMAGE_PREFIX_URL = "http://www.nytimes.com/";

    String weburl;
    String headline;
    String thumbnail;
    ArticleType type;


    public enum ArticleType {
        HAS_IMAGE,
        NO_IMAGE;


    }

    public Article(JSONObject jsonObject)  {
        try {
            this.weburl = jsonObject.getString("web_url");
            this.headline = jsonObject.getJSONObject("headline").getString("main");
            JSONArray multimedia = jsonObject.getJSONArray("multimedia"); //TODO:handle error is multimedia is empty

            if(multimedia.length() > 0){
                JSONObject multimediaJSONObject = multimedia.getJSONObject(0);
                this.type = ArticleType.HAS_IMAGE;
                this.thumbnail = IMAGE_PREFIX_URL + multimediaJSONObject.getString("url");
                //loop and find subtype = 'thumbnail'
                for (int i=0;i<multimedia.length();i++){
                    String subtype = multimedia.getJSONObject(i).getString("subtype");
                    if(subtype.equalsIgnoreCase("thumbnail")){
                        this.thumbnail = IMAGE_PREFIX_URL + multimediaJSONObject.getString("url");
                    }
                }

            } else {
                //no media..put some placeholder here maybe
                this.thumbnail = "";
                this.type = ArticleType.NO_IMAGE;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Article> fromJsonArray(JSONArray array){
        ArrayList<Article> articles = new ArrayList<>();

        for(int i = 0;i < array.length() ; i++){
            try {
                articles.add(new Article(array.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return articles;
    }

    public String getWeburl() {
        return weburl;
    }

    public String getHeadline() {
        return headline;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public ArticleType getType() {
        return type;
    }
}
