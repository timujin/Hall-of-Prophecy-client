package com.example.artemsinyakov.hallofprophecy.GenericPredictionVIew;

import android.content.Context;
import android.util.Log;

import com.example.artemsinyakov.hallofprophecy.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.UndeclaredThrowableException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public abstract class GenericPrediction {
    public JSONObject json;
    public Context context;

    protected JSONArray wagers;
    protected JSONArray comments;
    protected String[] processedWagers;
    protected String[] processedComments;


    protected GenericPrediction(JSONObject json, Context context) {
        Log.e("GENERICPREDICTION", json.toString());
        this.json = json;
        this.context = context;
    }

    abstract public String getType();
    abstract public String getTypeVerbose();
    abstract public String getDescription();
    public String getDescriptionBrief() {
        return getDescription();
    }
    abstract public String getJudgement();

    public boolean getDecided() {
        try {
            return json.getString("result") != null && json.getString("result").equals("1");
        } catch (JSONException e) {
            return false;
        }
    }

    public String getURL() {
        try {
            return json.getString("url");
        } catch (JSONException e) {
            return null;
        }
    }

    public String getDueDate() {
        try {
            long datel = json.getLong("dueDate") * 1000;
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(datel);
            if (!getDecided()) {
                return "Due at " +
                        new SimpleDateFormat(context.getResources().getString(R.string.date_format))
                                .format(new java.util.Date(datel));
            } else {
                return "Settled at " +
                        new SimpleDateFormat(context.getResources().getString(R.string.date_format))
                                .format(new java.util.Date(datel));
            }
        } catch (JSONException e) {
            return "Error";
        }
    }

    public String getWagerAuthorAt(int index) {
        try {
            if (wagers == null) {
                wagers = json.getJSONArray("wagers");
            }
            return wagers.getJSONObject(index).getString("handle");
        } catch (JSONException e) {
            return "Error";
        }
    }
    public String getCommentAuthorAt(int index) {
        try {
            if (comments == null) {
                comments = json.getJSONArray("comments");
            }
            return comments.getJSONObject(index).getString("handle");
        } catch (JSONException e) {
            return "Error";
        }
    }

    public String getWagerTextAt(int index) {
        try {
            if (wagers == null) {
                wagers = json.getJSONArray("wagers");
            }
            return processWagerText(wagers.getJSONObject(index));
        } catch (JSONException e) {
            return "Error";
        }
    }
    abstract protected String processWagerText(JSONObject j);

    abstract public boolean hasComments();

    public String getCommentTextAt(int index) {
        if (!this.hasComments()) {
            return "Error";
        }
        try {
            if (comments == null) {
                comments = json.getJSONArray("comments");
            }
            return processCommentText(comments.getJSONObject(index));
        } catch (JSONException e) {
            return "Error";
        }
    }

    public String[] getProcessedWagers() {
        try {
            if (processedWagers != null)
                return processedWagers;
            else {
                if (wagers == null) {
                    wagers = json.getJSONArray("wagers");
                }
                String[] result = new String[wagers.length()];
                for (int i = 0; i<wagers.length();i++) {
                    result[i] = getWagerTextAt(i);
                }
                return result;
            }
        } catch (JSONException e) {
            return new String[]{"Error"};
        }
    }

    public String[] getProcessedComments() {
        if (!this.hasComments()) {
            return new String[]{"Error"};
        }
        try {
            if (processedComments != null)
                return processedComments;
            else {
                if (comments == null) {
                    comments = json.getJSONArray("comments");
                }
                String[] result = new String[comments.length()];
                for (int i = 0; i<comments.length();i++) {
                    result[i] = getCommentTextAt(i);
                }
                return result;
            }
        } catch (JSONException e) {
            return new String[]{"Error"};
        }
    }


    abstract protected String processCommentText(JSONObject j);

    static public GenericPrediction GeneratePredictionFromType(String type, JSONObject json, Context context) {
        switch (type){
            case "twitter":
                return new TwitterPrediction(json, context);
            case "yahooFinance":
                return new YahooPrediction(json, context);
            default:
                return null;
        }
    }

    static public String[] getPredictionTypes() {
        return new String[]{"twitter","yahooFinance"};
    }



}
