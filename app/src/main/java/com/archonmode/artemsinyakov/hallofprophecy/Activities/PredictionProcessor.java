package com.archonmode.artemsinyakov.hallofprophecy.Activities;

import android.content.Context;

import com.archonmode.artemsinyakov.hallofprophecy.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

// id name result dueDate, currencies
public class PredictionProcessor {

    public String type;
    public JSONObject json;
    Context context;

    PredictionProcessor(String _type, JSONObject _json, Context _context) {
        type = _type;
        json = _json;
        context = _context;
    }

    public String getText(){
        try {
            if (type.equals("yahooFinance")) {
                String cur1 = json.getString("currencies").substring(0, 3);
                String cur2 = json.getString("currencies").substring(3, 6);
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

    public String getBriefText() {
        try {
            if (type.equals("yahooFinance")) {
                String cur1 = json.getString("currencies").substring(0, 3);
                String cur2 = json.getString("currencies").substring(3, 6);
                return cur1 + " to " + cur2;
            } else if (type.equals("twitter")) {
                String predictionText = json.getString("text");
                return predictionText.substring(0, Math.min(predictionText.length(), 60));
            } else {
                return "Wrong prediction";
            }
        } catch (JSONException e) {
            return "Wrong prediction";
        }
    }

    public Boolean getSuccess() {
        if (type.equals("yahooFinance")) {
            return true;
        } else if (type.equals("twitter")) {
            String res;
            try {
                res = json.getString("result");
            } catch (JSONException e) {
                res = "Undecided";
            }
            if (res != null)
                if (res.equals("1")) {
                    return true;
                } else {
                    return false;
                }
            else {
                return false;
            }
        } else {
            return false;
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

    public static class predictionsComparator implements Comparator<PredictionProcessor> {
        @Override
        public int compare(PredictionProcessor obj1, PredictionProcessor obj2) {
            try {
                Date date1 = new java.util.Date(obj1.json.getLong("dueDate") * 1000);
                Date date2 = new java.util.Date(obj2.json.getLong("dueDate") * 1000);
                if (date1.after(date2)) {
                    return 1;
                } else if (date2.after(date1)) {
                    return -1;
                } else {
                    return 0;
                }
            } catch (JSONException e) {
                return 0;
            }
        }
    }
}
