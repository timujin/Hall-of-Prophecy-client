package com.archonmode.artemsinyakov.hallofprophecy.Activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.archonmode.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.archonmode.artemsinyakov.hallofprophecy.R;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.twitter.sdk.android.Twitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ViewComments extends AppCompatActivity {
    String predictionText;
    String[] commentNames = {};
    String[] commentTexts = {};
    String[] fullCommentTexts = {};
    JSONArray comments = new JSONArray();
    ListView listView;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_comments);

        listView = (ListView) findViewById(R.id.comments_list);
        Intent intent = getIntent();
        predictionText = intent.getStringExtra("text");
        commentNames = intent.getStringArrayExtra("names");
        commentTexts = intent.getStringArrayExtra("texts");
        try {
            comments = new JSONArray(intent.getStringExtra("comments"));
        } catch (JSONException e) {
            comments = new JSONArray();
        }
        url = intent.getStringExtra("url");

        ((TextView) findViewById(R.id.prediction_text)).setText(predictionText);

        consolidateComments();



        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.user_profile_predictions_list, this.fullCommentTexts);
        listView.setAdapter(adapter);

        setUpButtons();

    }
    private void consolidateComments() {
        ArrayList<String> fwt = new ArrayList<>();

        /*for (int i = 0; i < commentNames.length; i++) {
            try {
                fwt.add(commentNames[i] + ": " + commentTexts[i]);
            } catch (RuntimeException e) {
                Log.e("3", e.toString());
            }
        }*/
        try {
            for (int i = 0; i < comments.length(); i++) {
                JSONObject obj = comments.getJSONObject(i);
                fwt.add("@" + obj.get("handle") + ": " + obj.get("text"));
            }
        } catch (JSONException e) {
            // do nothing
        }
        fullCommentTexts = fwt.toArray(new String[comments.length()]);
    }


    private void setUpButtons() {
        findViewById(R.id.comment_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText t = (EditText) findViewById(R.id.comment_text);
                if (t.getText().toString().length() > 0)
                    recordComment(t.getText().toString());
            }
        });
    }

    private void recordComment(String text) {
        JSONObject json = new JSONObject();
        try {
            json.put("text", text);
            json.put("author", Twitter.getInstance().core.getSessionManager().getActiveSession().getAuthToken().token);
        } catch (JSONException e) {
            Toast.makeText(this, "Could not record comment.", Toast.LENGTH_LONG).show();
        }
        final Activity activity = this;
        Log.e("54321", json.toString());
        HoPRequestHelper.post(this, "/prediction/twitter/comment/" + url, json, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Intent intent = new Intent(ViewComments.this, ViewPrediction.class);
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
