package com.example.artemsinyakov.hallofprophecy.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class UserProfile extends AppCompatActivity {

    String[] predictions = {};
    String[] predictionIDs = {};
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        listView = (ListView) findViewById(R.id.predictions_list);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        loadPredictions(url);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String openid = predictionIDs[position];

                Intent intent = new Intent(UserProfile.this, ViewPrediction.class);
                intent.putExtra("url", openid);
                startActivity(intent);
            }
        });


    }

    private void loadPredictions(String id) {
        final Activity th = this;
        HoPRequestHelper.get("/user/" + id, null, new AsyncHttpResponseHandler() {
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
        ArrayList<String> pr = new ArrayList<String>();
        ArrayList<String> id = new ArrayList<String>();
        JSONArray predictions = json.getJSONArray("predictions");
        for (int i = 0; i<predictions.length(); i++) {
            JSONObject obj = predictions.getJSONObject(i);
            pr.add(obj.getString("text"));
            id.add(obj.getString("url"));
        }
        this.predictions = pr.toArray(new String[0]);
        this.predictionIDs = id.toArray(new String[0]);
        Log.e("1", this.predictions[0]);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.user_profile_predictions_list, this.predictions);
        listView.setAdapter(adapter);
    }
}
