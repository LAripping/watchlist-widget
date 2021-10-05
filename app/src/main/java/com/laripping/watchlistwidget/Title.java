package com.laripping.watchlistwidget;

import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import android.content.ContentValues;


/**
 * Represents one record of the Titles table.
 */
@Entity(tableName = Title.TABLE_NAME)
public class Title {

    /** The name of the Cheese table. */
    public static final String TABLE_NAME = "titles";

    /** The constants for all column names */
    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONST = "const";
    public static final String COLUMN_CREATED = "created";
    public static final String COLUMN_TYPE = "title_type";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_RUNTIME = "runtime";
    public static final String COLUMN_YEAR = "year";
    public static final String COLUMN_GENRES = "genres";
    public static final String COLUMN_DIRECTORS = "directors";


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_ID)
    public int id;

    @ColumnInfo(name = COLUMN_TITLE)
    public String title;

    @ColumnInfo(name = COLUMN_CONST)
    public String constant;

    @ColumnInfo(name = COLUMN_CREATED)
    public String created;

    @ColumnInfo(name = COLUMN_TYPE)
    public String titleType;

    @ColumnInfo(name = COLUMN_RATING)
    public Float rating;

    @ColumnInfo(name = COLUMN_RUNTIME)
    public int runtime;

    @ColumnInfo(name = COLUMN_YEAR)
    public int year;

    @ColumnInfo(name = COLUMN_GENRES)
    public String genres;

    @ColumnInfo(name = COLUMN_DIRECTORS)
    public String directors;



    /**
     * Create a new {@link Title} from the specified {@link ContentValues}.
     *
     * @param values A {@link ContentValues} that at least contain {@link #COLUMN_TITLE}.
     * @return A newly created {@link Title} instance.
     */
    @NonNull
    public static Title fromContentValues(@Nullable ContentValues values) {
        final Title title = new Title();
//        if (values != null && values.containsKey(COLUMN_ID)) {
//            title.id = values.getAsInteger(COLUMN_ID);
//        }
        if (values != null && values.containsKey(COLUMN_TITLE)) {
            title.title = values.getAsString(COLUMN_TITLE);
        }
        if (values != null && values.containsKey(COLUMN_CONST)) {
            title.constant = values.getAsString(COLUMN_CONST);
        }
        if (values != null && values.containsKey(COLUMN_TYPE)){
            title.titleType = values.getAsString(COLUMN_TYPE);
        }
        if (values != null && values.containsKey(COLUMN_DIRECTORS)){
            title.directors = values.getAsString(COLUMN_DIRECTORS);
        }
        if (values != null && values.containsKey(COLUMN_CREATED)){
            title.created = values.getAsString(COLUMN_CREATED);
        }
        if (values != null && values.containsKey(COLUMN_GENRES)){
            title.genres = values.getAsString(COLUMN_GENRES);
        }
        if (values != null && values.containsKey(COLUMN_YEAR)){
            title.year = values.getAsInteger(COLUMN_YEAR);
        }
        if (values != null && values.containsKey(COLUMN_RATING)){
            title.rating = values.getAsFloat(COLUMN_RATING);
        }
        if (values != null && values.containsKey(COLUMN_RUNTIME)){
            title.runtime = values.getAsInteger(COLUMN_RUNTIME);
        }
        return title;
    }
}
