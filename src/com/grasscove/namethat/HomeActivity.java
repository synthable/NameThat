package com.grasscove.namethat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.grasscove.namethat.provider.NameThatDbHelper;

public class HomeActivity extends Activity implements View.OnClickListener {

	public static final int DIALOG_IMPORTING_WORDLIST = 0;

	private SharedPreferences mSettings;

	/**
	 * Display a loading dialog and start a new thread that reads the raw
	 * file contents of the SQLite3 database and writes it to the app's
	 * data directory. This effectively installs the database without
	 * having to execute each INSERT manually.
	 * After we are done set the "worldist_installed" shared preference to true so the
	 * application can skip this step next time it starts up.
	 */
	class WordlistInstall extends AsyncTask<Void, String, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			showDialog(DIALOG_IMPORTING_WORDLIST);
		}

		@Override
		protected Void doInBackground(Void... unused) {

			try {

				String mDbDir = "/data/data/com.grasscove.namethat/databases/";
				new File(mDbDir).mkdirs();

				OutputStream mOutputStream = new FileOutputStream(mDbDir + NameThatDbHelper.DATABASE_NAME);
				InputStream mInputStream = getResources().openRawResource(R.raw.namethat);

				byte[] buffer = new byte[1028];
				while ( mInputStream.read(buffer) > 0 ) {
					mOutputStream.write(buffer);
				}
				mOutputStream.close();
				mInputStream.close();

	            /**
                 * TODO: Actually do something with this exception.
                 */
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			mSettings.edit().putBoolean("wordlist_installed", true).commit();
			removeDialog(DIALOG_IMPORTING_WORDLIST);
		}
	}

	/**
	 * Set the home screen view and check if the application has installed
	 * the word list.  If not, start the WordlistInstall install thread.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mSettings = getSharedPreferences("settings", MODE_PRIVATE);
		boolean mInstalled = mSettings.getBoolean("wordlist_installed", false);

		if (mInstalled == false) {
			new WordlistInstall().execute();
		}
	}

	/**
	 * Create and display the loading dialog when the word list is being installed for the first time.
	 * This is only run once from the WordlistInstall thread to give a visual indication to the user
	 * the application is doing something.  This dialog cannot be canceled so the install process cannot
	 * be interrupted by the user.
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_IMPORTING_WORDLIST:
				ProgressDialog mProgressDialog;
				mProgressDialog = new ProgressDialog(this);
				mProgressDialog.setMessage(getResources().getString(R.string.importing_wordlist));
				mProgressDialog.setCancelable(false);
				mProgressDialog.show();
				return mProgressDialog;
			default:
				return null;
		}
	}

	@Override
    public void onClick(View target) {
        Intent mIntent;

        switch(target.getId()) {
            case R.id.new_game_btn:
                mIntent = new Intent(this, CategoriesList.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mIntent);
                break;
            case R.id.settings_btn:
                mIntent = new Intent(this, SettingsList.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mIntent);
                break;
            case R.id.rules_btn:
                mIntent = new Intent(this, Rules.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mIntent);
                break;
            default:
                break;
        }
    }
}