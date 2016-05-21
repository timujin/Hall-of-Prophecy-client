package com.example.artemsinyakov.hallofprophecy.SeriesOfPopups;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.example.artemsinyakov.hallofprophecy.Activities.CreateTwitterPrediction;
import com.example.artemsinyakov.hallofprophecy.Activities.DisplayGenericPrediction;
import com.example.artemsinyakov.hallofprophecy.GenericPredictionVIew.ViewGenericPrediction;
import com.example.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.example.artemsinyakov.hallofprophecy.R;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.twitter.sdk.android.Twitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class SoPPActivity extends AppCompatActivity {

    private String type;
    private SeriesOfPopups s;
    private int dialogNum = 0;
    ArrayList<AlertDialog> dialogs;

    boolean dialogsConstructed = false;
    boolean extraDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series_of_popups);
        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        Log.e("SOPP", type);
        final SoPPActivity context = this;
        s = PickAPredictionDialog.constructSeriesOfPopups(type, this, new INextPopupPlease() {
                @Override
                public void releasePopup() {
                    callUponDialog();
                }
                public void popupFailure() {
                    dialogNum -= 1;
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            callUponDialog();
                        }
                    }, 500);
                }
                public void startRolling() {
                    extraDataLoaded = true;
                    if (dialogsConstructed)
                        context.startRolling();
                }
        });

        dialogsConstructed = true;
        if (extraDataLoaded) {
            startRolling();
        }
    }

    private void startRolling() {
        dialogs = s.getDialogs();
        callUponDialog();
    }

    private void callUponDialog() {
        if (dialogs.size() <= dialogNum) {
            recordPrediction();
        } else {
            dialogs.get(dialogNum).show();
            dialogNum += 1;
        }
    }

    private void recordPrediction() {
        JSONObject json = new JSONObject();
        try {
            json = s.getJSONData();
            json.put("key", Twitter.getInstance().core.getSessionManager().getActiveSession().getAuthToken().token);
        } catch (JSONException e) {
            predictionFailure();
        }
        HoPRequestHelper.post(this, "/prediction/" + type, json, new AsyncHttpResponseHandler() {
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
                Log.e("HoP", new String(responseBody));
                predictionFailure();
            }
        });
    }

    private void predictionFailure() {
        Toast.makeText(this, "Prediction not created.", Toast.LENGTH_LONG).show();
    }

    private void predictionSuccess(String url) {
        Toast.makeText(this, "Prediction created!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(SoPPActivity.this, ViewGenericPrediction.class);
        intent.putExtra("url", url);
        intent.putExtra("type", type);
        startActivity(intent);
        finish();
    }



}
