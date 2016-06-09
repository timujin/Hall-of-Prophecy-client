package com.archonmode.artemsinyakov.hallofprophecy.Activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class CreateTwitterPrediction extends AppCompatActivity {

    private DatePickerDialog datePickerDialog;
    private TextView dateLabel;
    private long unixDate = 0;
    private EditText predictionText;
    private EditText arbiter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_twitter_prediction);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setUpDatePicker();

        predictionText = (EditText) findViewById(R.id.prediction_text);
        arbiter = (EditText) findViewById(R.id.arbiter);
        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Button)findViewById(R.id.confirm)).setClickable(false);
                if (!verifyPredction()) {
                    ((Button)findViewById(R.id.confirm)).setClickable(true);
                    return;
                }
                recordPrediction(predictionText.getText().toString(), arbiter.getText().toString(), unixDate / 1000);
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
        datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
        datePickerConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });
    }

    private void recordPrediction(String text, String arbiter, long date) {
        JSONObject json = new JSONObject();
        try {
            json.put("text", text);
            json.put("arbiterHandle", arbiter.charAt(0)=='@'?arbiter.substring(1).replaceAll("\\s", ""):arbiter.replaceAll("\\s",""));
            json.put("dueDate", date);
            json.put("key", Twitter.getInstance().core.getSessionManager().getActiveSession().getAuthToken().token);
        } catch (JSONException e) {
            predictionFailure();
        }
        Log.e("Sending json", json.toString());
        HoPRequestHelper.post(this, "/prediction/twitter", json, new AsyncHttpResponseHandler() {
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
        Intent intent = new Intent(CreateTwitterPrediction.this, DisplayGenericPrediction.class);
        intent.putExtra("url", url);
        intent.putExtra("type", "twitter");
        startActivity(intent);
        finish();
    }

    private boolean verifyPredction() {
        if (predictionText.getText().length() < 1) {
            Toast.makeText(this, "Prediction text is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (arbiter.getText().length() < 2) {
            Toast.makeText(this, "Arbiter is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        // TODO: check is arbiter exists
        if (unixDate < 1) {
            Toast.makeText(this, "Date is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}
