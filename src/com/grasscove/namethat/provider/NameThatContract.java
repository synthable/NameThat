package com.grasscove.namethat.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class NameThatContract {

    public static final String AUTHORITY = "com.grasscove.namethat.provider.namethatprovider";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class Categories {

        public static final Uri URI = Uri.parse(CONTENT_URI +"/categories");

        public static final Uri ID_URI = Uri.parse(CONTENT_URI +"/categories/#");

        public static final String TABLE = "categories";

        public static final String TYPE_DIR = "vnd.android.cursor.dir/vnd.grasscove.namethat.category";
        public static final String TYPE_ITEM = "vnd.android.cursor.item/vnd.grasscove.namethat.category";

        public static final class Columns implements BaseColumns {
            public static final String NAME = "name";
        }

        public static final String DEFAULT_SORT_ORDER = Columns.NAME +" ASC";
        
        public static final String CREATE =
            "CREATE TABLE " + TABLE + "("
                + Columns._ID + " integer primary key autoincrement,"
                + Columns.NAME + " varchar(128) default ''"
            + ");";

        public static final String[] PROJECTION = {
            Columns._ID,
            Columns.NAME
        };
    }

    public static final class Words {
        public static final Uri URI = Uri.parse(CONTENT_URI +"/words");

        public static final Uri ID_URI = Uri.parse(CONTENT_URI +"/words/#");

        public static final String TABLE = "wordlist";

        public static final String TYPE_DIR = "vnd.android.cursor.dir/vnd.grasscove.namethat.word";
        public static final String TYPE_ITEM = "vnd.android.cursor.item/vnd.grasscove.namethat.word";

        public static final class Columns implements BaseColumns {            
            public static final String NAME = "word";
            
            public static final String CATEGORY_ID = "cid";
        }

        public static final String DEFAULT_SORT_ORDER = Columns.NAME +" ASC";
        
        public static final String CREATE =
            "CREATE TABLE "+ TABLE +"("
                + Columns._ID +" integer primary key autoincrement,"
                + Columns.NAME +" varchar(128) default '',"
                + Columns.CATEGORY_ID +" integer default 0"
            +");";

        public static final String[] PROJECTION = {
            Columns._ID,
            Columns.NAME,
            Columns.CATEGORY_ID
        };
    }    
}