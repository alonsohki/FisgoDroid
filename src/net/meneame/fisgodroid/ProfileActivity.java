package net.meneame.fisgodroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ProfileActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_profile);
		
		TextView profile = (TextView) findViewById(R.id.userprofile);
		
		profile.setText(savedInstanceState.getString("username"));
	}
}
