package com.grasscove.namethat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SettingsList extends ListActivity {

	public static final String[] COUNTDOWN_MINUTES = {
		"1 minute", "2 minutes",
		"3 minutes", "4 minutes"
	};
	
	private static final int DIALOG_COUNTDOWN_TIME = 1;

	private SharedPreferences mSettings;
	private SharedPreferences.Editor mSettingsEdit;
	private TextView mSettingsCountdownText;
	private CheckBox mCheckbox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings);

		mSettings = getSharedPreferences("settings", MODE_PRIVATE);
		mSettingsEdit = mSettings.edit();

		setListAdapter(new SettingsListAdapter(this, mSettings));
		registerForContextMenu(getListView());
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
			case 0:
				showDialog(DIALOG_COUNTDOWN_TIME);
				break;
			case 1:
				mCheckbox = (CheckBox) v.findViewById(R.id.settings_row_checkbox);
				mCheckbox.toggle();
				mSettingsEdit.putBoolean("start_immediatly", mCheckbox.isChecked()).commit();
				break;
			case 2:
				mCheckbox = (CheckBox) v.findViewById(R.id.settings_row_checkbox);
				mCheckbox.toggle();
				mSettingsEdit.putBoolean("startup_window", mCheckbox.isChecked()).commit();
				break;
			case 3:
				mCheckbox = (CheckBox) v.findViewById(R.id.settings_row_checkbox);
				mCheckbox.toggle();
				mSettingsEdit.putBoolean("auto_team_switch", mCheckbox.isChecked()).commit();
				break;
			default:
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;

		switch (item.getItemId()) {
			case R.id.menu_options_play:
				intent = new Intent(this, CategoriesList.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;
			case R.id.menu_options_rules:
				intent = new Intent(this, Rules.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;
			case R.id.menu_options_settings:
				intent = new Intent(this, SettingsList.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;
			default:
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);

		switch (id) {
		case DIALOG_COUNTDOWN_TIME:
			mBuilder
			    .setCancelable(true)
			    .setTitle(getResources().getString(R.string.select_countdown_timer))
				.setItems(COUNTDOWN_MINUTES, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						mSettings.edit().putInt("countdown_time", item).commit();

						if (mSettingsCountdownText == null) {
							mSettingsCountdownText = (TextView) findViewById(R.id.settings_countdown_text);
						}

						mSettingsCountdownText.setText(COUNTDOWN_MINUTES[item]);
					}
				}).setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
					}
				});
			return mBuilder.create();
		default:
			return null;
		}
	}
	
	public class SettingsListAdapter extends BaseAdapter {
	    private Context context;
	    private SharedPreferences settings;
	    private TextView countdownTextView;

	    public SettingsListAdapter(Context context, SharedPreferences settings) {
	        this.context = context;
	        this.settings = settings;
	    }

	    @Override
	    public int getCount() {
	        return 1; //return 1 here to only display the first row
	    }

	    @Override
	    public Object getItem(int position) {
	        return null;
	    }

	    @Override
	    public long getItemId(int position) {
	        return 0;
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        LinearLayout row;

	        if (convertView == null) {
	            row = new LinearLayout(context);

	            LayoutInflater vi;
	            vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	            if (position == 0) {
	                vi.inflate(R.layout.settings_countdown_row, row, true);
	            } else {
	                vi.inflate(R.layout.settings_row, row, true);
	            }
	        } else {
	            row = (LinearLayout) convertView;
	        }

	        switch (position) {
	            case 0:
	                countdownTextView = (TextView) row.findViewById(R.id.settings_countdown_text);
	                countdownTextView.setText(SettingsList.COUNTDOWN_MINUTES[settings.getInt("countdown_time", 0)]);
	                break;
	            default:
	                break;
	        }

	        return row;
	    }
	}
}