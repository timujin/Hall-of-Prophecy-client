package com.example.artemsinyakov.hallofprophecy.Activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.example.artemsinyakov.hallofprophecy.R;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ViewPrediction extends AppCompatActivity {

    private String url;
    private String predictionText;
    private String arbiter;
    private String dueDate;
    private String result;
    private String name;

    private String[] wagerNames;
    private boolean[] wagerValues;

    private String[] commentNames;
    private String[] commentTexts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_prediction);

        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        final Activity activity = this;
        HoPRequestHelper.get("/prediction/twitter/" + url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                JSONObject json;
                try {
                    json = new JSONObject(new String(responseBody));
                    displayPrediction(json);
                    parseWagersComments(json);
                } catch (JSONException e) {
                    Log.e("e", e.toString());
                    Toast.makeText(activity, "Could not display prediction - JSON does not parse", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(activity, "Could not display prediction - network failure", Toast.LENGTH_LONG).show();
            }
        });

        setUpButtons();
    }

    private void displayPrediction(JSONObject json) throws JSONException {
        predictionText = json.getString("text");
        arbiter = json.getString("arbiterHandle");
        result = json.getString("result") == null? "null" : "something";
        dueDate = new SimpleDateFormat("MM dd, yyyy").format(new java.util.Date(json.getLong("dueDate")));
        name = json.getString("name");

        ((TextView)findViewById(R.id.urlbox)).setText(url);
        ((TextView)findViewById(R.id.prediction_text)).setText(predictionText);
        ((TextView)findViewById(R.id.arbiterHandle)).setText("@" + arbiter);
        ((TextView)findViewById(R.id.dueDate)).setText(dueDate);
        ((TextView)findViewById(R.id.username)).setText("Author: " + name);
        ((TextView)findViewById(R.id.result)).setText("Result: " + result);
    }

    private void parseWagersComments(JSONObject json) throws JSONException {
        ArrayList<String> wnames = new ArrayList<String>();
        ArrayList<Boolean> wvalues = new ArrayList<Boolean>();
        JSONArray wagers = json.getJSONArray("wagers");
        for (int i = 0; i<wagers.length(); i++) {
            JSONObject obj = wagers.getJSONObject(i);
            wnames.add(obj.getString("handle"));
            wvalues.add(obj.getInt("wager") == 1);
        }
        this.wagerNames = wnames.toArray(new String[0]);
        this.wagerValues = new boolean[wvalues.size()];
        int index = 0;
        for (Boolean object : wvalues) {
            wagerValues[index++] = object;
        }


        ArrayList<String> cnames = new ArrayList<String>();
        ArrayList<String> ctexts = new ArrayList<String>();
        JSONArray comments = json.getJSONArray("comments");
        Log.e("111", comments.toString());
        for (int i = 0; i<comments.length(); i++) {
            JSONObject obj = comments.getJSONObject(i);
            Log.e("222", obj.toString());
            cnames.add(obj.getString("handle"));
            ctexts.add(obj.getString("text"));
        }
        this.commentNames = wnames.toArray(new String[0]);
        this.commentTexts = ctexts.toArray(new String[0]);
    }

    private void setUpButtons() {
        findViewById(R.id.see_wagers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewPrediction.this, ViewWagers.class);
                intent.putExtra("text", predictionText);
                intent.putExtra("names", wagerNames);
                intent.putExtra("values", wagerValues);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });
        findViewById(R.id.see_comments).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewPrediction.this, ViewComments.class);
                intent.putExtra("text", predictionText);
                intent.putExtra("names", commentNames);
                intent.putExtra("texts", commentTexts);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });
    }
}
