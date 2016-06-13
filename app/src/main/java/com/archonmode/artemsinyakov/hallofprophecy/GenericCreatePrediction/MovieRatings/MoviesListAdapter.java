package com.archonmode.artemsinyakov.hallofprophecy.GenericCreatePrediction.MovieRatings;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.archonmode.artemsinyakov.hallofprophecy.InfiniteScroll.InfiniteScrollListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MoviesListAdapter extends InfiniteScrollListAdapter {

    // A placeholder for all the data points
    private List<MovieItem> entries = new ArrayList<MovieItem>();
    private List<MovieItem> visibleEntries = new ArrayList<MovieItem>();
    private NewPageListener newPageListener;
    private String filterString = null;

    // A demo listener to pass actions from view to adapter
    public static abstract class NewPageListener {
        public abstract void onScrollNext();
        public abstract View getInfiniteScrollListView(int position, View convertView, ViewGroup parent);
    }

    public MoviesListAdapter(NewPageListener newPageListener) {
        this.newPageListener = newPageListener;
    }

    public void addEntriesToBottom(List<MovieItem> entries) {
        // Add entries to the bottom of the list
        this.entries.addAll(entries);
        updateVisibleList();
        notifyDataSetChanged();
    }

    public void clearEntries() {
        // Clear all the data points
        this.entries.clear();
        this.visibleEntries.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return visibleEntries.size();
    }

    @Override
    public Object getItem(int position) {
        if (filterString == null)
            return entries.get(position);
        else {
            return visibleEntries.get(position);
        }
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

    private void updateVisibleList() {
        visibleEntries = new ArrayList<>();
        for (MovieItem item : entries) {
            if (compareToFilter(item, filterString))
                visibleEntries.add(item);
        }
    }

    public void filter(String filter) {
        this.filterString = filter;
        updateVisibleList();
        notifyDataSetChanged();
    }

    private boolean compareToFilter(MovieItem item, String filter) {
        return  filter == null || filter.equals("") || item.getTitle().toLowerCase().contains(filter);
    }
}
