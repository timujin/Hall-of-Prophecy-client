package com.archonmode.artemsinyakov.hallofprophecy.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.archonmode.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.archonmode.artemsinyakov.hallofprophecy.R;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

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
                    Intent intent = new Intent(UserProfile.this, DisplayGenericPrediction.class);
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
                    Log.e("2", new String(responseBody));
                    json = new JSONObject(new String(responseBody));
                    parseList(json);
                } catch (JSONException e) {
                    Log.e("e", e.toString());
                    Toast.makeText(th, "Could NOT display profile", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(th, "Could not display profile", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void parseList(JSONObject json) throws JSONException {
        JSONObject predictionJSONs = json.getJSONObject("predictions");
        JSONArray twitterJSONs = predictionJSONs.getJSONArray("twitter");
        ArrayList<PredictionProcessor> predictionsArray = new ArrayList<>();
        for (int i = 0; i<twitterJSONs.length(); i++) {
            JSONObject obj = twitterJSONs.getJSONObject(i);
            predictionsArray.add(new PredictionProcessor("twitter",obj,this));
        }
        JSONArray yahooJSONs = predictionJSONs.getJSONArray("yahooFinance");
        for (int i = 0; i<yahooJSONs.length(); i++) {
            JSONObject obj = yahooJSONs.getJSONObject(i);
            predictionsArray.add(new PredictionProcessor("yahooFinance",obj,this));
        }
        Collections.sort(predictionsArray, new PredictionProcessor.predictionsComparator());
        ArrayAdapter adapter = new GenericPredictionListAdapter(this, predictionsArray.toArray(new PredictionProcessor[0]));
        listView.setAdapter(adapter);
    }

}
