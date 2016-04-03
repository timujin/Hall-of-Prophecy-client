package com.example.artemsinyakov.hallofprophecy.Activities;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;

class ExtendedTwitterAPIClient extends TwitterApiClient {
    public ExtendedTwitterAPIClient(TwitterSession session) {
        super(session);
    }

    public UsersService getUsersService() {
        return getService(UsersService.class);
    }
}

