package com.grasscove.namethat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.grasscove.namethat.provider.NameThatContract.Categories;
import com.grasscove.namethat.provider.model.Word;

public class Play extends Activity implements View.OnClickListener {
	private static final int DIALOG_TIME_UP = 0;
	private static final int DIALOG_WINNER = 1;
	private static final int DIALOG_WORD_LIMIT = 2;

	private final SimpleDateFormat mFormat = new SimpleDateFormat("mm:ss");

	private Intent mIntent;
	private SharedPreferences mSettings;
	private MediaPlayer mMp;
	private CountDownTimer mTimer;
	private Word mWord;

	private TextView mCategoryView;
	private TextView mCategoryWordView;
	private TextView mTimerView;
	private TextView mTeam1ScoreView;
	private TextView mTeam2ScoreView;
	private TextView mTeam1TextView;
	private TextView mTeam2TextView;

	private int mCategoryId;
	private int mTeam1Score = 0;
	private int mTeam2Score = 0;
	private int mActiveTeam = 1;
	private int mMinutes = 0;

	private ArrayList<Integer> mUsedWords = new ArrayList<Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.play);

		mIntent = getIntent();
		mCategoryView = (TextView)findViewById(R.id.play_cat);
		mCategoryId = mIntent.getIntExtra(Categories.Columns._ID, 0);
		mCategoryView.setText(mIntent.getStringExtra(Categories.Columns.NAME));

		Log.v("categoryID", Integer.toString(mCategoryId));
		
		mMp = MediaPlayer.create(this, R.raw.tick);
		mMp.setLooping(true);
		mSettings = getSharedPreferences("settings", MODE_PRIVATE);

		mTeam1TextView = (TextView)findViewById(R.id.team1_text);
		mTeam2TextView = (TextView)findViewById(R.id.team2_text);
		mTeam1ScoreView = (TextView)findViewById(R.id.team1_score);
		mTeam2ScoreView = (TextView)findViewById(R.id.team2_score);
		mTimerView = (TextView)findViewById(R.id.play_timer);
		mCategoryWordView = (TextView)findViewById(R.id.play_word_bubble);

		if(mIntent.hasExtra("team1Score")) {
			mTeam1ScoreView.setText(mIntent.getStringExtra("team1Score"));
		}
		if(mIntent.hasExtra("team2Score")) {
			mTeam2ScoreView.setText(mIntent.getStringExtra("team2Score"));
		}

		mTeam1ScoreView.setTextColor(Color.WHITE);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mWord = getRandomWord();

        mCategoryWordView.setText(mWord.getName());

        mMinutes = mSettings.getInt("countdown_time", 0) + 1;
        mTimerView.setText(mMinutes +":00");

        if(mActiveTeam == 1) {
        	mActiveTeam = 2;
        	mTeam2ScoreView.setTextColor(Color.GREEN);
        	mTeam1ScoreView.setTextColor(Color.WHITE);

        	mTeam2TextView.setTextColor(Color.GREEN);
        	mTeam1TextView.setTextColor(Color.WHITE);
        } else {
        	mActiveTeam = 1;
        	mTeam1ScoreView.setTextColor(Color.GREEN);
        	mTeam2ScoreView.setTextColor(Color.WHITE);

        	mTeam1TextView.setTextColor(Color.GREEN);
        	mTeam2TextView.setTextColor(Color.WHITE);
        }

        if(mTimer == null) {
        	mTimer = new CountDownTimer(mMinutes * 60000, 1000) {
    	    	public void onTick(long millisUntilFinished) {
    	   	    	 if(millisUntilFinished <= 20000) {
    	   	    		 mMp.start();
    	   	    	 }
    	   	    	 mTimerView.setText(mFormat.format(millisUntilFinished));
    	   	     }

    	   	     public void onFinish() {
    	   	    	 mMp.stop();
    	   	    	 mTimerView.setText("done!");
    	   	    	 if(mActiveTeam == 2) {
    	   	        	 mTeam1ScoreView.setText(Integer.toString(++mTeam1Score));
    	   	        	 if(mTeam1Score == 7) {
    	   	        		 mTimer.cancel();
    	   	        		 showDialog(DIALOG_WINNER);
    	   	        	 } else {
    	   	        		showDialog(DIALOG_TIME_UP);
    	   	        	 }
    	   	         } else {
    	   	        	 mTeam2ScoreView.setText(Integer.toString(++mTeam2Score));
    	   	        	 if(mTeam2Score == 7) {
    	   	        		mTimer.cancel();
    	   	        		showDialog(DIALOG_WINNER);
	   	   	        	 } else {
	   	   	        		 showDialog(DIALOG_TIME_UP);
	   	   	        	 }
    	   	         }
    	   	     }
            };
    	   	mTimer.start();
        }
	}

	@Override
	protected void onPause() {
		super.onPause();

		mMp.pause();
	}

	@Override
	protected void onStop() {
		super.onStop();

		mMp.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mTimer.cancel();
	}

	private Word getRandomWord() {
	    Uri uri = Categories.URI.buildUpon()
	        .appendPath(Integer.toString(mCategoryId))
	        .appendPath("words")
	        .appendPath("random")
	        .build();
	    Cursor c = managedQuery(uri, null, null, null, null);
	    c.moveToFirst();
	    return new Word(c);
	}

	@Override
    public void onClick(View target) {
        switch(target.getId()) {
            case R.id.play_next_word:
                mWord = getRandomWord();

                /**
                 * Test if the word has been used before during this round.
                 */
                int id = mWord.getId();
                while(mUsedWords.contains(id)) {
                    if(mUsedWords.size() > 100) {
                        showDialog(DIALOG_WORD_LIMIT);
                        return;
                    }
                    mWord = getRandomWord();
                    id = mWord.getId();
                }
                mUsedWords.add(id);

                mCategoryWordView.setText(mWord.getName());
                if(mActiveTeam == 1) {
                    mActiveTeam = 2;
                    mTeam2ScoreView.setTextColor(Color.GREEN);
                    mTeam1ScoreView.setTextColor(Color.WHITE);

                    mTeam2TextView.setTextColor(Color.GREEN);
                    mTeam1TextView.setTextColor(Color.WHITE);
                } else {
                    mActiveTeam = 1;
                    mTeam1ScoreView.setTextColor(Color.GREEN);
                    mTeam2ScoreView.setTextColor(Color.WHITE);

                    mTeam1TextView.setTextColor(Color.GREEN);
                    mTeam2TextView.setTextColor(Color.WHITE);
                }
                break;
            default:
                break;
        }
    }

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String team = (mActiveTeam == 1) ? "Two" : "One";

    	switch(id) {
    		case DIALOG_TIME_UP:
    			team = (mActiveTeam == 1) ? "Two" : "One";
    			builder
    				.setCancelable(false)
					.setTitle(getResources().getString(R.string.times_up))
					.setMessage("Team "+ team +" gained a point!")
					.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							mTimerView.setText(mMinutes +":00");
							mTimer.start();
							dialog.dismiss();
						}
					});
			return builder.create();
    		case DIALOG_WINNER:
    			builder
	    			.setCancelable(false)
					.setTitle(getResources().getString(R.string.winner))
					.setMessage("Team "+ team +" won!  Do you want to play again?")
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							mTeam1Score = 0;
							mTeam2Score = 0;
							mTeam1ScoreView.setText("0");
							mTeam2ScoreView.setText("0");
	
							mTimerView.setText(mMinutes +":00");
							mTimer.start();
							mUsedWords = new ArrayList<Integer>();
							dialog.dismiss();
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							finish();
						}
					});
    			return builder.create();
    		case DIALOG_WORD_LIMIT:
    			mTimer.cancel();
    			mMp.stop();

    			builder
	    			.setCancelable(false)
					.setTitle(getResources().getString(R.string.limit_reached))
					.setMessage("You have reached the word limit for one round.  "+ team +" wins! \n\nDo you want to play again?")
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							mTeam1Score = 0;
							mTeam2Score = 0;
							mTeam1ScoreView.setText("0");
							mTeam2ScoreView.setText("0");
	
							mTimerView.setText(mMinutes +":00");
							mTimer.start();
							mUsedWords = new ArrayList<Integer>();
							dialog.dismiss();
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							finish();
						}
					});
				return builder.create();
    		default:
    			return null;
    	}
	}
}