package net.meneame.fisgodroid;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
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
    private ProgressBar mLoadingProgress;
    private ViewGroup mContents;
    private AsyncTask<?, ?, ?> mTask;

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
        mUsername = (TextView) findViewById(R.id.username);
        mName = (TextView) findViewById(R.id.name);
        mBio = (TextView) findViewById(R.id.bio);

        if ( getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE )
        {
            ((LinearLayout) mContents).setOrientation(LinearLayout.HORIZONTAL);
            mContents.requestLayout();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        String userid = getIntent().getExtras().getString("userid");
        mTask = new GetUserProfileTask().execute(userid);
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
            return UserProfileFetcher.fetch(userid);
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
        }
    }
}
