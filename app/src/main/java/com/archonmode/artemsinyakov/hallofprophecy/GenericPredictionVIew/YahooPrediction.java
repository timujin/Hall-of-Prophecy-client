package com.archonmode.artemsinyakov.hallofprophecy.GenericPredictionVIew;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class YahooPrediction extends GenericPrediction {

    public YahooPrediction(JSONObject json, Context context) {
        super(json, context);
    }

    public String getType() {
        return "yahooFinance";
    }

    public String getTypeVerbose() {
        return "Currency rates";
    }

    public String getDescription() {
        try {
            String cur1 = json.getString("currencies").substring(0, 3);
            String cur2 = json.getString("currencies").substring(3, 6);
            return "Predictions on the ratio of " + cur1 + " to " + cur2;
        } catch (JSONException e) {
            return "Error";
        }
    }

    @Override
    public String getDescriptionBrief() {
        try {
            String cur1 = json.getString("currencies").substring(0, 3);
            String cur2 = json.getString("currencies").substring(3, 6);
            return cur1 + " to " + cur2;
        } catch (JSONException e) {
            return "Error";
        }
    }

    public String getJudgement() {
        try {
            String res = json.getString("result");
            String bid =  json.getString("judgementBid");
            if (res == null || res.equals("null"))
                return "Not decided yet";
            return "Actual value: " + bid;
        } catch (JSONException e) {
            return "Not decided yet.";
        }
    }

    protected String processWagerText(JSONObject j) {
        try {
            return "@" + j.get("handle") + ": " +
                    (j.getString("bidDirection").equals("ge") ? ">" : "<") +
                    String.valueOf(j.getLong("targetBid"));
        } catch (JSONException e) {
            return "Error";
        }
    }

    public boolean hasComments() {
        return false;
    }

    protected String processCommentText(JSONObject j) {
        return "Error";
    }
}