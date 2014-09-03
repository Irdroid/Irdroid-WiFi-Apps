package com.microcontrollerbg.nfcremote;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
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
public class DisplayActivity extends Activity {

	private DbHelper mHelper;
	private SQLiteDatabase dataBase;
	private String macro_id1;
	private ArrayList<String> userId = new ArrayList<String>();
	private ArrayList<String> userId1 = new ArrayList<String>();
	private ArrayList<String> user_fName = new ArrayList<String>();
	private ArrayList<String> user_lName = new ArrayList<String>();
	final Context context = this;
	private ListView userList;

	TcpSocketChannel schannel;

	private AlertDialog.Builder build;
	LircClient client;

	String remote_command;
	int timeout;
	String remote_name;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_activity);

		userList = (ListView) findViewById(R.id.List);
		// client = null;

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		client = new LircClient(sharedPrefs.getString("prefUsername",
				"192.168.2.1"), Integer.parseInt(sharedPrefs.getString(
				"prefport", "8765")), true, 3000);
		timeout = Integer.parseInt(sharedPrefs.getString("timeout", "1000"));
		mHelper = new DbHelper(this);
		macro_id1 = getIntent().getExtras().getString("macro_id");
		Toast.makeText(context, "Edit Macro number:" + macro_id1, 3000).show();

		// add new record
		findViewById(R.id.btnAdd).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				Intent i = new Intent(getApplicationContext(),
						AddActivity.class);
				i.putExtra("macro_id2", macro_id1);

				startActivity(i);

			}
		});

		// click to update data
		userList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				// Intent i = new Intent(getApplicationContext(),
				// AddActivity.class);
				// i.putExtra("Fname", user_fName.get(arg2));
				// i.putExtra("Lname", user_lName.get(arg2));
				// i.putExtra("ID", userId.get(arg2));
				// i.putExtra("update", true);
				// startActivity(i);
				TextView text = (TextView) arg1.findViewById(R.id.txt_fName);
				remote_command = text.getText().toString();
				TextView text1 = (TextView) arg1.findViewById(R.id.txt_lName);
				remote_name = text1.getText().toString();
				SendIR();

			}
		});

		// long click to delete data
		userList.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {

				build = new AlertDialog.Builder(DisplayActivity.this);
				build.setTitle("Delete " + user_fName.get(arg2) + " "
						+ user_lName.get(arg2));
				build.setMessage("Do you want to delete ?");
				build.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {

								Toast.makeText(
										getApplicationContext(),
										user_fName.get(arg2) + " "
												+ user_lName.get(arg2)
												+ " is deleted.", 3000).show();

								dataBase.delete(
										DbHelper.TABLE_NAME,
										DbHelper.KEY_ID1 + "="
												+ userId1.get(arg2), null);
								displayData();
								dialog.cancel();
							}
						});

				build.setNegativeButton("No",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						});
				AlertDialog alert = build.create();
				alert.show();

				return true;
			}
		});

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

	private void SendIR() {

		try {
			client.sendIr1Command(remote_name, remote_command, 1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// public boolean onCreateOptionsMenu(Menu menu) {
	// MenuInflater inflater=getMenuInflater();
	// inflater.inflate(R.menu.more_tab_menu, menu);
	// return super.onCreateOptionsMenu(menu);

	// }
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch(item.getItemId())
	// {
	// case R.id.feeds:
	// execMacro();
	// break;

	// }
	// return true;
	// }

	// private void execMacro() {

	// Thread thread = new Thread() {
	// @Override
	// public void run() {

	// for (int i = 0; i < user_lName.size(); i++) {

	// try {
	// client.sendIr1Command(user_lName.get(i).toString(),
	// user_fName.get(i).toString(), 1);
	// Thread.sleep(timeout);
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// ;
	//
	// }

	// }
	// };
	//
	// thread.start();

	// }
	//
	private void displayData() {
		dataBase = mHelper.getWritableDatabase();
		Cursor mCursor = dataBase.rawQuery("SELECT * FROM "
				+ DbHelper.TABLE_NAME + " WHERE " + DbHelper.KEY_ID + "="
				+ macro_id1, null);
		userId1.clear();
		userId.clear();
		user_fName.clear();
		user_lName.clear();
		if (mCursor.moveToFirst()) {
			do {
				userId1.add(mCursor.getString(mCursor
						.getColumnIndex(DbHelper.KEY_ID1)));
				userId.add(mCursor.getString(mCursor
						.getColumnIndex(DbHelper.KEY_ID)));
				user_fName.add(mCursor.getString(mCursor
						.getColumnIndex(DbHelper.KEY_FNAME)));
				user_lName.add(mCursor.getString(mCursor
						.getColumnIndex(DbHelper.KEY_LNAME)));

			} while (mCursor.moveToNext());
		}
		DisplayAdapter disadpt = new DisplayAdapter(DisplayActivity.this,
				userId, user_fName, user_lName);
		userList.setAdapter(disadpt);
		// execMacro();
		mCursor.close();
	}

}
