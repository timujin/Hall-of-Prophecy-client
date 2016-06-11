package com.archonmode.artemsinyakov.hallofprophecy.GenericCreatePrediction.MovieRatings;

import android.view.View;
import android.view.ViewGroup;

import com.archonmode.artemsinyakov.hallofprophecy.InfiniteScroll.InfiniteScrollListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MoviesListAdapter extends InfiniteScrollListAdapter {

    // A placeholder for all the data points
    private List<MovieItem> entries = new ArrayList<MovieItem>();
    private NewPageListener newPageListener;

    // A demo listener to pass actions from view to adapter
    public static abstract class NewPageListener {
        public abstract void onScrollNext();
        public abstract View getInfiniteScrollListView(int position, View convertView, ViewGroup parent);
    }

    public MoviesListAdapter(NewPageListener newPageListener) {
        this.newPageListener = newPageListener;
    }

    public void addEntriesToTop(List<MovieItem> entries) {
        // Add entries in reversed order to achieve a sequence used in most of messaging/chat apps
        if (entries != null) {
            Collections.reverse(entries);
        }
        // Add entries to the top of the list
        this.entries.addAll(0, entries);
        notifyDataSetChanged();
    }

    public void addEntriesToBottom(List<MovieItem> entries) {
        // Add entries to the bottom of the list
        this.entries.addAll(entries);
        notifyDataSetChanged();
    }

    public void clearEntries() {
        // Clear all the data points
        this.entries.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Object getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    protected void onScrollNext() {
        if (newPageListener != null) {
            newPageListener.onScrollNext();
        }
    }

    @Override
    public View getInfiniteScrollListView(int position, View convertView, ViewGroup parent) {
        if (newPageListener != null) {
            return newPageListener.getInfiniteScrollListView(position, convertView, parent);
        }
        return convertView;
    }
}
