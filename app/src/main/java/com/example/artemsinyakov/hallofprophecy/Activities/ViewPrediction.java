package com.example.artemsinyakov.hallofprophecy.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ShareActionProvider;
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
import java.util.Collections;

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

    private JSONArray wagers = new JSONArray();
    private JSONArray comments = new JSONArray();



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
                    setUpButtons();
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
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Prediction: ");
                String shareMessage = context.getResources().getString(R.string.site) + "/prediction/twitter/" + url;
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                        shareMessage);
                startActivity(Intent.createChooser(shareIntent,
                        "Share prediction."));
                return true;
            }
        });
        return true;
    }

    private void displayPrediction(JSONObject json) throws JSONException {
        Log.e("432", json.toString());
        predictionText = json.getString("text");
        arbiter = json.getString("arbiterHandle");
        String res = null;
        try {
            res = json.getString("result");
        } catch (JSONException e) {
            res = "Undecided";
        }
        Log.e("1", res==null?"null":res);
        if (res != null)
        if (res.equals("1")) {
            result = "True";
        } else if (res.equals("-1")) {
            result = "False";
        } else {
            result = "Undecided";
        }

        dueDate = new SimpleDateFormat(getResources().getString(R.string.date_format)).format(new java.util.Date(json.getLong("dueDate") * 1000));
        name = json.getString("name");

        ((TextView)findViewById(R.id.prediction_text)).setText(predictionText);
        ((TextView)findViewById(R.id.arbiterHandle)).setText(getResources().getString(R.string.Arbiter_label) + arbiter);
        ((TextView)findViewById(R.id.dueDate)).setText(getResources().getString(R.string.Due_date) + dueDate);
        ((TextView)findViewById(R.id.username)).setText(getResources().getString(R.string.Author)  + name);
        ((TextView)findViewById(R.id.result)).setText(getResources().getString(R.string.Status)  + result);
    }

    private void parseWagersComments(JSONObject json) throws JSONException {
        ArrayList<String> wnames = new ArrayList<String>();
        ArrayList<Boolean> wvalues = new ArrayList<Boolean>();
        wagers = json.getJSONArray("wagers");
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
        comments = json.getJSONArray("comments");
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
                intent.putExtra("wagers", wagers.toString());
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });
        ((Button) findViewById(R.id.see_wagers)).setText(getResources().getString(R.string.View_wagers) + "(" + wagers.length() + ")");
        findViewById(R.id.see_comments).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewPrediction.this, ViewComments.class);
                intent.putExtra("text", predictionText);
                intent.putExtra("names", commentNames);
                intent.putExtra("texts", commentTexts);
                intent.putExtra("comments", comments.toString());
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });
        ((Button) findViewById(R.id.see_comments)).setText(getResources().getString(R.string.View_comments) + "(" + comments.length() + ")");
    }
}
