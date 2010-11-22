package com.grasscove.namethat.provider.model;

import com.grasscove.namethat.provider.NameThatContract.Categories;

import android.database.Cursor;

public class Category {

    private int id;
    private String name;
    
    public Category(Cursor c) {
        this.id = c.getInt(c.getColumnIndexOrThrow(Categories.Columns._ID));
        this.name = c.getString(c.getColumnIndexOrThrow(Categories.Columns.NAME));
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
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
}
