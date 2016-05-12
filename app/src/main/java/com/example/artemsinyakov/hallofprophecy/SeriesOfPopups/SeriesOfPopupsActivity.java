package com.example.artemsinyakov.hallofprophecy.SeriesOfPopups;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.artemsinyakov.hallofprophecy.Activities.DisplayGenericPrediction;
import com.example.artemsinyakov.hallofprophecy.R;
import com.example.artemsinyakov.hallofprophecy.SeriesOfPopups.SeriesOfPopups;
import com.example.artemsinyakov.hallofprophecy.SeriesOfPopups.TwitterSeriesOfPopups;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;

public class SeriesOfPopupsActivity extends AppCompatActivity {

    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series_of_popups);


    }
}
