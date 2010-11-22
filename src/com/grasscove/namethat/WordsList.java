package com.grasscove.namethat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.grasscove.namethat.provider.NameThatContract.Categories;
import com.grasscove.namethat.provider.NameThatContract.Words;
import com.grasscove.namethat.provider.model.Word;
import com.grasscove.namethat.quickaction.ActionItem;
import com.grasscove.namethat.quickaction.QuickAction;

public class WordsList extends ListActivity implements MyAsyncQueryHandler.IAsyncQueryListener {

    public static final int EDIT_WORD = 0;
    public static final int DELETE_WORD = 1;

    public static final int DIALOG_CONFIRM_DELETE = 0;
    public static final int DIALOG_EDIT_WORD = 1;
    public static final int DIALOG_ADD_WORD = 2;

    public static final int OPTION_MENU_ADD = 0;

    private SimpleCursorAdapter mListAdapter;
    private Word mWord;
    private EditText mEditedWordView;
    private MyAsyncQueryHandler mQueryHandler;

    private ActionItem mEditActionItem = null;
    private ActionItem mDeleteActionItem = null;
    private QuickAction mQuickAction = null;

    private int mCategoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCategoryId = getIntent().getIntExtra(Categories.Columns._ID, 0);

        mQueryHandler = new MyAsyncQueryHandler(getContentResolver(), this);

        Uri uri;
        if(mCategoryId == 1) {
            uri = Words.URI;
        } else {
            uri = Categories.URI.buildUpon()
                .appendPath(Integer.toString(mCategoryId))
                .appendPath("words")
                .build();
        }
        mQueryHandler.startQuery(uri, Words.PROJECTION);

        String[] from = new String[] {
            Words.Columns.NAME
        };
        int[] to = new int[] {
            R.id.words_row_text
        };

        mListAdapter = new WordsListAdapter(this, R.layout.words_row, null, from, to);

        setContentView(R.layout.categories);

        mEditActionItem = new ActionItem();
        mEditActionItem.setTitle("Edit");
        mEditActionItem.setIcon(getResources().getDrawable(R.drawable.edit_icon));
        mEditActionItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_EDIT_WORD);
            }
        });

        mDeleteActionItem = new ActionItem();
        mDeleteActionItem.setTitle("Delete");
        mDeleteActionItem.setIcon(getResources().getDrawable(R.drawable.delete_icon));
        mDeleteActionItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_CONFIRM_DELETE);
            }
        });

        setListAdapter(mListAdapter);
        registerForContextMenu(getListView());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        mQuickAction = new QuickAction(v, position);

        mWord = (Word) mListAdapter.getItem(position);

        mQuickAction.addActionItem(mEditActionItem);
        mQuickAction.addActionItem(mDeleteActionItem);
        mQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);

        mQuickAction.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mCategoryId == 1) {
            return false; /* We don't want people adding words to the "everything" category */
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.words_add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_words_add:
                showDialog(DIALOG_ADD_WORD);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if(mQuickAction != null) {
            mQuickAction.dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View layout;

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        switch (id) {
            case DIALOG_CONFIRM_DELETE:
                builder
                    .setCancelable(true)
                    .setTitle(getResources().getString(R.string.confirm))
                    .setMessage(getResources().getString(R.string.delete_word_confirm))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Uri uri = Words.URI.buildUpon()
                                .appendPath(Integer.toString(mWord.getId()))
                                .appendPath("delete")
                                .build();
                            mQueryHandler.startDelete(uri);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                return builder.create();
            case DIALOG_EDIT_WORD:
                layout = inflater.inflate(R.layout.words_row_edit, null);

                mEditedWordView = (EditText) layout.findViewById(R.id.word_edit);
                mEditedWordView.setText(mWord.getName());

                builder
                    .setTitle(getResources().getString(R.string.edit_word))
                    .setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mWord.setName(mEditedWordView.getText().toString());
                            Uri uri = Words.URI.buildUpon()
                                .appendPath(Integer.toString(mWord.getId()))
                                .build();
                            mQueryHandler.startUpdate(uri, mWord.getValues());
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                builder.setView(layout);

                return builder.create();
            case DIALOG_ADD_WORD:
                layout = inflater.inflate(R.layout.words_row_edit, null);

                mEditedWordView = (EditText) layout.findViewById(R.id.word_edit);

                builder
                    .setTitle(getResources().getString(R.string.add_word))
                    .setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Word word = new Word();
                            word.setName(mEditedWordView.getText().toString());
                            word.setCategoryId(mCategoryId);
                            mQueryHandler.startInsert(Words.URI, word.getValues());
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                builder.setView(layout);

                return builder.create();
            default:
                return null;
        }
    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        startManagingCursor(cursor);
        mListAdapter.changeCursor(cursor);
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeleteComplete(int token, Object cookie, int result) {
        mListAdapter.getCursor().requery();
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onInsertComplete(int token, Object cookie, Uri uri) {
        mListAdapter.getCursor().requery();
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onUpdateComplete(int token, Object cookie, int result) {
        mListAdapter.getCursor().requery();
        mListAdapter.notifyDataSetChanged();
    }

    public class WordsListAdapter extends SimpleCursorAdapter {

        public WordsListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
        }

        @Override
        public Object getItem(int position) {
            Cursor c = getCursor();
            c.moveToPosition(position);
            return new Word(c);
        }
    }
}