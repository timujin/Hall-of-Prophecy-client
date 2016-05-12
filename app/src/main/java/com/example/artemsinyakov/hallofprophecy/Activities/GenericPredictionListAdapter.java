package com.example.artemsinyakov.hallofprophecy.Activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.artemsinyakov.hallofprophecy.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class GenericPredictionListAdapter extends ArrayAdapter<PredictionProcessor> {
    private final Context context;
    private final PredictionProcessor[] values;

    public GenericPredictionListAdapter(Context context, PredictionProcessor[] values) {
        super(context, R.layout.profile_prediction_view, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /*LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        if (convertView != null) {
            rowView = convertView;
        }
        else {
            rowView = inflater.inflate(R.layout.profile_prediction_view, parent, false);
        }
        TextView text = (TextView) rowView.findViewById(R.id.text);
        TextView date = (TextView) rowView.findViewById(R.id.date);
        JSONObject object = values[position];
        try {
            text.setText(object.getString("text"));
            date.setText(new SimpleDateFormat("MM-dd-yyyy").format(new java.util.Date(object.getLong("dueDate") * 1000)));
            String result = object.getString("result");
            if (result.equals("1")) {
                rowView.setBackgroundColor(context.getResources().getColor(R.color.predictionTrue));
            } else if (result.equals("-1")) {
                rowView.setBackgroundColor(context.getResources().getColor(R.color.predictionFalse));
            }
        } catch (JSONException e) {
            text.setText(context.getResources().getString(R.string.Invalid_prediction));
        }
        return rowView;*/
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        if (convertView != null) {
            rowView = convertView;
        }
        else {
            rowView = inflater.inflate(R.layout.profile_prediction_view, parent, false);
        }
        TextView text = (TextView) rowView.findViewById(R.id.text);
        TextView date = (TextView) rowView.findViewById(R.id.date);
        PredictionProcessor object = values[position];
        text.setText(object.getBriefText());
        date.setText(object.getDueDate());
        boolean result = object.getSuccess();
        if (result) {
            rowView.setBackgroundColor(context.getResources().getColor(R.color.predictionTrue));
        } else  {
            //rowView.setBackgroundColor(context.getResources().getColor(R.color.predictionFalse));
        }
        return rowView;
    }
}