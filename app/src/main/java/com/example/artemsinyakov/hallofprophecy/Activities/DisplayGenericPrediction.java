package com.example.artemsinyakov.hallofprophecy.Activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.example.artemsinyakov.hallofprophecy.R;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class DisplayGenericPrediction extends AppCompatActivity {

    private String type;
    private String url;

    private TextView predictionText;
    private TextView dueDate;
    private TextView result;
    private Button seeComments;
    private Button seeWagers;

    private JSONObject json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_generic_prediction);
        /////////////////////////
        Intent intent = getIntent();
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.VIEW")) {
            Log.e("1", intent.getAction());
            Log.e("1", intent.getData().toString());
            ArrayList<String> path = (ArrayList<String>)(intent.getData().getPathSegments());
            url = path.get(path.size()-1);
            type = path.get(path.size()-2);
        } else {
            url = intent.getStringExtra("url");
            type = intent.getStringExtra("type");
        }
        ///////////////////////
        predictionText = (TextView) findViewById(R.id.prediction_text);
        dueDate = (TextView) findViewById(R.id.dueDate);
        result = (TextView) findViewById(R.id.result);
        seeComments = (Button) findViewById(R.id.see_comments);
        seeWagers = (Button) findViewById(R.id.see_wagers);
        //////////////////////
        setUpButtons();
        processPrediction();
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
                String shareMessage = context.getResources().getString(R.string.site) + "/prediction/" + type + "/" + url;
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                        shareMessage);
                startActivity(Intent.createChooser(shareIntent,
                        "Share prediction."));
                return true;
            }
        });
        return true;
    }

    private void setUpButtons() {
        if (type.equals("twitter")) {
            seeComments.setVisibility(View.VISIBLE);
            seeComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // see comments
                }
            });
        }
        seeWagers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //see wagers
            }
        });
    }

    private void processPrediction() {
        final Context context = this;
        HoPRequestHelper.get("/prediction/"+type+"/" + url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    json = new JSONObject(new String(responseBody));
                    PredictionProcessor processor = new PredictionProcessor(type, json, context);
                    predictionText.setText(processor.getText());
                    dueDate.setText(processor.getDueDate());
                    result.setText(processor.getResult());
                } catch (JSONException e) {
                    Log.e("e", e.toString());
                    Toast.makeText(context, "Could not display prediction - JSON does not parse", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "Could not display prediction - network failure", Toast.LENGTH_LONG).show();
            }
        });
    }
}
