package com.example.artemsinyakov.hallofprophecy.GenericPredictionVIew;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.artemsinyakov.hallofprophecy.Activities.PredictionProcessor;
import com.example.artemsinyakov.hallofprophecy.Activities.ViewPrediction;
import com.example.artemsinyakov.hallofprophecy.GenericProfileView.GPVActivity;
import com.example.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.example.artemsinyakov.hallofprophecy.R;
import com.example.artemsinyakov.hallofprophecy.SeriesOfPopups.INextPopupPlease;
import com.example.artemsinyakov.hallofprophecy.SeriesOfPopups.PickAPredictionDialog;
import com.example.artemsinyakov.hallofprophecy.SeriesOfPopups.SeriesOfPopups;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.twitter.sdk.android.Twitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

public class ViewGenericPrediction extends AppCompatActivity {

    private String url;
    private String type;

    private GenericPrediction prediction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_generic_prediction);

        Intent intent = getIntent();
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.VIEW")) {
            Log.e("1", intent.getAction());
            Log.e("1", intent.getData().toString());
            ArrayList<String> path = (new ArrayList<>(Arrays.asList(intent.getData().getPath().split("/"))));
            Log.e("1", path.toString());
            url = path.get(path.size()-1);
            type = path.get(path.size()-2);
            Log.e("dsf", type+url);
        } else {
            url = intent.getStringExtra("url");
            type = intent.getStringExtra("type");
        }

        downloadPrediction();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_prediction, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        final Context context = this;
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Prediction: ");
                String shareMessage;
                shareMessage = context.getResources().getString(R.string.site) + "/prediction/" + type + "/" + url;
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                        shareMessage);
                startActivity(Intent.createChooser(shareIntent,
                        "Share prediction."));
                return true;
            }
        });
        return true;
    }

    private void downloadPrediction() {
        final Context context = this;
        HoPRequestHelper.get("/prediction/"+type+"/" + url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject json = new JSONObject(new String(responseBody));
                    prediction = GenericPrediction.GeneratePredictionFromType(type, json, context);
                    if (prediction == null)
                        throw new RuntimeException();
                    populateUIElements();
                } catch (JSONException e) {
                    Log.e("JSON", e.toString());
                    Toast.makeText(context, "Could not display prediction - JSON does not parse", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "Could not display prediction - network failure", Toast.LENGTH_LONG).show();
            }
        });
    }
    ListView wagersList;
    ListView commentsList;
    Button   swapWagersComments;
    private void populateUIElements() {
        final TextView typeText = (TextView) findViewById(R.id.type_text);
        final TextView dueText = (TextView) findViewById(R.id.due_text);
        final TextView mainText = (TextView) findViewById(R.id.main_text);
        final TextView judgementText = (TextView) findViewById(R.id.judgement_text);
                       wagersList = (ListView) findViewById(R.id.wagers_list);
                       commentsList = (ListView) findViewById(R.id.comments_list);
        final Button   makeWager = (Button) findViewById(R.id.make_wager);
        final Button   makeComment = (Button) findViewById(R.id.make_comment);
                       swapWagersComments = (Button) findViewById(R.id.swap_wagers_comments);

        populateTexts(typeText, dueText, mainText, judgementText);
        populateLists(wagersList, commentsList);
        activateButtons(makeWager, makeComment, swapWagersComments);
        activateCreateWagerComment(makeWager, makeComment);
        activateClickOnList(wagersList, commentsList);

        showUI();
    }

    private void populateTexts(TextView typeText, TextView dueText, TextView mainText, TextView judgementText) {
        typeText.setText(prediction.getTypeVerbose());
        dueText.setText(prediction.getDueDate());
        mainText.setText(prediction.getDescription());judgementText.setText(prediction.getJudgement());
    }
    private void populateLists(ListView wagersList, ListView commentsList) {
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.simple_text_list_view, prediction.getProcessedWagers());
        wagersList.setAdapter(adapter);
        if (prediction.hasComments()) {
            adapter = new ArrayAdapter<>(this, R.layout.simple_text_list_view, prediction.getProcessedComments());
            commentsList.setAdapter(adapter);
        }
        commentsList.setVisibility(View.GONE);
    }
    private void activateButtons(Button makeWager, Button makeComment, Button swapWagersComments) {
        if (!prediction.hasComments()) {
            makeComment.setVisibility(View.GONE);
            swapWagersComments.setVisibility(View.GONE);
        } else {
            swapWagersComments.setVisibility(View.VISIBLE);
            swapWagersComments.setText("Wagers");
            swapWagersComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    swapLists();
                }
            });
        }
    }


    boolean showingComments = false;
    private void swapLists() {
        if (showingComments) {
            showingComments = false;
            swapWagersComments.setText("Comments");
            wagersList.setVisibility(View.GONE);
            commentsList.setVisibility(View.VISIBLE);
        } else {
            showingComments = true;
            swapWagersComments.setText("Wagers");
            wagersList.setVisibility(View.VISIBLE);
            commentsList.setVisibility(View.GONE);
        }
    }

    ///////////////// Wager/comment buttons


    boolean dialogsConstructed = false;
    boolean extraDataLoaded = false;
    ArrayList<AlertDialog> wagerDialogs;
    SeriesOfPopups s;
    int dialogNum = 0;

    private void activateCreateWagerComment(Button makeWager, Button makeComment) {
        final ViewGenericPrediction context = this;
        s = PickAPredictionDialog.constructSeriesOfPopups(type + "_wager", context, new INextPopupPlease() {
            @Override
            public void releasePopup() {
                callUponDialog();
            }
            @Override
            public void popupFailure() {
                dialogNum -= 1;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        callUponDialog();
                    }
                }, 500);
            }
            @Override
            public void startRolling() {

            }
        });
        makeWager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWagerPopup();
            }
        });

        makeComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                constructCommentDialog().show();
            }
        });
    }

    private void showWagerPopup() {
        wagerDialogs = s.getDialogs();
        callUponDialog();
    }

    private void callUponDialog() {
        if (wagerDialogs.size() <= dialogNum) {
            recordWager();
        } else {
            wagerDialogs.get(dialogNum).show();
            dialogNum += 1;
        }
    }

    public void recordWager() {
        Log.e("1", "RECORDING WAGER");
        JSONObject json;
        try {
            json = s.getJSONData();
            json.put("key", Twitter.getInstance().core.getSessionManager().getActiveSession().getAuthToken().token);
        } catch (JSONException e) {
            Toast.makeText(this, "Could not record wager.", Toast.LENGTH_LONG).show();
            return;
        }
        Log.e("1", json.toString());
        final Activity activity = this;
        HoPRequestHelper.post(this, "/prediction/"+type+"/wager/"+url, json, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Intent intent = new Intent(ViewGenericPrediction.this, ViewGenericPrediction.class);
                intent.putExtra("url", url);
                intent.putExtra("type", type);
                Toast.makeText(activity, "Wager made!", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(activity, "You can't make a new wager.", Toast.LENGTH_LONG).show();
                Log.e("2", new String(responseBody));
            }
        });
    }

    private AlertDialog constructCommentDialog() {
        final Context context = this;
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.input_text_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);
        final TextView textView = (TextView) promptView.findViewById(R.id.textView);
        textView.setText("Write a comment.");
        final EditText editText = (EditText) promptView.findViewById(R.id.editText);
        alertDialogBuilder.setCancelable(true)
                .setPositiveButton("OK", null);
        final AlertDialog d = alertDialogBuilder.create();
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (editText.getText().length() < 1) {
                            Toast.makeText(context, "A text is required.", Toast.LENGTH_SHORT).show();
                        } else {
                            recordComment(editText.getText().toString());
                        }
                    }
                });
            }
        });
        return d;
    }

    private void recordComment(String comment) {
        Log.e("1", "RECORDING COMMENT");
        JSONObject json = new JSONObject();
        try {
            json.put("text", comment);
            json.put("author", Twitter.getInstance().core.getSessionManager().getActiveSession().getAuthToken().token);
        } catch (JSONException e) {
            Toast.makeText(this, "Could not record comment.", Toast.LENGTH_LONG).show();
        }
        final Activity activity = this;
        Log.e("54321", json.toString());
        HoPRequestHelper.post(this, "/prediction/"+type+ "/comment/" + url, json, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Intent intent = new Intent(ViewGenericPrediction.this, ViewGenericPrediction.class);
                intent.putExtra("url", url);
                intent.putExtra("type", type);
                Toast.makeText(activity, "Comment made!", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(activity, "You can't make a new comment.", Toast.LENGTH_LONG).show();
                Log.e("2", new String(responseBody));
            }
        });
    }


    ///////////// click on list


    private void activateClickOnList(ListView wagersList, ListView commentsList) {
        wagersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String wagerAuthor = prediction.getWagerAuthorAt(position);
                if (wagerAuthor != null) {
                    Intent intent = new Intent(ViewGenericPrediction.this, GPVActivity.class);
                    intent.putExtra("url", wagerAuthor);
                    startActivity(intent);
                }
            }
        });
        commentsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String wagerAuthor = prediction.getCommentAuthorAt(position);
                if (wagerAuthor != null) {
                    Intent intent = new Intent(ViewGenericPrediction.this, GPVActivity.class);
                    intent.putExtra("url", wagerAuthor);
                    startActivity(intent);
                }
            }
        });
    }

    private void showUI(){
        (findViewById(R.id.progressBar1)).setVisibility(View.GONE);
        (findViewById(R.id.the_entire_layout)).setVisibility(View.VISIBLE);
    }
}
