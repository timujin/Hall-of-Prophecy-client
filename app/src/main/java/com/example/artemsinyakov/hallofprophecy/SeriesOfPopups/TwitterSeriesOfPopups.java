package com.example.artemsinyakov.hallofprophecy.SeriesOfPopups;

import android.app.Activity;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TwitterSeriesOfPopups implements SeriesOfPopups {

    private Context context;
    private INextPopupPlease cb;

    public ArrayList<AlertDialog> dialogs = new ArrayList<>();

    public String predictionText;
    public String arbiter;
    public long unixDate = 0L;

    public TwitterSeriesOfPopups(Context context, INextPopupPlease cb) {
        this.context = context;
        this.cb = cb;
        setUpDialogs();
        cb.releasePopup();
    }

    private void setUpDialogs() {
        dialogs.add(constructPredictionTextPrompt());
        dialogs.add(constructArbiterHandlePrompt());
        dialogs.add(constructDatePickerDialog());
    }

    private AlertDialog constructPredictionTextPrompt() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View promptView = layoutInflater.inflate(R.layout.input_text_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);
        final TextView textView = (TextView) promptView.findViewById(R.id.textView);
        textView.setText("What do you think will happen?");
        final EditText editText = (EditText) promptView.findViewById(R.id.editText);
        editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(130)});
        alertDialogBuilder.setCancelable(false)
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
                            Toast.makeText(context, "Prediction text is required", Toast.LENGTH_SHORT).show();
                        } else {
                            predictionText = editText.getText().toString();
                            cb.releasePopup();
                            d.dismiss();
                        }
                    }
                });
            }
        });
        return d;
    }
    private AlertDialog constructArbiterHandlePrompt() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View promptView = layoutInflater.inflate(R.layout.input_text_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);
        final TextView textView = (TextView) promptView.findViewById(R.id.textView);
        textView.setText("What Twitter user will judge this?");
        final EditText editText = (EditText) promptView.findViewById(R.id.editText);
        editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(30)});
        editText.setText("@");
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", null);
        final AlertDialog d = alertDialogBuilder.create();
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (editText.getText().length() < 2) {
                            Toast.makeText(context, "Arbiter is required", Toast.LENGTH_SHORT).show();
                        } else {
                            arbiter = editText.getText().toString();
                            cb.releasePopup();
                            d.dismiss();
                        }
                    }
                });
            }
        });
        return d;
    }

    private AlertDialog constructDatePickerDialog() {
        Calendar newCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                if (newDate.before(Calendar.getInstance())) {
                    Toast.makeText(context, "Please pick a date in the future.", Toast.LENGTH_SHORT).show();
                    cb.popupFailure();
                } else {
                    unixDate = newDate.getTimeInMillis();
                    Toast.makeText(context, Long.toString(unixDate), Toast.LENGTH_LONG).show();
                    cb.releasePopup();
                }
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setCancelable(false);
        datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
        return datePickerDialog;
    }

    public ArrayList<AlertDialog> getDialogs() {
        return dialogs;
    }

    public JSONObject getJSONData() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("text", predictionText);
        j.put("arbiterHandle", arbiter.charAt(0)=='@'?arbiter.substring(1).replaceAll("\\s", ""):arbiter.replaceAll("\\s",""));
        j.put("dueDate", unixDate / 1000);
        return j;
    }
}
