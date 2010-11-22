package com.grasscove.namethat.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.grasscove.namethat.provider.NameThatContract.Categories;

public class NameThatDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "namethat.db";
    public static final int DATABASE_VERSION = 1;

    public NameThatDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when no database exists in disk and the helper class needs to create a new one.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Categories.CREATE);
        //db.execSQL(Words.CREATE);
    }

    /**
     * Called when there is a database version mismatch meaning that the version of the database on
     * disk needs to be upgraded to the current version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /**
         * Upgrade the existing database to conform to the new version. Multiple previous versions
         * can be handled by comparing _oldVersion and _newVersion values. The simplest case is to
         * drop the old table and create a new one.
         */
        //db.execSQL("DROP TABLE IF EXISTS " + Categories.TABLE);
        //db.execSQL("DROP TABLE IF EXISTS " + Words.TABLE);

        // Create a new one.
        //onCreate(db);
    }
}