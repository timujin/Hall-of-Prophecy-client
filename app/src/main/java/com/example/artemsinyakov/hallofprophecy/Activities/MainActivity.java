package com.example.artemsinyakov.hallofprophecy.Activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
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

import com.example.artemsinyakov.hallofprophecy.GenericPredictionVIew.ViewGenericPrediction;
import com.example.artemsinyakov.hallofprophecy.GenericProfileView.GPVActivity;
import com.example.artemsinyakov.hallofprophecy.HoPRequestHelper;
import com.example.artemsinyakov.hallofprophecy.R;
//import com.example.artemsinyakov.hallofprophecy.SeriesOfPopups.SeriesOfPopups;
import com.example.artemsinyakov.hallofprophecy.SeriesOfPopups.PickAPredictionDialog;
import com.example.artemsinyakov.hallofprophecy.SeriesOfPopups.SeriesOfPopups;
import com.example.artemsinyakov.hallofprophecy.SeriesOfPopups.SoPPActivity;
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

        findViewById(R.id.add_prediction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MainActivity.this, CreateTwitterPrediction.class);
                //startActivity(intent);
                PickAPredictionDialog.showPredictionDialog(context);
            }
        });

        /*findViewById(R.id.add_yahoo_finance_prediction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/

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
                Intent intent = new Intent(MainActivity.this, GPVActivity.class);
                intent.putExtra("url", Twitter.getSessionManager().getActiveSession().getUserName());
                //intent.putExtra("api", "/user/withwagers/");
                startActivity(intent);
            }
        });

        /*findViewById(R.id.upcoming).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(MainActivity.this, UserProfile.class);
                intent.putExtra("url", Twitter.getSessionManager().getActiveSession().getUserName());
                intent.putExtra("api", "/user/onlyundecided/");
                startActivity(intent);
                Intent intent = new Intent(MainActivity.this, SoPPActivity.class);
                startActivity(intent);
            }
        });*/

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
        /*Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;

        banner = (ImageView) findViewById(R.id.banner);

        Bitmap def = BitmapFactory.decodeResource(getResources(),
                R.drawable.default_banner);

        int imageWidth = def.getWidth();
        int imageHeight = def.getHeight();

        int newWidth = MainActivity.screenWidth; //this method should return the width of device screen.
        float scaleFactor = (float)newWidth/(float)imageWidth;
        int newHeight = (int)(imageHeight * scaleFactor);

        Bitmap result = Bitmap.createScaledBitmap(def, newWidth, newHeight, true);
        banner.setImageBitmap(result);



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
                            new DownloadAvatarTask(avatar).execute(result.data.profileImageUrl);
                        } else {
                            new DownloadImageTask(banner).execute(result.data.profileBannerUrl);
                            new DownloadAvatarTask(avatar).execute(result.data.profileImageUrl);
                        }
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        Log.d("twittercommunity", "exception is " + exception);
                    }
                });*/
    }
}
