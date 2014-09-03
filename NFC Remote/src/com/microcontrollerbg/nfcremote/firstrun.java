package com.microcontrollerbg.nfcremote;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Application about box activity. This class displays information about the
 * application to the user.
 * 
 * @author krdavis
 */
public class firstrun extends Activity {
	// Activity tag used by logging APIs

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Log.d(APP_TAG, ACT_TAG + " -> onCreate()");

		// Initialize the layout
		super.onCreate(savedInstanceState);

		setContentView(R.layout.firstrun);

		// Set the application version
		TextView text = (TextView) this.findViewById(R.id.app_version);
		text.setText("v1.0");
	}
}