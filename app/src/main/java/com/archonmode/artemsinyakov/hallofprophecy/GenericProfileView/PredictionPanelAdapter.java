package com.archonmode.artemsinyakov.hallofprophecy.GenericProfileView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.archonmode.artemsinyakov.hallofprophecy.GenericPredictionVIew.GenericPrediction;
import com.archonmode.artemsinyakov.hallofprophecy.R;


public class PredictionPanelAdapter extends ArrayAdapter<GenericPrediction> {

    private final Context context;
    private final GenericPrediction[] values;

    public PredictionPanelAdapter(Context context, GenericPrediction[] values) {
        super(context, R.layout.prediction_panel, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        if (convertView != null) {
            rowView = convertView;
        }
        else {
            rowView = inflater.inflate(R.layout.prediction_panel, parent, false);
        }
        TextView title = (TextView) rowView.findViewById(R.id.title);
        TextView type = (TextView) rowView.findViewById(R.id.type);
        TextView dueText = (TextView) rowView.findViewById(R.id.due_text);


        GenericPrediction object = values[position];
        if (object == null) {
            title.setText("Error");
        } else {
            title.setText(object.getDescriptionBrief());
            type.setText(object.getType());
            dueText.setText(object.getDueDate());
        }
        return rowView;
    }
}
