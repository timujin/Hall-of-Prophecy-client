package com.example.artemsinyakov.hallofprophecy.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import com.example.artemsinyakov.hallofprophecy.GCMServices.RegistrationIntentService;
import com.example.artemsinyakov.hallofprophecy.GenericProfileView.GPVActivity;
import com.example.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.example.artemsinyakov.hallofprophecy.R;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import io.fabric.sdk.android.Fabric;

public class TwitterLogin extends AppCompatActivity {

    private TwitterLoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(getString(R.string.consumer_key), getString(R.string.consumer_secret));
        Fabric.with(this, new Twitter(authConfig));

        setContentView(R.layout.activity_twitter_login);

        final Activity activity = this;
        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setClickable(false);
                loginButton.setVisibility(View.INVISIBLE);
            }
        });
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // The TwitterSession is also available through:
                // Twitter.getInstance().core.getSessionManager().getActiveSession()
                TwitterSession session = result.data;
                TwitterAuthToken token = session.getAuthToken();
                String user_id = token.token.split("-")[0];
                JSONObject json = new JSONObject();
                try {
                    json.put("key", token.token);
                    json.put("secret", token.secret);
                    json.put("user_id", user_id);
                } catch (JSONException e) {
                    authFailure();
                }
                HoPRequestHelper.post(activity, "/register", json, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        authSuccess();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        authFailure();
                    }
                });
            }

            @Override
            public void failure(TwitterException exception) {
                authFailure();
            }
        });

        HoPRequestHelper.setUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Make sure that the loginButton hears the result from any
        // Activity that it triggered.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    public void authSuccess() {
        ///////////////
        Intent registerService = new Intent(TwitterLogin.this, RegistrationIntentService.class);
        startService(registerService);
        //////////////
        Intent main = new Intent(TwitterLogin.this, MainActivity.class);
        startActivity(main);
        finish();
    }

    public void authFailure() {
        Toast.makeText(this, "Login unsuccessful", Toast.LENGTH_LONG).show();
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();
        Twitter.getSessionManager().clearActiveSession();
        Twitter.logOut();
        loginButton.setVisibility(View.VISIBLE);
        loginButton.setClickable(true);
    }


}
