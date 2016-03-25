package com.example.artemsinyakov.hallofprophecy.Activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.example.artemsinyakov.hallofprophecy.R;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

import cz.msebera.android.httpclient.Header;

public class ViewPrediction extends AppCompatActivity {

    private String url;
    private String predictionText;
    private String arbiter;
    private String dueDate;
    private String result;
    private String name;


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
                } catch (JSONException e) {
                    Log.e("e", e.toString());
                    Toast.makeText(activity, "Could not display prediction", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(activity, "Could not display prediction", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayPrediction(JSONObject json) {
        try {
            predictionText = json.getString("text");
            arbiter = json.getString("arbiterHandle");
            result = json.getString("result") == null? "null" : "something";
            dueDate = new SimpleDateFormat("MM dd, yyyy").format(new java.util.Date(json.getLong("dueDate")));
            name = json.getString("name");
        } catch (JSONException e) {
            Log.e("e", e.toString());
        }

        ((TextView)findViewById(R.id.urlbox)).setText(url);
        ((TextView)findViewById(R.id.prediction_text)).setText(predictionText);
        ((TextView)findViewById(R.id.arbiterHandle)).setText("@"+arbiter);
        ((TextView)findViewById(R.id.dueDate)).setText(dueDate);
        ((TextView)findViewById(R.id.username)).setText("Author: "+name);
        ((TextView)findViewById(R.id.result)).setText("Result: "+result);
    }
}
