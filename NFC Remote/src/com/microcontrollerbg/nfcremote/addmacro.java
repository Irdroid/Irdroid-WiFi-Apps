package com.microcontrollerbg.nfcremote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * activity to get input from user and insert into SQLite database
 * 
 * @author ketan(Visit my <a
 *         href="http://androidsolution4u.blogspot.in/">blog</a>)
 */
public class addmacro extends Activity implements OnClickListener {
	private Button btn_save;
	private EditText edit_first, edit_last;
	private DbHelper mHelper;
	private SQLiteDatabase dataBase;
	private String id, fname, lname;
	private boolean isUpdate;
	final Context context = this;
	ArrayAdapter adapter = null;
	ArrayAdapter commandi = null;
	LircClient client = null;
	Spinner states;
	Spinner commands;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addmacro);

		btn_save = (Button) findViewById(R.id.savemacro_btn);
		edit_first = (EditText) findViewById(R.id.editText1);
		// edit_last=(EditText)findViewById(R.id.last_editTxt);

		// states.setAdapter(adapter);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		// client = new LircClient("192.168.2.1", 8765, true, 3000);

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
		fname = edit_first.getText().toString();
		// lname= states.getSelectedItem().toString().trim();
		if (fname.length() > 0) {
			saveData();
		} else {
			AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
					addmacro.this);
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

		values.put(DbHelper.KEY_MACRONAME, fname);
		// values.put(DbHelper.KEY_LNAME,lname );

		System.out.println("");
		if (isUpdate) {
			// update database with new data
			dataBase.update(DbHelper.TABLE_NAME, values, DbHelper.KEY_ID + "="
					+ id, null);
		} else {
			// insert data into database
			dataBase.insert(DbHelper.MACROS, null, values);
		}
		// close database
		dataBase.close();
		finish();

	}

}