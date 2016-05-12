package com.example.artemsinyakov.hallofprophecy.SeriesOfPopups;

import android.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by newti on 5/12/2016.
 */
public interface SeriesOfPopups {
    ArrayList<AlertDialog> getDialogs();
    JSONObject getJSONData() throws JSONException;

}
