package com.archonmode.artemsinyakov.hallofprophecy;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.entity.StringEntity;


public class ThemoviedbRequestHelper {

    private static final String BASE_URL = "http://api.themoviedb.org";
    private static final String apiKey = "e39ba2b1bbc4afffdf0b2df5dd9d3684";
    private static AsyncHttpClient client = new SyncHttpClient();

    public static void setUp() {
        client.setTimeout(5);
        client.setResponseTimeout(5);
        client.setConnectTimeout(5);
        client.setMaxRetriesAndTimeout(5,5);
        client.setUserAgent("com.example.artemsinyakov.hallofprophecy.0.0.1");
        Log.e("1", "set up themoviedb");
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        RequestParams withkey = params;
        withkey.add("api_key", apiKey);
        client.get(getAbsoluteUrl(url), withkey, responseHandler);
    }

    public static void getUpcomingMovies(RequestParams params, AsyncHttpResponseHandler responseHandler) {
        get("/3/movie/upcoming", params, responseHandler);
    }

    public static void getUpcomingMovies(int page, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        RequestParams withpage = params;
        withpage.add("page", String.valueOf(page));
        getUpcomingMovies(withpage, responseHandler);
    }

    public static void getUpcomingMovies(int page, AsyncHttpResponseHandler responseHandler) {
        RequestParams withpage = new RequestParams();
        if (page > 0)
            withpage.add("page", String.valueOf(page));
        getUpcomingMovies(withpage, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
