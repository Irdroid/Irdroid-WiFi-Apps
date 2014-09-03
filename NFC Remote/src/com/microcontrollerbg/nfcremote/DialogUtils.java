package com.microcontrollerbg.nfcremote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public abstract class DialogUtils {

	public static void displayErrorDialog(Context context, int title,
			int message) {
		displayErrorDialog(context, context.getString(title),
				context.getString(message));
	}

	public static void displayErrorDialog(Context context, String title,
			String message) {
		new AlertDialog.Builder(context)
				.setTitle(title)
				.setMessage(message)
				.setIcon(R.drawable.error_icon)
				.setPositiveButton(R.string.okay,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	public static void displayInfoDialog(Context context, int title, int message) {
		displayInfoDialog(context, context.getString(title),
				context.getString(message));
	}

	public static void displayInfoDialog(Context context, String title,
			String message) {
		new AlertDialog.Builder(context)
				.setTitle(title)
				.setMessage(message)
				.setIcon(R.drawable.about_icon)
				.setPositiveButton(R.string.okay,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	public static void displayAboutDialog(Context context, int dialogType) {
		createAboutDialog(context, dialogType).show();
	}

	public static AlertDialog createAboutDialog(Context context, int dialogType) {
		// Create the given about dialog type
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		View aboutLayout = LayoutInflater.from(context).inflate(
				R.layout.about_dialog, null);
		TextView aboutText = (TextView) aboutLayout
				.findViewById(R.id.aboutText);

		dialog.setView(aboutLayout);
		dialog.setIcon(R.drawable.about_icon);
		// ((ImageView)aboutLayout.findViewById(R.id.aboutLogo)).setImageDrawable(context.getResources().getDrawable(R.drawable.full_logo));

		dialog.setPositiveButton(R.string.close,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});

		// Determine what dialog type is to be shown
		switch (dialogType) {

		default:
			return null;
		}

		// Make links out all URL's and email's in the dialog
		// Except the release notes. The linker recognizes
		// 4 digit version numbers as IP addresses

	}
}