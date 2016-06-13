package com.archonmode.artemsinyakov.hallofprophecy.SeriesOfPopups;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.archonmode.artemsinyakov.hallofprophecy.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by newti on 6/11/2016.
 */
public class MovieRatingsWagerSeriesOfPopups implements SeriesOfPopups {


    private Context context;
    private INextPopupPlease cb;

    public ArrayList<AlertDialog> dialogs = new ArrayList<>();

    private int bidLow;
    private int bidHigh;

    public MovieRatingsWagerSeriesOfPopups(Context context, INextPopupPlease cb) {
        this.context = context;
        this.cb = cb;
        setUpDialogs();
    }


    private void setUpDialogs() {
        dialogs.add(constructBidRangeDialog());
        cb.startRolling();
    }

    private AlertDialog constructBidRangeDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("What do you think of this movie's ratings?");
        alertDialogBuilder.setItems(new String[]{
                "0-20",
                "21-40",
                "41-60",
                "61-80",
                "81-90",
                "91-100"
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bidLow = new int[]{0,21,41,61,81,91}[which];
                bidHigh = new int[]{20,40,60,80,90,100}[which];
                cb.releasePopup();
            }
        });
        alertDialogBuilder.setCancelable(true);
        return alertDialogBuilder.create();
    }

    public ArrayList<AlertDialog> getDialogs() {
        return dialogs;
    }

    public JSONObject getJSONData() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("targetBidMin", bidLow);
        j.put("targetBidMax", bidHigh);
        return j;
    }

}
