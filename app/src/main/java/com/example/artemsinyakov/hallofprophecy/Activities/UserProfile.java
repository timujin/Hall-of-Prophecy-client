package com.example.artemsinyakov.hallofprophecy.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.example.artemsinyakov.hallofprophecy.R;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class UserProfile extends AppCompatActivity {

    ListView listView;
    String api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.predictions_list);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        api = intent.getStringExtra("api");
        loadPredictions(url);

        final Context context = this;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                try {
                    JSONObject obj = (JSONObject) parent.getItemAtPosition(position);
                    Intent intent = new Intent(UserProfile.this, ViewPrediction.class);
                    intent.putExtra("url", obj.getString("url"));
                    startActivity(intent);
                } catch (JSONException e) {
                    Log.e("2", e.toString());
                    Toast.makeText(context, "Could not open prediction", Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    private void loadPredictions(String id) {
        final Activity th = this;
        HoPRequestHelper.get(api + id, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                JSONObject json;
                try {
                    json = new JSONObject(new String(responseBody));
                    parseList(json);
                } catch (JSONException e) {
                    Log.e("e", e.toString());
                    Toast.makeText(th, "Could not display profile", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(th, "Could not display profile", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void parseList(JSONObject json) throws JSONException {
        JSONArray predictions = json.getJSONArray("predictions");
        ArrayList<JSONObject> predictionsArray = new ArrayList<>();
        for (int i = 0; i<predictions.length(); i++) {
            JSONObject obj = predictions.getJSONObject(i);
            predictionsArray.add(obj);
        }
        Collections.sort(predictionsArray, new predictionsComparator());
        ArrayAdapter adapter = new ProfilePredictionListAdapter(this, predictionsArray.toArray(new JSONObject[0]));
        listView.setAdapter(adapter);
    }

    private class predictionsComparator implements Comparator<JSONObject> {
        @Override
        public int compare(JSONObject obj1, JSONObject obj2) {
            try {
                Date date1 = new java.util.Date(obj1.getLong("dueDate") * 1000);
                Date date2 = new java.util.Date(obj2.getLong("dueDate") * 1000);
                if (date1.after(date2)) {
                    return 1;
                } else if (date2.after(date1)) {
                    return -1;
                } else {
                    return 0;
                }
            } catch (JSONException e) {
                return 0;
            }
        }
    }
}
