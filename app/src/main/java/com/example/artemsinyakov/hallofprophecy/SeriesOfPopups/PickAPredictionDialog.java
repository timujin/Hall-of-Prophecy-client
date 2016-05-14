package com.example.artemsinyakov.hallofprophecy.SeriesOfPopups;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.artemsinyakov.hallofprophecy.Activities.CreateTwitterPrediction;
import com.example.artemsinyakov.hallofprophecy.R;

public class PickAPredictionDialog {

    static public final String[] predictionTypeNames = {"Tweet an arbitrary prediction",
                                                    "Future currencies ratio"};
    static public final String[] predictionTypes = {"twitter", "yahooFinance"};

    public static void showPredictionDialog(Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("What type of prediction?");
        final Context c = context;
        builder.setItems(predictionTypeNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openSeriesOfPopups(predictionTypes[which], c);
            }
        });
        builder.create().show();
    }

    private static void openSeriesOfPopups(String type, Context context) {
        Intent intent = new Intent(context, SoPPActivity.class);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    public static SeriesOfPopups constructSeriesOfPopups(String type, Context c, INextPopupPlease i) {
        switch (type) {
            case "twitter":
                return new TwitterSeriesOfPopups(c, i);
            case "yahooFinance":
                return new YahooFinanceSeriesOfPopups(c, i);
            default:
                return null;
        }
    }
}
