package com.grasscove.namethat.provider.model;

import com.grasscove.namethat.provider.NameThatContract;
import com.grasscove.namethat.provider.NameThatContract.Words;

import android.content.ContentValues;
import android.database.Cursor;

public class Word {
	private int id;
    private int categoryId;
    private String name;

    private boolean isNew = false;

    public Word() {
        isNew = true;
    }

    public Word(Cursor c) {
        this.id = c.getInt(c.getColumnIndexOrThrow(Words.Columns._ID));
        this.categoryId = c.getInt(c.getColumnIndexOrThrow(Words.Columns.CATEGORY_ID));
        this.setName(c.getString(c.getColumnIndexOrThrow(Words.Columns.NAME)));
    }

    /**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the categoryId
	 */
	public int getCategoryId() {
		return categoryId;
	}

	/**
	 * @param categoryId the categoryId to set
	 */
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	/**
	 * @return the isNew
	 */
	public boolean isNew() {
		return isNew;
	}

	/**
	 * @param isNew the isNew to set
	 */
	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put(NameThatContract.Words.Columns.NAME, this.name);
        values.put(NameThatContract.Words.Columns.CATEGORY_ID, this.categoryId);
        return values;
    }
}