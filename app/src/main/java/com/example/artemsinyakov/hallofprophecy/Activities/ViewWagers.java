package com.example.artemsinyakov.hallofprophecy.Activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityRecord;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.example.artemsinyakov.hallofprophecy.R;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.twitter.sdk.android.Twitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ViewWagers extends AppCompatActivity {

    String predictionText;
    String[] wagerNames = {};
    boolean[] wagerValues = {};
    String[] fullWagerTexts = {};
    ListView listView;
    JSONArray wagers = new JSONArray();
    String url;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_wagers);

        listView = (ListView) findViewById(R.id.wagers_list);
        intent = getIntent();
        predictionText = intent.getStringExtra("text");
        wagerNames = intent.getStringArrayExtra("names");
        wagerValues = intent.getBooleanArrayExtra("values");
        url = intent.getStringExtra("url");
        try {
            wagers = new JSONArray(intent.getStringExtra("wagers"));
        } catch (JSONException e) {
            wagers = new JSONArray();
        }
        ((TextView) findViewById(R.id.prediction_text)).setText(predictionText);

        consolidateWagers();



        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.user_profile_predictions_list, this.fullWagerTexts);
        listView.setAdapter(adapter);

        setUpButtons();

    }
    private void consolidateWagers() {
        ArrayList<String> fwt = new ArrayList<>();
        try {
            for (int i = 0; i < wagers.length(); i++) {
                JSONObject obj = wagers.getJSONObject(i);
                fwt.add("@" + obj.get("handle") + ": " + (obj.getInt("wager") == 1?"True":"False"));
            }
        } catch (JSONException e) {
            // do nothing
        }
        fullWagerTexts = fwt.toArray(new String[wagers.length()]);
    }

    private void setUpButtons() {
        findViewById(R.id.will_happen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordWager(true);
            }
        });

        findViewById(R.id.will_not_happen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordWager(false);
            }
        });
    }

    private void recordWager(Boolean wager) {
        JSONObject json = new JSONObject();
        try {
            json.put("wager", wager?"1":"0");
            json.put("author", Twitter.getInstance().core.getSessionManager().getActiveSession().getAuthToken().token);
        } catch (JSONException e) {
            Toast.makeText(this, "Could not record wager.", Toast.LENGTH_LONG).show();
        }
        final Activity activity = this;
        HoPRequestHelper.post(this, "/prediction/twitter/wager/"+url, json, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Intent intent = new Intent(ViewWagers.this, ViewPrediction.class);
                intent.putExtra("url", url);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(activity, new String(responseBody), Toast.LENGTH_LONG).show();
            }
        });
    }
}
