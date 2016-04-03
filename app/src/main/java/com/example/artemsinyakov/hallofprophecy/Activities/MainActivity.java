package com.example.artemsinyakov.hallofprophecy.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.net.Uri;

import cz.msebera.android.httpclient.Header;
import io.fabric.sdk.android.Fabric;

import com.example.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.example.artemsinyakov.hallofprophecy.R;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.tweetui.TweetUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

public class MainActivity extends AppCompatActivity {

    private TwitterLoginButton loginButton;
    private ImageView banner;
    private ImageView avatar;

    static public int screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(getString(R.string.consumer_key), getString(R.string.consumer_secret));
        Fabric.with(this, new Twitter(authConfig));

        setContentView(R.layout.activity_main);
        setUpButtons();

        HoPRequestHelper.setUp();
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
        } else {
            loadUsersBannerAvatar();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Make sure that the loginButton hears the result from any
        // Activity that it triggered.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    protected void loadUsersBannerAvatar() {
        banner = (ImageView) findViewById(R.id.banner);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;

        avatar = (ImageView) findViewById(R.id.avatar);
        RelativeLayout.LayoutParams avatarLayout = (RelativeLayout.LayoutParams) avatar.getLayoutParams();
        avatarLayout.width = screenWidth / 5;
        avatarLayout.height = screenWidth / 5;
        avatarLayout.topMargin = -1 * screenWidth / 5;
        avatar.setLayoutParams(avatarLayout);

        TwitterSession session = Twitter.getInstance().core.getSessionManager().getActiveSession();
        new ExtendedTwitterAPIClient(session).getUsersService().show(Twitter.getSessionManager().getActiveSession().getUserId(), null, false,
                new Callback<User>() {
                    @Override
                    public void success(Result<User> result) {
                        if (result.data.profileBannerUrl == null) {
                            // default banner
                        } else {
                            new DownloadImageTask(banner).execute(result.data.profileBannerUrl);
                            new DownloadAvatarTask(avatar).execute(result.data.profileImageUrl);
                        }
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        Log.d("twittercommunity", "exception is " + exception);
                    }
                });
    }
}
