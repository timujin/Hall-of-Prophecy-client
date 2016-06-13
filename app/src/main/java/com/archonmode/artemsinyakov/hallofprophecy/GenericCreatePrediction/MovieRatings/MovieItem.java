package com.archonmode.artemsinyakov.hallofprophecy.GenericCreatePrediction.MovieRatings;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.archonmode.artemsinyakov.hallofprophecy.ThemoviedbRequestHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.helpers.ParserAdapter;

import java.io.IOException;
import java.net.URL;

public class MovieItem {
    private JSONObject json;
    private Bitmap poster;
    private int counter = 0;

    public MovieItem(JSONObject json) {
        Log.e("CREATED", "CREATED");
        this.json = json;
    }

    public String getTitle() {
        try {
            return json.getString("title");
        } catch (JSONException e) {
            return "Error!";
        }
    }

    public String getReleaseDateFull() {
        try {
            return "Release: " + json.getString("release_date");
        } catch (JSONException e) {
            return "Error!";
        }
    }

    public String getReleaseDate() {
        try {
            return json.getString("release_date");
        } catch (JSONException e) {
            return "Error!";
        }
    }

    public Uri getPosterUri() {
        try {
            return Uri.parse(ThemoviedbRequestHelper.getImageUri(json.getString("poster_path")));
        } catch (JSONException e) {
            return Uri.EMPTY;
        }
    }

    public String getPosterUrl() {
        try {
            return ThemoviedbRequestHelper.getImageUri(json.getString("poster_path"));
        } catch (JSONException e) {
            return null;
        }
    }

    public void putPosterOnImageView(ImageView view, MovieItem posterCache) {
        final ImageView posterView = view;
        final MovieItem cache = posterCache;
        if (poster != null) {
            posterView.setImageBitmap(poster);
            return;
        }
        counter++;
        Log.e("REDOWNLOAD", String.valueOf(counter));

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                String posterUrl = getPosterUrl();
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(new URL(posterUrl).openConnection().getInputStream());
                    return bitmap;
                } catch (IOException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                posterView.setImageBitmap(bitmap);
                cache.cachePoster(bitmap);
            }
        }.execute();
    }

    public void cachePoster(Bitmap bitmap) {
        this.poster = bitmap;
    }

    public void downloadPoster() {
        // DO NOT CALL ON UI THREAD
        String posterUrl = getPosterUrl();
        try {
            poster = BitmapFactory.decodeStream(new URL(posterUrl).openConnection().getInputStream());
        } catch (IOException e) {
            poster = null;
        }
    }

    public Bitmap getPoster() {
        return poster;
    }
}
