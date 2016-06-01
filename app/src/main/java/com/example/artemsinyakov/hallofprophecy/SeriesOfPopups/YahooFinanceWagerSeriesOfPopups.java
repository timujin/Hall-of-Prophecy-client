package com.example.artemsinyakov.hallofprophecy.SeriesOfPopups;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.example.artemsinyakov.hallofprophecy.R;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import cz.msebera.android.httpclient.Header;

/**
 * Created by newti on 5/15/2016.
 */
public class YahooFinanceWagerSeriesOfPopups implements SeriesOfPopups  {

    private Context context;
    private INextPopupPlease cb;

    public ArrayList<AlertDialog> dialogs = new ArrayList<>();

    private boolean direction;
    private String value;

    public YahooFinanceWagerSeriesOfPopups(Context context, INextPopupPlease cb) {
        this.context = context;
        this.cb = cb;
        setUpDialogs();
    }


    private void setUpDialogs() {
        dialogs.add(constructMoreLessDialog());
        dialogs.add(constructValueDialog());
        cb.startRolling();
    }

    private AlertDialog constructMoreLessDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Do you predict it to be more or less that a particular value?");
        alertDialogBuilder.setItems(new String[]{"More than...", "Less than..."}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                direction = (which == 1);
                cb.releasePopup();
            }
        });
        alertDialogBuilder.setCancelable(true);
        return alertDialogBuilder.create();
    }

    private AlertDialog constructValueDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View promptView = layoutInflater.inflate(R.layout.input_text_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);
        final TextView textView = (TextView) promptView.findViewById(R.id.textView);
        textView.setText("...than what value?");
        final EditText editText = (EditText) promptView.findViewById(R.id.editText);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        alertDialogBuilder.setCancelable(true)
                .setPositiveButton("OK", null);
        final AlertDialog d = alertDialogBuilder.create();
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (editText.getText().length() < 1) {
                            Toast.makeText(context, "A number is required.", Toast.LENGTH_SHORT).show();
                        } else {
                            value = editText.getText().toString();
                            d.dismiss();
                            cb.releasePopup();
                        }
                    }
                });
            }
        });
        return d;
    }
    public ArrayList<AlertDialog> getDialogs() {
        return dialogs;
    }

    public JSONObject getJSONData() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("targetBid", value);
        j.put("bidDirection", direction?"le":"ge");
        return j;
    }
}