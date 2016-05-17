package com.example.artemsinyakov.hallofprophecy.SeriesOfPopups;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.artemsinyakov.hallofprophecy.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class TwitterWagerSeriesOfPopups implements SeriesOfPopups {

    private Context context;
    private INextPopupPlease cb;

    public ArrayList<AlertDialog> dialogs = new ArrayList<>();

    boolean wager;

    public TwitterWagerSeriesOfPopups(Context context, INextPopupPlease cb) {
        this.context = context;
        this.cb = cb;
        setUpDialogs();
        cb.startRolling();
    }

    private void setUpDialogs() {
        dialogs.add(constructOpinionPrompt());
        Log.e("DIALOGS", String.valueOf(dialogs.size()));
    }

    private AlertDialog constructOpinionPrompt() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("What's your opinion?");
        alertDialogBuilder.setItems(new String[]{"It will happen!","It will not happen!"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                wager = (which == 0);
                cb.releasePopup();
            }
        });
        alertDialogBuilder.setCancelable(false);
        return alertDialogBuilder.create();
    }

    public ArrayList<AlertDialog> getDialogs() {
        Log.e("GETDIALOGS",  String.valueOf(dialogs.size()));
        return dialogs;
    }

    public JSONObject getJSONData() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("wager", wager ? "1" : "0");
        return j;
    }
}