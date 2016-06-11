package com.archonmode.artemsinyakov.hallofprophecy.GenericCreatePrediction.MovieRatings;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.archonmode.artemsinyakov.hallofprophecy.ThemoviedbRequestHelper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ThemoviedbWrapper {

    public int currentPage = 0;
    public int maxPages;
    private Context context;
    private ArrayList<MovieItem> loadedMovies;
    private boolean lock = false;

    public ThemoviedbWrapper(Context context) {
        this.context = context;
    }

    public ArrayList<MovieItem> loadNextPage() {
        if (currentPage > 0 && currentPage == maxPages)
            return null;
        lock = true;
        Looper.prepare();
        AsyncHttpResponseHandler handler = new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    Log.e("SUCCESS", "SUCCESS");
                    JSONObject json = new JSONObject(new String(responseBody));
                    ArrayList<MovieItem> load = new ArrayList<>();
                    currentPage = json.getInt("page");
                    maxPages = json.getInt("total_pages");
                    JSONArray results = json.getJSONArray("results");
                    for (int i = 0; i<results.length(); i++) {
                        load.add(new MovieItem(results.getJSONObject(i)));
                    }
                    loadedMovies = load;
                    lock = false;
                } catch (JSONException e) {
                    Log.e("JSON", e.toString());
                    Toast.makeText(context, "Could not load movies.", Toast.LENGTH_LONG).show();
                    loadedMovies = null;
                    lock = false;
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                //Log.e("FAILEURE", new String(responseBody));
                Toast.makeText(context, "Can't load more movies.", Toast.LENGTH_LONG).show();
                loadedMovies = null;
                lock = false;
            }
        };
        ThemoviedbRequestHelper.getUpcomingMovies(currentPage+1, handler);
        try {
            while (lock) {
                Log.e("SLEEP", "SLEEP");
                Thread.sleep(200L);
            }
        } catch (InterruptedException e) {
            return null;
        }
        return loadedMovies;
    }

    public boolean isThereMore() {
        return currentPage == 0 || currentPage < maxPages;
    }
}
