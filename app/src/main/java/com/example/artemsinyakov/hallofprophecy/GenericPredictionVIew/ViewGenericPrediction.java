package com.example.artemsinyakov.hallofprophecy.GenericPredictionVIew;

import android.content.Context;
import android.content.Intent;
import android.opengl.Visibility;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.artemsinyakov.hallofprophecy.Activities.PredictionProcessor;
import com.example.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.example.artemsinyakov.hallofprophecy.R;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

public class ViewGenericPrediction extends AppCompatActivity {

    private String url;
    private String type;

    private GenericPrediction prediction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_generic_prediction);

        Intent intent = getIntent();
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.VIEW")) {
            Log.e("1", intent.getAction());
            Log.e("1", intent.getData().toString());
            ArrayList<String> path = (new ArrayList<>(Arrays.asList(intent.getData().getPath().split("/"))));
            Log.e("1", path.toString());
            url = path.get(path.size()-1);
            type = path.get(path.size()-2);
            Log.e("dsf", type+url);
        } else {
            url = intent.getStringExtra("url");
            type = intent.getStringExtra("type");
        }

        downloadPrediction();
    }

    private void downloadPrediction() {
        final Context context = this;
        HoPRequestHelper.get("/prediction/"+type+"/" + url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject json = new JSONObject(new String(responseBody));
                    prediction = GenericPrediction.GeneratePredictionFromType(type, json, context);
                    if (prediction == null)
                        throw new RuntimeException();
                    populateUIElements();
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

    private void populateUIElements() {
        final TextView typeText = (TextView) findViewById(R.id.type_text);
        final TextView dueText = (TextView) findViewById(R.id.due_text);
        final TextView mainText = (TextView) findViewById(R.id.main_text);
        final TextView judgementText = (TextView) findViewById(R.id.judgement_text);
        final ListView wagersList = (ListView) findViewById(R.id.wagers_list);
        final ListView commentsList = (ListView) findViewById(R.id.comments_list);
        final Button makeWager = (Button) findViewById(R.id.make_wager);
        final Button makeComment = (Button) findViewById(R.id.make_comment);

        populateTexts(typeText, dueText, mainText, judgementText);
        populateLists(wagersList, commentsList);
        activateButtons(makeWager, makeComment);
    }

    private void populateTexts(TextView typeText, TextView dueText, TextView mainText, TextView judgementText) {
        typeText.setText(prediction.getTypeVerbose());
        dueText.setText(prediction.getDueDate());
        mainText.setText(prediction.getDescription());
        judgementText.setText(prediction.getJudgement());
    }
    private void populateLists(ListView wagersList, ListView commentsList) {
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.simple_text_list_view, prediction.getProcessedWagers());
        wagersList.setAdapter(adapter);
        if (prediction.hasComments()) {
            adapter = new ArrayAdapter<>(this, R.layout.simple_text_list_view, prediction.getProcessedComments());
            commentsList.setAdapter(adapter);
        } else {
            commentsList.setVisibility(View.GONE);
        }
    }
    private void activateButtons(Button makeWager, Button makeComment) {
        if (!prediction.hasComments()) {
            makeComment.setVisibility(View.GONE);
        }
    }

}
