package com.archonmode.artemsinyakov.hallofprophecy.GenericProfileView;

import android.content.Context;

import com.archonmode.artemsinyakov.hallofprophecy.GenericPredictionVIew.GenericPrediction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProfileViewResponseDispatcher {
    private Context context;
    public JSONObject withwagersJSON;
    public JSONObject onlyundecidedJSON;

    public ArrayList<GenericPrediction> all;
    public ArrayList<GenericPrediction> onlyUndecided;


    public ProfileViewResponseDispatcher(JSONObject withwagers, JSONObject onlyundecided, Context context) {
        withwagersJSON = withwagers;
        onlyundecidedJSON = onlyundecided;
        this.context = context;
    }

    public String getName() {
        try {
            return withwagersJSON.getString("handle");
        } catch (JSONException e) {
            return "Error";
        }
    }

    public GenericPrediction[] getAllArray() {
        if (all != null)
            return all.toArray(new GenericPrediction[all.size()]);

        try {
            all = new ArrayList<>();
            JSONObject allPredictions = withwagersJSON.getJSONObject("predictions");
            for (String type : GenericPrediction.getPredictionTypes()) {
                JSONArray predictionsOfType = allPredictions.getJSONArray(type);
                for (int i=0;i<predictionsOfType.length();i++) {
                    GenericPrediction p = GenericPrediction.GeneratePredictionFromType
                                    (type, predictionsOfType.getJSONObject(i), context);
                    all.add(p);
                }
            }
            return all.toArray(new GenericPrediction[all.size()]);
        } catch (JSONException e) {
            return null;
        }
    }
    public GenericPrediction[] getUpcomingArray() {
        if (onlyUndecided != null)
            return onlyUndecided.toArray(new GenericPrediction[onlyUndecided.size()]);
        try {
            onlyUndecided = new ArrayList<>();
            JSONObject allPredictions = onlyundecidedJSON.getJSONObject("predictions");
            for (String type : GenericPrediction.getPredictionTypes()) {
                JSONArray predictionsOfType = allPredictions.getJSONArray(type);
                for (int i=0;i<predictionsOfType.length();i++) {
                    GenericPrediction p = GenericPrediction.GeneratePredictionFromType
                            (type, predictionsOfType.getJSONObject(i), context);
                    onlyUndecided.add(p);
                }
            }
            return onlyUndecided.toArray(new GenericPrediction[onlyUndecided.size()]);
        } catch (JSONException e) {
            return null;
        }
    }
}
