package com.grasscove.namethat;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.grasscove.namethat.provider.NameThatContract.Categories;
import com.grasscove.namethat.provider.model.Category;
import com.grasscove.namethat.quickaction.ActionItem;
import com.grasscove.namethat.quickaction.QuickAction;

public class CategoriesList extends ListActivity implements MyAsyncQueryHandler.IAsyncQueryListener {

    private ActionItem mPlayActionItem;
    private ActionItem mEditActionItem;
    private QuickAction mQuickAction;
    private CategoryListAdapter mListAdapter;
    private MyAsyncQueryHandler mQueryHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.categories);

        mPlayActionItem = new ActionItem();
        mPlayActionItem.setTitle(getResources().getString(R.string.menu_options_play));
        mPlayActionItem.setIcon(getResources().getDrawable(R.drawable.start_icon));
        mPlayActionItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Category category = (Category) getListAdapter().getItem(mQuickAction.position);
                Intent intent = new Intent(CategoriesList.this, Play.class);
                intent.putExtra(Categories.Columns._ID, category.getId());
                intent.putExtra(Categories.Columns.NAME, category.getName());
                startActivity(intent);
            }
        });

        mEditActionItem = new ActionItem();
        mEditActionItem.setTitle(getResources().getString(R.string.edit_word_list));
        mEditActionItem.setIcon(getResources().getDrawable(R.drawable.edit_icon));
        mEditActionItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Category category = (Category) getListAdapter().getItem(mQuickAction.position);
                Intent intent = new Intent(CategoriesList.this, WordsList.class);
                intent.putExtra(Categories.Columns._ID, category.getId());
                startActivity(intent);
            }
        });

        mQueryHandler = new MyAsyncQueryHandler(getContentResolver(), this);
        mQueryHandler.startQuery(Categories.URI, Categories.PROJECTION, null);

        String[] from = new String[] {
            Categories.Columns.NAME
        };
        int[] to = new int[] {
            R.id.categories_row_text
        };

        mListAdapter = new CategoryListAdapter(this, R.layout.categories_row, null, from, to);
        setListAdapter(mListAdapter);
        registerForContextMenu(getListView());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        mQuickAction = new QuickAction(v, position);

        mQuickAction.addActionItem(mPlayActionItem);
        mQuickAction.addActionItem(mEditActionItem);
        mQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);

        mQuickAction.show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mQuickAction != null) {
            mQuickAction.dismiss();
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
    }

    @Override
    public void onInsertComplete(int token, Object cookie, Uri uri) {
    }

    @Override
    public void onUpdateComplete(int token, Object cookie, int result) {
    }

    public class CategoryListAdapter extends SimpleCursorAdapter {

        public CategoryListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
        }

        @Override
        public Object getItem(int position) {
            Cursor c = getCursor();
            c.moveToPosition(position);
            return new Category(c);
        }
    }
}