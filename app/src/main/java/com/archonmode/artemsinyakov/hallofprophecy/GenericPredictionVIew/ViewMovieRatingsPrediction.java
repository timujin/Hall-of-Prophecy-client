package com.archonmode.artemsinyakov.hallofprophecy.GenericPredictionVIew;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.archonmode.artemsinyakov.hallofprophecy.GenericCreatePrediction.MovieRatings.MovieItem;
import com.archonmode.artemsinyakov.hallofprophecy.R;
import com.archonmode.artemsinyakov.hallofprophecy.ThemoviedbRequestHelper;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ViewMovieRatingsPrediction extends ViewGenericPrediction {


    private ImageView moviePoster;
    private boolean lock = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.unusualViewsAccountedFor = true;
        setContentView(R.layout.activity_view_movie_ratings_prediction);
        moviePoster = (ImageView) findViewById(R.id.movie_poster);
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void populateUIElements() {
        super.populateUIElements();
        loadMoviePoster();
    }

    private void loadMoviePoster() {
        moviePoster.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.no_poster_w185, null));
        lock = true;
        new AsyncTask<ImageView, Void, Bitmap>() {
            private Bitmap moviePosterBitmap;
            private ImageView view;

            @Override
            protected Bitmap doInBackground(ImageView ... params) {
                view = params[0];
                ThemoviedbRequestHelper.searchForMovieByTitle(((MovieRatingsPrediction) prediction).getTitle(), new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            Log.e("POSTER", "SUCCESS");
                            JSONObject json = new JSONObject(new String(responseBody));
                            JSONObject movie = json.getJSONArray("results").getJSONObject(0);
                            MovieItem item = new MovieItem(movie);
                            item.downloadPoster();
                            moviePosterBitmap = item.getPoster();
                            lock = false;
                        } catch (JSONException e) {
                            Log.e("POSTER", "ERROR");
                            moviePosterBitmap = null;
                            lock = false;
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.e("POSTER", "FAILURE");
                        moviePosterBitmap = null;
                        lock = false;
                    }
                });

                try {
                    while (lock) {
                        Log.e("SLEEP", "SLEEP");
                        Thread.sleep(100L);
                    }
                } catch (InterruptedException e) {
                    return null;
                }
                return moviePosterBitmap;
            }

            @Override
            protected void onPostExecute(Bitmap value) {
                if (value != null)
                    view.setImageBitmap(value);
            }
        }.execute(moviePoster);
    }
}
