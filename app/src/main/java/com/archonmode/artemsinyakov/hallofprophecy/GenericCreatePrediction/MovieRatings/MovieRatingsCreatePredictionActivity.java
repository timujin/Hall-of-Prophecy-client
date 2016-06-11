package com.archonmode.artemsinyakov.hallofprophecy.GenericCreatePrediction.MovieRatings;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.archonmode.artemsinyakov.hallofprophecy.GenericCreatePrediction.GenericCreatePredictionActivity;
import com.archonmode.artemsinyakov.hallofprophecy.InfiniteScroll.InfiniteScrollListView;
import com.archonmode.artemsinyakov.hallofprophecy.R;
import com.archonmode.artemsinyakov.hallofprophecy.ThemoviedbRequestHelper;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MovieRatingsCreatePredictionActivity extends GenericCreatePredictionActivity {

    private InfiniteScrollListView moviesListView;
    private MoviesListAdapter moviesListAdapter;
    private AsyncTask<Void, Void, List<MovieItem>> fetchAsyncTask;
    private ThemoviedbWrapper themoviedbWrapper;
    private boolean someMoviesLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_ratings_create_prediction);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setMoviesView();
    }

    private void setMoviesView() {
        final Context context = this;
        themoviedbWrapper = new ThemoviedbWrapper(this);
        moviesListView = (InfiniteScrollListView) findViewById(R.id.movie_list);
        if (moviesListView == null)
            return;
        moviesListView.setLoadingMode(InfiniteScrollListView.LoadingMode.SCROLL_TO_BOTTOM);
        moviesListView.setStopPosition(InfiniteScrollListView.StopPosition.REMAIN_UNCHANGED);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        moviesListView.setLoadingView(layoutInflater.inflate(R.layout.infinite_scroll_loading_view, null));

        moviesListAdapter = new MoviesListAdapter(new MoviesListAdapter.NewPageListener() {
            @Override
            public void onScrollNext() {
                fetchAsyncTask = new AsyncTask<Void, Void, List<MovieItem>>() {
                    @Override
                    protected void onPreExecute() {
                        moviesListAdapter.lock();
                    }
                    @Override
                    protected List<MovieItem> doInBackground(Void ... params) {
                        ArrayList<MovieItem> result = themoviedbWrapper.loadNextPage();
                        if (result != null && !result.isEmpty())
                            someMoviesLoaded = true;
                        return result;
                    }
                    @Override
                    protected void onPostExecute(List<MovieItem> result) {
                        if (!someMoviesLoaded)
                            Toast.makeText(context, "Could not download movies.", Toast.LENGTH_LONG).show();
                        if (isCancelled() || result == null || result.isEmpty()) {
                            moviesListAdapter.notifyEndOfList();
                        } else {
                            moviesListAdapter.addEntriesToBottom(result);
                            // Add or remove the loading view depend on if there might be more to load
                            if (themoviedbWrapper.isThereMore()) {
                                moviesListAdapter.notifyHasMore();
                            } else {
                                moviesListAdapter.notifyEndOfList();
                            }
                        }
                    };
                    @Override
                    protected void onCancelled() {
                        // Tell the adapter it is end of the list when task is cancelled
                        moviesListAdapter.notifyEndOfList();
                    }
                }.execute();
            }
            @Override
            public View getInfiniteScrollListView(int position, View convertView, ViewGroup parent) {
                // Customize the row for list view
                if(convertView == null) {
                    LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = layoutInflater.inflate(R.layout.movie_list_view_row, null);
                }
                MovieItem name = (MovieItem) moviesListAdapter.getItem(position);
                if (name != null) {
                    TextView rowName = (TextView) convertView.findViewById(R.id.testTextBox);
                    rowName.setText(name.getTitle());
                }
                return convertView; // TODO: movie view
            }
        });

        moviesListView.setAdapter(moviesListAdapter);
        moviesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // create prediction
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        // Load the first page to start demo
        moviesListAdapter.onScrollNext();

        /*AsyncHttpResponseHandler handler = new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.e("SUCCESS", "SUCCESS");
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("failure", String.valueOf(statusCode));
            }
        };
        ThemoviedbRequestHelper.getUpcomingMovies(0, handler);*/
    }
}
