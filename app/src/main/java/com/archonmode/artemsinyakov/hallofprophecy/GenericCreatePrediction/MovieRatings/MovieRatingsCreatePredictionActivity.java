package com.archonmode.artemsinyakov.hallofprophecy.GenericCreatePrediction.MovieRatings;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.archonmode.artemsinyakov.hallofprophecy.GenericCreatePrediction.GenericCreatePredictionActivity;
import com.archonmode.artemsinyakov.hallofprophecy.GenericPredictionVIew.ViewGenericPrediction;
import com.archonmode.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.archonmode.artemsinyakov.hallofprophecy.InfiniteScroll.InfiniteScrollListAdapter;
import com.archonmode.artemsinyakov.hallofprophecy.InfiniteScroll.InfiniteScrollListView;
import com.archonmode.artemsinyakov.hallofprophecy.R;
import com.archonmode.artemsinyakov.hallofprophecy.SeriesOfPopups.INextPopupPlease;
import com.archonmode.artemsinyakov.hallofprophecy.SeriesOfPopups.PickAPredictionDialog;
import com.archonmode.artemsinyakov.hallofprophecy.SeriesOfPopups.SeriesOfPopups;
import com.archonmode.artemsinyakov.hallofprophecy.ThemoviedbRequestHelper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.twitter.sdk.android.Twitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MovieRatingsCreatePredictionActivity extends GenericCreatePredictionActivity {

    private InfiniteScrollListView moviesListView;
    private MoviesListAdapter moviesListAdapter;
    private AsyncTask<Void, Void, List<MovieItem>> fetchAsyncTask;
    private ThemoviedbWrapper themoviedbWrapper;
    private boolean someMoviesLoaded = false;
    private String type = "movieRatings";

    private SeriesOfPopups seriesOfPopups;
    private int dialogNum = 0;
    ArrayList<AlertDialog> dialogs;
    boolean dialogsConstructed = false;
    boolean extraDataLoaded = false;
    MovieItem moviePicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_ratings_create_prediction);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Select a movie");
        setMoviesView();
        generateSeriesOfPopups();


    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                moviesListAdapter.filter(newText);
                return false;
            }
        });

        /*SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
        }*/
        return super.onCreateOptionsMenu(menu);
    }

    private void generateSeriesOfPopups() {
        dialogNum = 0;
        dialogsConstructed = false;
        extraDataLoaded = false;
        final MovieRatingsCreatePredictionActivity context = this;
        seriesOfPopups = PickAPredictionDialog.constructSeriesOfPopups(type + "_wager", this, new INextPopupPlease() {
            @Override
            public void releasePopup() {
                callUponDialog();
            }
            public void popupFailure() {
                dialogNum -= 1;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        callUponDialog();
                    }
                }, 500);
            }
            public void startRolling() {
                extraDataLoaded = true;
                if (dialogsConstructed)
                    context.startRolling();
            }
            public void cancel() {
                finish();
            }
        });

        dialogsConstructed = true;

    }

    private void startRolling() {
        dialogs = seriesOfPopups.getDialogs();
        callUponDialog();
    }

    private void callUponDialog() {
        if (dialogs.size() <= dialogNum) {
            try {
                JSONObject json = seriesOfPopups.getJSONData();
                recordPrediction(moviePicked, json);
            } catch (JSONException e) {
                predictionFailure();
                return;
            }
        } else {
            dialogs.get(dialogNum).show();
            dialogNum += 1;
        }
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
                return convertMovieRowView(position, convertView, parent);
            }
        });

        moviesListView.setAdapter(moviesListAdapter);
        moviesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                moviePicked = (MovieItem)moviesListAdapter.getItem(position);
                startRolling();
            }
        });

    }

    private View convertMovieRowView(int position, View convertView, ViewGroup parent) {
        // Customize the row for list view
        if(convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.movie_list_view_row, null);
        }
        final MovieItem item = (MovieItem) moviesListAdapter.getItem(position);
        if (item != null) {
            TextView titleView = (TextView) convertView.findViewById(R.id.title_view);
            titleView.setText(item.getTitle());
            TextView releaseDateView = (TextView) convertView.findViewById(R.id.release_date_view);
            releaseDateView.setText(item.getReleaseDateFull());
            ImageView posterView = (ImageView) convertView.findViewById(R.id.poster_view);
            Bitmap poster = item.getPoster();
            if (poster!=null)
                posterView.setImageBitmap(poster);
            else
                posterView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.no_poster_w185, null));
        }
        return convertView;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load the first page to start demo
        moviesListAdapter.onScrollNext();

    }


    private void recordPrediction(MovieItem item, JSONObject wagerData) {
        JSONObject json = wagerData;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        long unixDate;
        try {
            unixDate = format.parse(item.getReleaseDate()).getTime();
            Log.e("DATE", item.getReleaseDate() + " " + String.valueOf(unixDate));
        } catch (ParseException e) {
            predictionFailure();
            return;
        }
        try {
            json.put("key", Twitter.getInstance().core.getSessionManager().getActiveSession().getAuthToken().token);
            json.put("dueDate", unixDate / 1000);
            json.put("title", item.getTitle());
        } catch (JSONException e) {
            predictionFailure();
            return;
        }
        HoPRequestHelper.post(this, "/prediction/" + type, json, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject json = new JSONObject(new String(responseBody));
                    predictionSuccess(json.getString("url"));
                } catch (JSONException e) {
                    Log.e("JSON", e.toString());
                    predictionFailure();
                    return;
                }
                Log.d("1", new String(responseBody));
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("HoP", new String(responseBody));
                predictionFailure();
            }
        });
    }

    private void predictionFailure() {
        Toast.makeText(this, "Prediction not created.", Toast.LENGTH_LONG).show();
        moviePicked = null;
        generateSeriesOfPopups();
    }

    private void predictionSuccess(String url) {
        Toast.makeText(this, "Prediction created!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(MovieRatingsCreatePredictionActivity.this, ViewGenericPrediction.class);
        intent.putExtra("url", url);
        intent.putExtra("type", type);
        startActivity(intent);
        finish();
    }

}
