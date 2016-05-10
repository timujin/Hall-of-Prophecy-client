package com.example.artemsinyakov.hallofprophecy.Activities;

import android.content.Context;
import android.util.Log;

import com.example.artemsinyakov.hallofprophecy.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

// id name result dueDaye, currencies
public class PredictionProcessor {

    String type;
    JSONObject json;
    Context context;

    PredictionProcessor(String _type, JSONObject _json, Context _context) {
        type = _type;
        json = _json;
        context = _context;
    }

    public String getText(){
        try {
            if (type.equals("yahooFinance")) {
                String cur1 = json.getString("currencies");//.substring(0, 2);
                String cur2 = json.getString("currencies");//.substring(3, 5);
                return "The ratio of " + cur1 + " to " + cur2;
            } else if (type.equals("twitter")) {
                String predictionText = json.getString("text");
                String arbiter = json.getString("arbiterHandle");
                String name = json.getString("name");
                return name + " claims that " + predictionText + "\n@" + arbiter + " will judge this.";
            } else {
                return "Wrong prediction";
            }
        } catch (JSONException e) {
            return "Wrong prediction";
        }
    }

    public String getDueDate() {
        try {
            return "Due: " +
                    new SimpleDateFormat(context.getResources().getString(R.string.date_format))
                            .format(new java.util.Date(json.getLong("dueDate") * 1000));
        } catch (JSONException e) {
            return "Error";
        }
    }

    public String getResult() {
        if (type.equals("yahooFinance")) {
            return "Yahoo"; // TODO
        } else if (type.equals("twitter")) {
            String res;
            try {
                res = json.getString("result");
            } catch (JSONException e) {
                res = "Undecided";
            }
            if (res != null)
                if (res.equals("1")) {
                    return "True";
                } else if (res.equals("-1")) {
                    return "False";
                } else {
                    return "Undecided";
                }
            else {
                return "Undecided";
            }
        } else {
            return "Error";
        }

    }
}
