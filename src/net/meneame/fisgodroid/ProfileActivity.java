package net.meneame.fisgodroid;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ProfileActivity extends Activity
{
    private TextView mErrorText;
    private TextView mUsername;
    private TextView mName;
    private TextView mBio;
    private ImageView mAvatar;
    private ImageView mFriendship;
    private ProgressBar mLoadingProgress;
    private ViewGroup mContents;
    private AsyncTask<?, ?, ?> mTask;
    private AsyncTask<?, ?, ?> mFriendshipTask;
    private FisgoService.FisgoBinder mService;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_profile);

        mErrorText = (TextView) findViewById(R.id.error_message);
        mLoadingProgress = (ProgressBar) findViewById(R.id.loading_progress);
        mContents = (ViewGroup) findViewById(R.id.contents);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mFriendship = (ImageView) findViewById(R.id.friends);
        mUsername = (TextView) findViewById(R.id.username);
        mName = (TextView) findViewById(R.id.name);
        mBio = (TextView) findViewById(R.id.bio);

        mFriendship.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String userid = getIntent().getStringExtra("userid");
                if ( userid != null )
                {
                    swapFriendship(userid);
                }
            }
        });

        if ( getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE )
        {
            ((LinearLayout) mContents).setOrientation(LinearLayout.HORIZONTAL);
            mContents.requestLayout();
        }

        bindService(new Intent(this, FisgoService.class), mServiceConn, 0);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if ( mTask != null )
        {
            mTask.cancel(true);
            mTask = null;
        }

        if ( mFriendshipTask != null )
        {
            mFriendshipTask.cancel(true);
            mFriendshipTask = null;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        unbindService(mServiceConn);
    }

    private void fetchUserInfo(String userid)
    {
        mTask = new GetUserProfileTask().execute(userid);
    }

    private void swapFriendship(String userid)
    {
        if ( mService != null && mFriendshipTask == null )
        {
            mFriendshipTask = new SwapFriendshipTask().execute(userid);
        }
    }

    private void populateProfile(UserProfile profile)
    {
        if ( profile.getAvatarUrl() != null )
        {
            Picasso.with(ProfileActivity.this).load(profile.getAvatarUrl()).into(mAvatar);
        }

        mUsername.setText(profile.getUsername());

        if ( profile.getName() != null )
        {
            mName.setText(profile.getName());
        }

        if ( profile.getBio() != null )
        {
            mBio.setText(profile.getBio());
        }

        setFriendshipIcon(profile.getFriendship());
    }

    private void setFriendshipIcon(FriendshipStatus status)
    {
        int resourceId = -1;
        int visibility = View.VISIBLE;

        switch (status)
        {
        default:
        case UNKNOWN:
            visibility = View.INVISIBLE;
            break;

        case NONE:
            resourceId = R.drawable.ic_friend_no;
            break;

        case FRIENDS:
            resourceId = R.drawable.ic_friend_yes;
            break;

        case FRIEND_ME:
            resourceId = R.drawable.ic_friend_me;
            break;

        case FRIEND_THEY:
            resourceId = R.drawable.ic_friend_they;
            break;

        case IGNORED:
            resourceId = R.drawable.ic_friend_ignored;
            break;
        }

        mFriendship.setVisibility(visibility);
        if ( resourceId != -1 )
        {
            mFriendship.setImageResource(resourceId);
        }
    }

    private class GetUserProfileTask extends AsyncTask<String, Void, UserProfile>
    {
        @Override
        protected void onPreExecute()
        {
            mLoadingProgress.setVisibility(View.VISIBLE);
            mContents.setVisibility(View.GONE);
            mErrorText.setVisibility(View.GONE);
        }

        @Override
        protected UserProfile doInBackground(String... arg0)
        {
            String userid = arg0[0];
            if ( mService != null && mService.isLoggedIn() )
            {
                return UserProfileFetcher.fetch(mService, userid);
            }
            return null;
        }

        @Override
        protected void onPostExecute(UserProfile profile)
        {
            mLoadingProgress.setVisibility(View.GONE);
            if ( profile != null )
            {
                mContents.setVisibility(View.VISIBLE);
                populateProfile(profile);
            }
            else
            {
                mErrorText.setVisibility(View.VISIBLE);
            }
            mTask = null;
        }
    }

    private class SwapFriendshipTask extends AsyncTask<String, Void, FriendshipStatus>
    {
        @Override
        protected FriendshipStatus doInBackground(String... args)
        {
            String userid = args[0];
            return mService.swapFriendship(userid);
        }

        @Override
        protected void onPostExecute(FriendshipStatus status)
        {
            setFriendshipIcon(status);
            mFriendshipTask = null;
        }
    }

    private ServiceConnection mServiceConn = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binder)
        {
            mService = (FisgoService.FisgoBinder) binder;
            if ( mService != null && mService.isLoggedIn() == false )
            {
                finish();
            }
            else
            {
                String userid = getIntent().getStringExtra("userid");
                if ( userid != null )
                {
                    fetchUserInfo(userid);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            mService = null;
        }
    };
}
