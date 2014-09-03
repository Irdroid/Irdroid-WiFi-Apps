package com.microcontrollerbg.irmacro;


import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * activity to display all records from SQLite database
 * 
 * @author ketan(Visit my <a
 *         href="http://androidsolution4u.blogspot.in/">blog</a>)
 */
public class Macros extends Activity {

	private DbHelper mHelper;
	private SQLiteDatabase dataBase;

	private ArrayList<String> userId = new ArrayList<String>();
	private ArrayList<String> user_fName = new ArrayList<String>();
	
	private ArrayList<String> userId1 = new ArrayList<String>();
	private ArrayList<String> user_fName1 = new ArrayList<String>();
	private ArrayList<String> user_lName1 = new ArrayList<String>();
	final Context context = this;
	private ListView userList;
	String macro_short;

	TcpSocketChannel schannel;
	AlertDialog levelDialog;
	private AlertDialog.Builder build;
	LircClient client;
	SharedPreferences sharedPrefs;
	

	int timeout;
	String remote_command;
	String macro_id1;
	SharedPreferences mPrefs;
	String macro_id2;
	String remote_name;
	
	private static final int RESULT_SETTINGS = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Uri data = intent.getData();

		// String uri = null;

		// Bundle extras = getIntent().getExtras();

		setContentView(R.layout.macros);

		userList = (ListView) findViewById(R.id.List1);
		// client = null;

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		client = new LircClient(sharedPrefs.getString("prefUsername",
				"192.168.2.1"), Integer.parseInt(sharedPrefs.getString(
				"prefport", "8765")), true, 3000);
		timeout = Integer.parseInt(sharedPrefs.getString("timeout", "1000"));
		mHelper = new DbHelper(this);

		// this is the case other than first run

		if (data != null) {
			String domain = data.getHost();

			Toast.makeText(getApplicationContext(),
					"Executing Macro: " + domain, 3000).show();

			macro_id1 = domain;

			displayDataexec();

			execMacro();

		}

		// add new record
		findViewById(R.id.btnAddmacro).setOnClickListener(
				new OnClickListener() {

					public void onClick(View v) {

						Intent i = new Intent(getApplicationContext(),
								addmacro.class);
						i.putExtra("update", false);
						startActivity(i);

					}
				});

		// click to update data
		userList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				TextView text = (TextView) arg1.findViewById(R.id.txt_id1);

				macro_id1 = text.getText().toString();

				displayDataexec();

				execMacro();

				Toast.makeText(getApplicationContext(), "Starting macro..",
						3000).show();
				// Intent i = new Intent(getApplicationContext(),
				// DisplayActivity.class);

				// i.putExtra("macro_id", macro_id);
				// i.putExtra("Lname", user_lName.get(arg2));
				// i.putExtra("ID", userId.get(arg2));
				// i.putExtra("update", true);

				// startActivity(i);

				// remote_command = text.getText().toString();
				// TextView text1 = (TextView)
				// arg1.findViewById(R.id.txt_lName);
				// remote_name = text1.getText().toString();
				// SendIR();

			}
		});

		// long click to delete data
		userList.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {
				final CharSequence[] items = { " Edit Macro ",
						" Delete Macro ", " Shortcut " };
				TextView text = (TextView) arg1.findViewById(R.id.txt_id1);
				final String macro_id = text.getText().toString();
				build = new AlertDialog.Builder(Macros.this);
				build.setTitle("Select option:");
				// build.setMessage("Select action");

				build.setSingleChoiceItems(items, -1,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {

								switch (item) {
								case 0:

									Intent i = new Intent(
											getApplicationContext(),
											DisplayActivity.class);

									i.putExtra("macro_id", macro_id);
									startActivity(i);
									break;
								case 1:
									// Your code when 2nd option seletced
									Toast.makeText(getApplicationContext(),
											user_fName.get(arg2) + " "

											+ " is deleted.", 3000).show();
									dataBase.delete(
											DbHelper.TABLE_NAME,
											DbHelper.KEY_ID + "="
													+ userId.get(arg2), null);
									dataBase.delete(DbHelper.MACROS,
											DbHelper.KEY_ID_MACRO + "="
													+ userId.get(arg2), null);

									displayData();
									dialog.cancel();
									break;
								case 2:
									// Your code when 3rd option seletced
									macro_short = user_fName.get(arg2);

									macro_id2 = userId.get(arg2);

									ShortcutIcon();
									break;

								}
								levelDialog.dismiss();
							}
						});

				levelDialog = build.create();
				levelDialog.show();

				return true;
			}
		});

		if (getFirstRun()) {
			// This is first run
			setRunned();
			Intent c = new Intent(this, firstrun.class);
			startActivity(c);

			// your code for first run goes here

		} else {

		}
	}

	public boolean getFirstRun() {
		return sharedPrefs.getBoolean("firstRun", true);
	}

	public void setRunned() {
		SharedPreferences.Editor edit = sharedPrefs.edit();
		edit.putBoolean("firstRun", false);
		edit.commit();
	}

	@Override
	protected void onResume() {
		displayData();

		// execMacro();
		super.onResume();
	}

	/**
	 * displays data from SQLite
	 * 
	 * 
	 * 
	 */

