package com.example.artemsinyakov.hallofprophecy.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.artemsinyakov.hallofprophecy.R;

import java.util.ArrayList;

public class ViewComments extends AppCompatActivity {
    String predictionText;
    String[] commentNames = {};
    String[] commentTexts = {};
    String[] fullCommentTexts = {};
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_comments);

        listView = (ListView) findViewById(R.id.comments_list);
        Intent intent = getIntent();
        predictionText = intent.getStringExtra("text");
        commentNames = intent.getStringArrayExtra("names");
        commentTexts = intent.getStringArrayExtra("texts");

        ((TextView) findViewById(R.id.prediction_text)).setText(predictionText);

        consolidateComments();



        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.user_profile_predictions_list, this.fullCommentTexts);
        listView.setAdapter(adapter);

    }
    private void consolidateComments() {
        ArrayList<String> fwt = new ArrayList<>();
        Log.e("1", String.valueOf(commentNames.length));
        Log.e("1", String.valueOf(commentTexts.length));

        for (int i = 0; i < commentNames.length; i++) {
            try {
                fwt.add(commentNames[i] + ": " + commentTexts[i]);
            } catch (RuntimeException e) {
                Log.e("3", e.toString());
            }
        }
        fullCommentTexts = fwt.toArray(new String[0]);
    }
}
