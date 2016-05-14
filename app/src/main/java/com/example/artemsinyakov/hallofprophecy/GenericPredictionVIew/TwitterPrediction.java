package com.example.artemsinyakov.hallofprophecy.GenericPredictionVIew;

import android.content.Context;

import com.example.artemsinyakov.hallofprophecy.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TwitterPrediction extends GenericPrediction {

    public JSONObject json;
    public Context context;

    public TwitterPrediction(JSONObject json, Context context) {
        super(json, context);
    }

    public String getType() {
        return "twitter";
    }

    public String getTypeVerbose() {
        return "Twitter";
    }

    public String getDescription() {
        try {
            String predictionText = json.getString("text");
            String arbiter = json.getString("arbiterHandle");
            String name = json.getString("name");
            return name + ": " + predictionText + "\n@" + arbiter + " will judge this.";
        } catch (JSONException e) {
            return "Error";
        }
    }
    public String getJudgement() {
        String res;
        try {
            res = json.getString("result");
        } catch (JSONException e) {
            return "Not decided yet.";
        }
        if (res != null)
            if (res.equals("1")) {
                return "Judged to be true!";
            } else {
                return "Judged to be false!";
            }
        else {
            return "Not decided yet.";
        }
    }

    protected String processWagerText(JSONObject j) {
        try {
            return "@" + j.get("handle") + ": " + (j.getInt("wager") == 1?"True":"False");
        } catch (JSONException e) {
            return "Error";
        }
    }

    public boolean hasComments() { return true; }

    protected String processCommentText(JSONObject j) {
        try {
            return "@" + j.get("handle") + ": " + j.get("text");
        } catch (JSONException e) {
            return "Error";
        }
    }

}
