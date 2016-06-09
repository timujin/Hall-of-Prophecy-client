package com.archonmode.artemsinyakov.hallofprophecy.SeriesOfPopups;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;

public class PickAPredictionDialog {

    static public final String[] predictionTypeNames = {"Tweet a prediction",
                                                    "Predict currencies ratio",
                                                    "Predict movie IMDB rating"};
    static public final String[] predictionTypes = {"twitter", "yahooFinance", "movieRating"};

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

    public static SeriesOfPopups constructSeriesOfPopups(String type, Activity c, INextPopupPlease i) {
        switch (type) {
            case "twitter":
                return new TwitterSeriesOfPopups(c, i);
            case "yahooFinance":
                return new YahooFinanceSeriesOfPopups(c, i);
            case "twitter_wager":
                return new TwitterWagerSeriesOfPopups(c, i);
            case "yahooFinance_wager":
                return new YahooFinanceWagerSeriesOfPopups(c, i);
            default:
                Log.e("CREATESERIES", "FAILURE");
                return null;
        }
    }
}
