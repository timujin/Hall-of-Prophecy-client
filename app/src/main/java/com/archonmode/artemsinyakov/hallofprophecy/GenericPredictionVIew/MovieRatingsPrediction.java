package com.archonmode.artemsinyakov.hallofprophecy.GenericPredictionVIew;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by newti on 6/12/2016.
 */
public class MovieRatingsPrediction extends GenericPrediction {

    public MovieRatingsPrediction(JSONObject json, Context context) {
        super(json, context);
    }

    public String getType() {
        return "movieRatings";
    }

    public String getTypeVerbose() {
        return "Movie ratings";
    }

    public String getTitle() {
        try {
            return json.getString("title");
        } catch (JSONException e) {
            return "Error";
        }

    }

    public String getDescription() {
        try {
            String title = json.getString("title");
            return "Predictions on IMBD ratings of " + title + ".";
        } catch (JSONException e) {
            return "Error";
        }
    }

    @Override
    public String getDescriptionBrief() {
        try {
            String title = json.getString("title");
            return "IMBD ratings of " + title + ".";
        } catch (JSONException e) {
            return "Error";
        }
    }

    public String getJudgement() {
        try {
            String res = json.getString("result");
            String bid = json.getString("judgementBid");
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
                    (j.getString("targetBidMin")) + " - " +
                    (j.getString("targetBidMax"));
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
