package com.archonmode.artemsinyakov.hallofprophecy.GenericCreatePrediction.MovieRatings;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.helpers.ParserAdapter;

public class MovieItem {
    private JSONObject json;
    private String testItem;

    public MovieItem(JSONObject json) {
        this.json = json;
    }
    public MovieItem(String testString) {
        testItem = testString;
    }
    public String getTestItem() {
        return testItem;
    }
    public String getTitle() {
        try {
            return json.getString("title");
        } catch (JSONException e) {
            return "Error!";
        }
    }

}