//	private void SendIR() {

//		try {
	//		client.sendIr1Command(remote_name, remote_command, 1);
	//	} catch (Exception e) {
	//		// TODO Auto-generated catch block
	//		e.printStackTrace();
	//	}
//	}
//
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.more_tab_menu, menu);
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.Addmacro:

			Intent i = new Intent(getApplicationContext(), addmacro.class);
			i.putExtra("update", false);
			startActivity(i);
			break;
		case R.id.menu_settings:
			Intent a = new Intent(this, UserSettingActivity.class);
			startActivityForResult(a, RESULT_SETTINGS);
			break;
		case R.id.firstrun:
			Intent c = new Intent(this, firstrun.class);
			startActivity(c);
			break;
		case R.id.About:
			Intent b = new Intent(this, About.class);
			startActivity(b);
			break;

		}
		return true;
	}

	private void execMacro() {

		Thread thread = new Thread() {
			@Override
			public void run() {

				for (int i = 0; i < user_lName1.size(); i++) {

					try {
						client.sendIr1Command(user_lName1.get(i).toString(),
								user_fName1.get(i).toString(), 1);
						Thread.sleep(timeout);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					;

				}

			}
		};

		thread.start();

	}

	private void displayDataexec() {
		dataBase = mHelper.getWritableDatabase();
		Cursor mCursor = dataBase.rawQuery("SELECT * FROM "
				+ DbHelper.TABLE_NAME + " WHERE " + DbHelper.KEY_ID + "="
				+ macro_id1, null);
		userId1.clear();
		userId1.clear();
		user_fName1.clear();
		user_lName1.clear();
		if (mCursor.moveToFirst()) {
			do {
				userId1.add(mCursor.getString(mCursor
						.getColumnIndex(DbHelper.KEY_ID1)));
				userId1.add(mCursor.getString(mCursor
						.getColumnIndex(DbHelper.KEY_ID)));
				user_fName1.add(mCursor.getString(mCursor
						.getColumnIndex(DbHelper.KEY_FNAME)));
				user_lName1.add(mCursor.getString(mCursor
						.getColumnIndex(DbHelper.KEY_LNAME)));

			} while (mCursor.moveToNext());
		}

		// execMacro();
		mCursor.close();
	}

	protected void onNewIntent(Intent in) {
		super.onNewIntent(in);
		System.out.println(in.getStringExtra("hello"));

	}

	private void ShortcutIcon() {

		// Intent shortcutIntent = new Intent(getApplicationContext(),
		// Macros.class);
		// shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		// Intent addIntent = new Intent();
		// addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		// addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, user_fName);
		// addIntent.putExtra("start_macro", macro_short);
		// addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
		// Intent.ShortcutIconResource.fromContext(getApplicationContext(),
		// R.drawable.ic_launcher));
		// addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		// getApplicationContext().sendBroadcast(addIntent);

		// String urlStr =
		// String.format(context.getString(R.string.homescreen_shortcut_search_url),
		// context.getString(R.string.app_id));
		Intent shortcutIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("irdroid://" + macro_id2));

		// shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		// Sets the custom shortcut's title
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, macro_short);
		// Set the custom shortcut icon
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(context,
						R.drawable.macro));
		intent.putExtra("duplicate", false);

		// add the shortcut
		intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		context.sendBroadcast(intent);

	}

	private void displayData() {
		dataBase = mHelper.getWritableDatabase();
		Cursor mCursor = dataBase.rawQuery("SELECT * FROM " + DbHelper.MACROS,
				null);

		userId.clear();
		user_fName.clear();
		// user_lName.clear();
		if (mCursor.moveToFirst()) {
			do {
				userId.add(mCursor.getString(mCursor
						.getColumnIndex(DbHelper.KEY_ID_MACRO)));
				user_fName.add(mCursor.getString(mCursor
						.getColumnIndex(DbHelper.KEY_MACRONAME)));
				// user_lName.add(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_LNAME)));

			} while (mCursor.moveToNext());
		}
		MacrosAdapter disadpt = new MacrosAdapter(Macros.this, userId,
				user_fName);
		userList.setAdapter(disadpt);
		// execMacro();
		mCursor.close();
	}

}