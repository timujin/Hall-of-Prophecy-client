package com.example.artemsinyakov.hallofprophecy.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import io.fabric.sdk.android.Fabric;

import com.example.artemsinyakov.hallofprophecy.R;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;


public class MainActivity extends AppCompatActivity {

    private TwitterLoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(getString(R.string.consumer_key), getString(R.string.consumer_secret));
        Fabric.with(this, new Twitter(authConfig));

        setContentView(R.layout.activity_main);

        setUpButtons();

    }

    private void setUpButtons() {

        final Activity context = this;

        findViewById(R.id.add_twitter_prediction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateTwitterPrediction.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CookieSyncManager.createInstance(context);
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeSessionCookie();
                Twitter.getSessionManager().clearActiveSession();
                Twitter.logOut();
                Intent intent = new Intent(MainActivity.this, TwitterLogin.class);
                startActivity(intent);
                finish();
            }
        });


        findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserProfile.class);
                intent.putExtra("url", Twitter.getSessionManager().getActiveSession().getUserName());
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        TwitterSession twitterSession = Twitter.getSessionManager().getActiveSession();
        if (twitterSession == null) {
            Intent login = new Intent(MainActivity.this, TwitterLogin.class);
            startActivity(login);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Make sure that the loginButton hears the result from any
        // Activity that it triggered.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

}
