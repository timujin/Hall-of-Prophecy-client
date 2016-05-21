package com.example.artemsinyakov.hallofprophecy.GenericProfileView;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.artemsinyakov.hallofprophecy.GenericPredictionVIew.GenericPrediction;
import com.example.artemsinyakov.hallofprophecy.GenericPredictionVIew.ViewGenericPrediction;
import com.example.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.example.artemsinyakov.hallofprophecy.R;
import com.example.artemsinyakov.hallofprophecy.SeriesOfPopups.PickAPredictionDialog;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class GPVActivity extends AppCompatActivity {

    String url;
    ProfileViewResponseDispatcher dispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpv);

        Intent intent = getIntent();
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.VIEW")) {
            url = intent.getData().getLastPathSegment();
        } else {
            url = intent.getStringExtra("url");
        }

        loadProfile();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_prediction, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        final Context context = this;
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"User profile: ");
                String shareMessage;
                shareMessage = context.getResources().getString(R.string.site) + "/user/" + url;
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                        shareMessage);
                startActivity(Intent.createChooser(shareIntent,
                        "Share user's profile."));
                return true;
            }
        });
        return true;
    }

    private void loadProfile() {
        final Context context = this;
        HoPRequestHelper.get("/user/withwagers/"+ url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject json = new JSONObject(new String(responseBody));
                    loadProfileStep2(json);
                } catch (JSONException e) {
                    Log.e("JSON", e.toString());
                    Toast.makeText(context, "Could not display prediction - JSON does not parse", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "Could not display prediction - network failure", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadProfileStep2(final JSONObject withwagersJSON) {
        final Context context = this;
        HoPRequestHelper.get("/user/onlyundecided/"+ url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject json = new JSONObject(new String(responseBody));
                    dispatcher = new ProfileViewResponseDispatcher(withwagersJSON, json, context);
                    setUpUI();
                } catch (JSONException e) {
                    Log.e("JSON", e.toString());
                    Toast.makeText(context, "Could not display prediction - JSON does not parse", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "Could not display prediction - network failure", Toast.LENGTH_LONG).show();
            }
        });
    }

//////////////// UI ////////////////
    //ImageView avatar;
    TextView titleText;
    Button showAll;
    Button showUpcoming;
    ListView allList;
    ListView upcomingList;


    private void setUpUI() {
       //avatar = (ImageView) findViewById(R.id.avatar);
        titleText = (TextView) findViewById(R.id.title_text);
        showAll = (Button) findViewById(R.id.show_all);
        showUpcoming = (Button) findViewById(R.id.show_upcoming);
        allList = (ListView) findViewById(R.id.all_list);
        upcomingList = (ListView) findViewById(R.id.upcoming_list);

        setUpTitle();
        setUpButtons();
        setUpLists();
        setUpListClicks();
        showUI();
        setUpFAB();
    }

    private void setUpTitle() {
        titleText.setText("@" + dispatcher.getName() + "'s prediction profile.");
    }

    private void setUpButtons() {
        showUpcoming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpcoming.setEnabled(false);
                showAll.setEnabled(true);
                upcomingList.setVisibility(View.VISIBLE);
                allList.setVisibility(View.GONE);
            }
        });
        showAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpcoming.setEnabled(true);
                showAll.setEnabled(false);
                upcomingList.setVisibility(View.GONE);
                allList.setVisibility(View.VISIBLE);
            }
        });
        showAll.setEnabled(false);
    }

    private void setUpLists() {
        upcomingList.setVisibility(View.GONE);
        ArrayAdapter adapter = new PredictionPanelAdapter(this, dispatcher.getAllArray());
        allList.setAdapter(adapter);
        adapter = new PredictionPanelAdapter(this, dispatcher.getUpcomingArray());
        upcomingList.setAdapter(adapter);
    }

    private void setUpListClicks() {
        upcomingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GenericPrediction prediction = dispatcher.getUpcomingArray()[position];
                String predictionType = prediction.getType();
                String predictionURL = prediction.getURL();
                if (predictionURL != null) {
                    Intent intent = new Intent(GPVActivity.this, ViewGenericPrediction.class);
                    intent.putExtra("url", predictionURL);
                    intent.putExtra("type", predictionType);
                    startActivity(intent);
                }
            }
        });
        allList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GenericPrediction prediction = dispatcher.getAllArray()[position];
                String predictionType = prediction.getType();
                String predictionURL = prediction.getURL();
                if (predictionURL != null) {
                    Intent intent = new Intent(GPVActivity.this, ViewGenericPrediction.class);
                    intent.putExtra("url", predictionURL);
                    intent.putExtra("type", predictionType);
                    startActivity(intent);
                }
            }
        });
    }


    private void showUI(){
        (findViewById(R.id.progressBar1)).setVisibility(View.GONE);
        (findViewById(R.id.the_entire_layout)).setVisibility(View.VISIBLE);
    }

    private void setUpFAB() {
        final Context context = this;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    PickAPredictionDialog.showPredictionDialog(context);
                    return true;
                }
                return true; // consume the event
            }
        });
    }
}
