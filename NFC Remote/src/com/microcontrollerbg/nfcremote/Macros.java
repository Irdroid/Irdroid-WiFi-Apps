package com.microcontrollerbg.nfcremote;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
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
	static PendingIntent pi;
	private ArrayList<String> userId1 = new ArrayList<String>();
	private ArrayList<String> user_fName1 = new ArrayList<String>();
	private ArrayList<String> user_lName1 = new ArrayList<String>();
	final Context context = this;
	private ListView userList;
	String macro_short;
	List<ScanResult> mScanResults;
	TcpSocketChannel schannel;
	AlertDialog levelDialog;
	private AlertDialog.Builder build;
	LircClient client;
	SharedPreferences sharedPrefs;
	WifiManager mWifiManager;
	String nfcData;
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
		if (intent.getType() != null
				&& intent.getType().equals("application/" + getPackageName())) {
			// Read the first record which contains the relay info
			Parcelable[] rawMsgs = intent
					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			NdefRecord relayRecord = ((NdefMessage) rawMsgs[0]).getRecords()[0];
			nfcData = new String(relayRecord.getPayload());

		}

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
		if (sharedPrefs.getBoolean("wifi_connect", false)) {
			Connect_Irdroid();

		}
		// this is the case other than first run

		if (nfcData != null) {

			Toast.makeText(getApplicationContext(),
					"Executing Macro: " + nfcData, 3000).show();

			macro_id1 = nfcData;

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
						" Delete Macro ", " Create macro shortcut",
						" Write macro to NFC " };
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
								case 3:
									// Your code when 3rd option seletced
									macro_short = user_fName.get(arg2);

									macro_id2 = userId.get(arg2);

									// ShortcutIcon();
									String nfcMessage = macro_id2;

									// When an NFC tag comes into range, call
									// the main activity which handles writing
									// the data to the tag
									NfcAdapter nfcAdapter = NfcAdapter
											.getDefaultAdapter(context);

									Intent nfcIntent = new Intent(context,
											Macros.class)
											.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
									nfcIntent
											.putExtra("nfcMessage", nfcMessage);
									pi = PendingIntent.getActivity(context, 0,
											nfcIntent,
											PendingIntent.FLAG_UPDATE_CURRENT);
									IntentFilter tagDetected = new IntentFilter(
											NfcAdapter.ACTION_TAG_DISCOVERED);

									nfcAdapter.enableForegroundDispatch(
											(Activity) context, pi,
											new IntentFilter[] { tagDetected },
											null);

									DialogUtils
											.displayInfoDialog(context,
													"Write macro to NFC tag",
													"Tap your NFC tag in order to record your macro");
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

	// private void SendIR() {

	// try {
	// client.sendIr1Command(remote_name, remote_command, 1);
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
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
		Tag tag = in.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		String nfcMessage = in.getStringExtra("nfcMessage");

		if (nfcMessage != null) {
			writeTag(this, tag, nfcMessage);
		}

	}

	private void Connect_Irdroid() {

		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// mWifiManager.startScan();
		WifiConfiguration wc = new WifiConfiguration();
		wc.SSID = "\"Irdroid\"";
		wc.preSharedKey = "\"Irdroid1234\"";
		wc.status = WifiConfiguration.Status.ENABLED;
		wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

		int netId = mWifiManager.addNetwork(wc);
		mWifiManager.enableNetwork(netId, true);
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

	public static boolean writeTag(Context context, Tag tag, String data) {
		// Record to launch Play Store if app is not installed
		NdefRecord appRecord = NdefRecord.createApplicationRecord(context
				.getPackageName());

		// Record with actual data we care about
		NdefRecord relayRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				new String("application/" + context.getPackageName())
						.getBytes(Charset.forName("US-ASCII")), null,
				data.getBytes());

		// Complete NDEF message with both records
		NdefMessage message = new NdefMessage(new NdefRecord[] { relayRecord,
				appRecord });

		try {
			// If the tag is already formatted, just write the message to it
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();

				// Make sure the tag is writable
				if (!ndef.isWritable()) {
					DialogUtils.displayErrorDialog(context,
							R.string.nfcReadOnlyErrorTitle,
							R.string.nfcReadOnlyError);
					return false;
				}

				// Check if there's enough space on the tag for the message
				int size = message.toByteArray().length;
				if (ndef.getMaxSize() < size) {
					DialogUtils.displayErrorDialog(context,
							R.string.nfcBadSpaceErrorTitle,
							R.string.nfcBadSpaceError);
					return false;
				}

				try {
					// Write the data to the tag
					ndef.writeNdefMessage(message);

					DialogUtils.displayInfoDialog(context,
							R.string.nfcWrittenTitle, R.string.nfcWritten);
					pi.cancel();
					return true;
				} catch (TagLostException tle) {
					DialogUtils.displayErrorDialog(context,
							R.string.nfcTagLostErrorTitle,
							R.string.nfcTagLostError);
					return false;
				} catch (IOException ioe) {
					DialogUtils.displayErrorDialog(context,
							R.string.nfcFormattingErrorTitle,
							R.string.nfcFormattingError);
					return false;
				} catch (FormatException fe) {
					DialogUtils.displayErrorDialog(context,
							R.string.nfcFormattingErrorTitle,
							R.string.nfcFormattingError);
					return false;
				}
				// If the tag is not formatted, format it with the message
			} else {
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message);

						DialogUtils.displayInfoDialog(context,
								R.string.nfcWrittenTitle, R.string.nfcWritten);
						return true;
					} catch (TagLostException tle) {
						DialogUtils.displayErrorDialog(context,
								R.string.nfcTagLostErrorTitle,
								R.string.nfcTagLostError);
						return false;
					} catch (IOException ioe) {
						DialogUtils.displayErrorDialog(context,
								R.string.nfcFormattingErrorTitle,
								R.string.nfcFormattingError);
						return false;
					} catch (FormatException fe) {
						DialogUtils.displayErrorDialog(context,
								R.string.nfcFormattingErrorTitle,
								R.string.nfcFormattingError);
						return false;
					}
				} else {
					DialogUtils.displayErrorDialog(context,
							R.string.nfcNoNdefErrorTitle,
							R.string.nfcNoNdefError);
					return false;
				}
			}
		} catch (Exception e) {
			DialogUtils.displayErrorDialog(context,
					R.string.nfcUnknownErrorTitle, R.string.nfcUnknownError);
		}

		return false;
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