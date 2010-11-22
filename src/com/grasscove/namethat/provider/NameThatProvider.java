package com.grasscove.namethat.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.grasscove.namethat.provider.NameThatContract.Categories;
import com.grasscove.namethat.provider.NameThatContract.Words;

public class NameThatProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher;

    private static final int CATEGORIES = 100;
    private static final int CATEGORY = 101;
    private static final int CATEGORY_WORDS = 102;
    private static final int CATEGORY_WORDS_RANDOM = 103;

    private static final int WORDS = 200;
    private static final int WORD = 201;
    private static final int WORD_DELETE = 202;

    private NameThatDbHelper dbHelper;

    /*
     * This static block sets up the pattern matches for the various URIs that this content provider
     * can handle.
     */
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        sUriMatcher.addURI(NameThatContract.AUTHORITY, "categories", CATEGORIES);
        sUriMatcher.addURI(NameThatContract.AUTHORITY, "categories/#", CATEGORY);
        sUriMatcher.addURI(NameThatContract.AUTHORITY, "categories/#/words", CATEGORY_WORDS);
        sUriMatcher.addURI(NameThatContract.AUTHORITY, "categories/#/words/random", CATEGORY_WORDS_RANDOM);

        sUriMatcher.addURI(NameThatContract.AUTHORITY, "words", WORDS);
        sUriMatcher.addURI(NameThatContract.AUTHORITY, "words/#", WORD);
        sUriMatcher.addURI(NameThatContract.AUTHORITY, "words/#/delete", WORD_DELETE);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new NameThatDbHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case WORD_DELETE: {
                String wordId = uri.getPathSegments().get(1);
                count = db.delete(Words.TABLE, Words.Columns._ID + "=" + wordId, null);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case CATEGORIES:
                return Categories.TYPE_DIR;
            case CATEGORY:
                return Categories.TYPE_ITEM;
            case WORDS:
                return Words.TYPE_DIR;
            case WORD:
                return Words.TYPE_ITEM;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        try {

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            long rowId;
            Uri notifyUri;
            switch (sUriMatcher.match(uri)) {
                case CATEGORIES: {
                    notifyUri = Categories.URI;
                    rowId = db.insertOrThrow(Categories.TABLE, null, values);
                    break;
                }
                case WORDS: {
                    notifyUri = Words.URI;
                    rowId = db.insertOrThrow(Words.TABLE, null, values);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown URL " + uri);
            }

            if (rowId > 0) {
                Uri insertUri = ContentUris.withAppendedId(notifyUri, rowId);
                getContext().getContentResolver().notifyChange(insertUri, null);
                return insertUri;
            }
        } catch (SQLiteConstraintException e) {
            /**
             * Most likely this is because the row is already in the local database.
             * TODO: Handle this
             */

        }

        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        Log.v("query()", uri.toString());

        String limit = null;
        
        switch (sUriMatcher.match(uri)) {
            case CATEGORIES:
                qb.setTables(Categories.TABLE);
                break;
            case CATEGORY:
                qb.setTables(Categories.TABLE);
                if (selection == null) {
                    selection = Categories.Columns._ID + " = " + uri.getLastPathSegment();
                }
                break;
            case WORDS:
                qb.setTables(Words.TABLE);
                sortOrder = Words.DEFAULT_SORT_ORDER;
                break;
            case WORD:
                qb.setTables(Words.TABLE);
                if (selection == null) {
                    selection = Words.Columns._ID + " = " + uri.getLastPathSegment();
                }
                break;
            case CATEGORY_WORDS:
                qb.setTables(Words.TABLE);
                selection = Words.Columns.CATEGORY_ID +"="+ uri.getPathSegments().get(1);
                sortOrder = Words.DEFAULT_SORT_ORDER;
                break;
            case CATEGORY_WORDS_RANDOM:
                qb.setTables(Words.TABLE);
                selection = Words.Columns.CATEGORY_ID +"="+ uri.getPathSegments().get(1);
                sortOrder = "RANDOM()";
                limit = "1";
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }

        SQLiteDatabase mDb = dbHelper.getReadableDatabase();
        Cursor c = qb.query(mDb, projection, selection, selectionArgs, null, null, sortOrder, limit);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        switch(sUriMatcher.match(uri)) {
            case WORD:
                selection = Words.Columns._ID +"="+ uri.getPathSegments().get(1);
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(Words.TABLE, values, selection, selectionArgs);
    }
}
