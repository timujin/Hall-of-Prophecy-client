package com.archonmode.artemsinyakov.hallofprophecy.Activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.archonmode.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.archonmode.artemsinyakov.hallofprophecy.R;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.twitter.sdk.android.Twitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class CreateYahooFinancePrediction extends AppCompatActivity {

    private DatePickerDialog datePickerDialog;
    private TextView dateLabel;
    private long unixDate = 0;

    private Button currency;
    private Button direction;
    private EditText value;

    private String[] currencies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_yahoo_finance_prediction);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setUpUIElements();
        setUpDatePicker();


        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Button)findViewById(R.id.confirm)).setClickable(false);
                if (!verifyPredction()) {
                    ((Button)findViewById(R.id.confirm)).setClickable(true);
                    return;
                }
                recordPrediction(unixDate / 1000);
            }
        });
    }

    private void setUpUIElements() {
        final Context context = this;

        value = (EditText) findViewById(R.id.value_picker);

        currency = (Button)findViewById(R.id.currencies_picker);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                    Log.e("1", currencies[0]);
                    builder.setTitle("Pick currencies");
                    builder.setItems(currencies, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currency.setText(currencies[which]);
                        }
                    });
                } catch (JSONException e) {
                    Log.e("JSON", e.toString());
                    return;
                }
                Log.d("1", new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "Could not download currencies", Toast.LENGTH_LONG).show();
            }
        });
        currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.show();
            }
        });

        final String[] directions = {"More than...", "Less than..."};
        direction = (Button)findViewById(R.id.direction_picker);
        final AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("Pick direction");
        builder2.setItems(directions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                direction.setText(directions[which]);
            }
        });
        direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder2.show();
            }
        });

    }

    private void setUpDatePicker() {
        dateLabel = (TextView) findViewById(R.id.dateLabel);
        final Button datePickerConfirm = (Button) findViewById(R.id.datePickerConfirm);
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        final Activity context = this;
        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                Log.e("1", dateFormatter.format(newDate.getTime()));
                Log.e("2", dateFormatter.format(Calendar.getInstance().getTime()));
                if (newDate.before(Calendar.getInstance())) {
                    Toast.makeText(context, "Please pick a date in the future.", Toast.LENGTH_SHORT).show();
                    return;
                }
                dateLabel.setText(dateFormatter.format(newDate.getTime()));
                unixDate = newDate.getTimeInMillis();
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });
    }

    private void recordPrediction(long date) {
        JSONObject json = new JSONObject();
        try {
            json.put("currencies", currency.getText());
            json.put("targetBid", value.getText());
            json.put("bidDirection", direction.getText().equals("More than...")?"ge":"le");
            json.put("dueDate", date);
            json.put("key", Twitter.getInstance().core.getSessionManager().getActiveSession().getAuthToken().token);
        } catch (JSONException e) {
            predictionFailure();
        }
        Log.e("Sending json", json.toString());
        HoPRequestHelper.post(this, "/prediction/yahooFinance", json, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject json = new JSONObject(new String(responseBody));

                    predictionSuccess(json.getString("url"));
                } catch (JSONException e) {
                    Log.e("JSON", e.toString());
                    predictionFailure();
                    return;
                }
                Log.d("1", new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                predictionFailure();
            }
        });
    }

    private void predictionFailure() {
        ((Button)findViewById(R.id.confirm)).setClickable(true);
        Toast.makeText(this, "Prediction not created.", Toast.LENGTH_LONG).show();
    }

    private void predictionSuccess(String url) {
        ((Button)findViewById(R.id.confirm)).setClickable(true);
        Toast.makeText(this, "Prediction created!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(CreateYahooFinancePrediction.this, DisplayGenericPrediction.class);
        intent.putExtra("type", "yahooFinance");
        intent.putExtra("url", url);
        startActivity(intent);
        finish();
    }

    private boolean verifyPredction() {
        if (currency.getText().equals(getString(R.string.Yahoo_currency_button))) {
            Toast.makeText(this, "Please pick a currency", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (direction.getText().equals(getString(R.string.Yahoo_direction))) {
            Toast.makeText(this, "Please pick direction", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (value.getText().equals("") || value.getText().equals("Value")) {
            Toast.makeText(this, "Please input value", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (unixDate < 1) {
            Toast.makeText(this, "Date is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}
