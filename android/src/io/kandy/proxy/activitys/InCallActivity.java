package io.kandy.proxy.activitys;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class InCallActivity extends Activity {
	
	public InCallActivity() {
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		getActionBar().hide();
	}
}
