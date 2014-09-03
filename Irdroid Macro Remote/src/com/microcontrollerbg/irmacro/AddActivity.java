package com.microcontrollerbg.irmacro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * activity to get input from user and insert into SQLite database
 * 
 * @author ketan(Visit my <a
 *         href="http://androidsolution4u.blogspot.in/">blog</a>)
 */
public class AddActivity extends Activity implements OnClickListener {
	private Button btn_save;
	private EditText edit_first, edit_last;
	private DbHelper mHelper;
	private SQLiteDatabase dataBase;
	private String id, fname, lname;
	private boolean isUpdate;
	private String macro_id2;
	private String macro_id;
	final Context context = this;
	ArrayAdapter adapter = null;
	ArrayAdapter commandi = null;
	LircClient client = null;
	Spinner states;
	Spinner commands;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_activity);

		btn_save = (Button) findViewById(R.id.save_btn);
		// edit_first=(EditText)findViewById(R.id.frst_editTxt);
		// edit_last=(EditText)findViewById(R.id.last_editTxt);
		states = (Spinner) findViewById(R.id.bike_states);
		commands = (Spinner) findViewById(R.id.commands);
		macro_id = getIntent().getExtras().getString("macro_id2");

		Toast.makeText(context, "Add command to :" + macro_id, 3000).show();
		states.setAdapter(adapter);

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

		try {
			adapter = new ArrayAdapter(this,
					android.R.layout.simple_spinner_item, client.getRemotes()
							.toArray());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		states.setAdapter(adapter);

		states.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {

				try {
					commandi = new ArrayAdapter(context,
							android.R.layout.simple_spinner_item,
							client.getCommands(
									states.getSelectedItem().toString())
									.toArray());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				commands.setAdapter(commandi);

				// saveData();
				// alertDialog.dismiss();
				// saveData();
				// gdevice = spinDevice.getSelectedItem().toString();

			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		// macro_id = getIntent().getExtras().getString(macro_id);

		isUpdate = getIntent().getExtras().getBoolean("update");
		if (isUpdate) {
			id = getIntent().getExtras().getString("ID");
			fname = getIntent().getExtras().getString("Fname");
			lname = getIntent().getExtras().getString("Lname");
			edit_first.setText(fname);
			edit_last.setText(lname);

		}

		btn_save.setOnClickListener(this);

		mHelper = new DbHelper(this);

	}

	// saveButton click event
	public void onClick(View v) {
		fname = commands.getSelectedItem().toString().trim();
		lname = states.getSelectedItem().toString().trim();
		if (fname.length() > 0 && lname.length() > 0) {
			saveData();
		} else {
			AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
					AddActivity.this);
			alertBuilder.setTitle("Invalid Data");
			alertBuilder.setMessage("Please, Enter valid data");
			alertBuilder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();

						}
					});
			alertBuilder.create().show();
		}

	}

	/**
	 * save data into SQLite
	 */
	private void saveData() {
		dataBase = mHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		Toast.makeText(context, "Data:" + macro_id, 3000).show();
		values.put(DbHelper.KEY_ID, macro_id);
		values.put(DbHelper.KEY_FNAME, fname);
		values.put(DbHelper.KEY_LNAME, lname);

		System.out.println("");
		if (isUpdate) {
			// update database with new data
			dataBase.update(DbHelper.TABLE_NAME, values, DbHelper.KEY_ID + "="
					+ id, null);
		} else {
			// insert data into database
			dataBase.insert(DbHelper.TABLE_NAME, null, values);
		}
		// close database
		dataBase.close();
		finish();

	}

}
