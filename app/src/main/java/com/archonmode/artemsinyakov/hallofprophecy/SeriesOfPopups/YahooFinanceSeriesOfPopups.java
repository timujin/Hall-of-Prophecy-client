package com.archonmode.artemsinyakov.hallofprophecy.SeriesOfPopups;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
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

import com.archonmode.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.archonmode.artemsinyakov.hallofprophecy.R;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import cz.msebera.android.httpclient.Header;

public class YahooFinanceSeriesOfPopups implements SeriesOfPopups  {

    private Activity context;
    private INextPopupPlease cb;

    public ArrayList<AlertDialog> dialogs = new ArrayList<>();

    private String currency;
    private boolean direction;
    private String value;
    public long unixDate = 0L;

    private String[] currencies;


    public YahooFinanceSeriesOfPopups(Activity context, INextPopupPlease cb) {
        this.context = context;
        this.cb = cb;
        downloadCurrencies();
    }

    private void downloadCurrencies(){
        HoPRequestHelper.get("/yahooFinance", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject json = new JSONObject(new String(responseBody));
                    Log.e("1", json.toString());
                    JSONArray curs = json.getJSONArray("currencies");
                    ArrayList<String> _currencies = new ArrayList<String>();
                    for (int i = 0; i<curs.length(); i++) {
                        _currencies.add(curs.getString(i));
                    }
                    currencies = _currencies.toArray(new String[0]);
                    setUpDialogs();
                } catch (JSONException e) {
                    Log.e("JSON", e.toString());
                    return;
                }
                Log.d("1", new String(responseBody));
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "Could not download currencies", Toast.LENGTH_LONG).show();
                context.finish();
            }
        });
    }

    private void setUpDialogs() {
        dialogs.add(constructCurrenciesPickerDialog());
        dialogs.add(constructMoreLessDialog());
        dialogs.add(constructValueDialog());
        dialogs.add(constructDatePickerDialog());
        cb.startRolling();
    }

    private AlertDialog constructCurrenciesPickerDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Pick currencies");
        String[] readableCurrencies = new String[currencies.length];
        for (int i=0;i<currencies.length;i++){
            readableCurrencies[i] = (currencies[i].substring(0, 3)+" - "+currencies[i].substring(3, 6));
        }
        alertDialogBuilder.setItems(readableCurrencies, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currency = currencies[which];
                cb.releasePopup();
            }
        });
        alertDialogBuilder.setCancelable(true);
        AlertDialog d = alertDialogBuilder.create();
        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cb.cancel();
            }
        });
        return d;
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
        AlertDialog d = alertDialogBuilder.create();
        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cb.cancel();
            }
        });
        return d;
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
        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cb.cancel();
            }
        });
        return d;
    }
    private AlertDialog constructDatePickerDialog() {
        Calendar newCalendar = Calendar.getInstance();
        int nextMonth = newCalendar.get(Calendar.MONTH);
        if (nextMonth == Calendar.DECEMBER) {
            nextMonth = Calendar.JANUARY;
        } else {
            nextMonth++;
        }
        final DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                if (newDate.before(Calendar.getInstance())) {
                    Toast.makeText(context, "Please pick a date in the future.", Toast.LENGTH_SHORT).show();
                    cb.popupFailure();
                } else {
                    unixDate = newDate.getTimeInMillis();
                    cb.releasePopup();
                }
            }
        },newCalendar.get(Calendar.YEAR), nextMonth, newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setCancelable(false);
        datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
        return datePickerDialog;
    }

    public ArrayList<AlertDialog> getDialogs() {
        return dialogs;
    }

    public JSONObject getJSONData() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("currencies", currency);
        j.put("targetBid", value);
        j.put("bidDirection", direction?"le":"ge");
        j.put("dueDate", unixDate / 1000);
        return j;
    }
}
