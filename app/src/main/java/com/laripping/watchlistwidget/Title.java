package com.laripping.watchlistwidget;

import android.database.Cursor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DAO/POJO/you name it, to access Cursor rows programmatically
 */
public class Title {
    private String title;
    private String tConst;
    private Date created;
    private Type tType;
    private float rating;
    private int runtime;
    private int year;
    private String genres;
    private String directors;

    public enum Type {
        tvMovie, movie, tvMiniSeries, tvSeries
    }

    public Title(Cursor cursor) {
        for(int colIndex=0; colIndex<cursor.getColumnCount(); colIndex++){
            switch(cursor.getColumnName(colIndex)){
                case AppDatabaseSqlite.COLUMN_CONST:
                    this.tConst = cursor.getString(colIndex);
                    break;
                case AppDatabaseSqlite.COLUMN_CREATED:
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        this.created = format.parse(cursor.getString(colIndex));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case AppDatabaseSqlite.COLUMN_DIRECTORS:
                    this.directors = cursor.getString(colIndex);
                    break;
                case AppDatabaseSqlite.COLUMN_TITLE:
                    this.title = cursor.getString(colIndex);
                    break;
                case AppDatabaseSqlite.COLUMN_GENRES:
                    this.genres = cursor.getString(colIndex);
                    break;
                case AppDatabaseSqlite.COLUMN_RATING:
                    this.rating = cursor.getFloat(colIndex);
                    break;
                case AppDatabaseSqlite.COLUMN_RUNTIME:
                    this.runtime = cursor.getInt(colIndex);
                    break;
                case AppDatabaseSqlite.COLUMN_YEAR:
                    this.year = cursor.getInt(colIndex);
                    break;
                case AppDatabaseSqlite.COLUMN_TYPE:
                    this.tType = Enum.valueOf(Type.class, cursor.getString(colIndex) );
                    break;
            }
        }
    }

    public String getTitle() {
        return title;
    }

    public String gettConst() {
        return tConst;
    }

    public Date getCreated() {
        return created;
    }

    public Type gettType() {
        return tType;
    }

    public float getRating() {
        return rating;
    }

    public int getRuntime() {
        return runtime;
    }

    public int getYear() {
        return year;
    }

    public String getGenres() {
        return genres;
    }

    public String getDirectors() {
        return directors;
    }
}
