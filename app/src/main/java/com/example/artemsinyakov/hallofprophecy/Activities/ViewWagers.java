package com.example.artemsinyakov.hallofprophecy.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.artemsinyakov.hallofprophecy.R;

import java.util.ArrayList;

public class ViewWagers extends AppCompatActivity {

    String predictionText;
    String[] wagerNames = {};
    boolean[] wagerValues = {};
    String[] fullWagerTexts = {};
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_wagers);

        listView = (ListView) findViewById(R.id.wagers_list);
        Intent intent = getIntent();
        predictionText = intent.getStringExtra("text");
        wagerNames = intent.getStringArrayExtra("names");
        wagerValues = intent.getBooleanArrayExtra("values");

        ((TextView) findViewById(R.id.prediction_text)).setText(predictionText);

        consolidateWagers();



        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.user_profile_predictions_list, this.fullWagerTexts);
        listView.setAdapter(adapter);

    }
    private void consolidateWagers() {
        ArrayList<String> fwt = new ArrayList<>();
        for (int i = 0; i < wagerNames.length; i++) {
            fwt.add(wagerNames[i] + ": " + (wagerValues[i]?"true":"false"));
        }
        fullWagerTexts = fwt.toArray(new String[0]);
    }
}
