package com.laripping.watchlistwidget;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface TitleDao {
    @Query("SELECT * FROM "+Title.TABLE_NAME)
    Single<List<Title>> getAll();

    /**
     * Select all titles in a Cursor.
     *
     * @return A {@link Cursor} of all the cheeses in the table.
     */
    @Query("SELECT * FROM " + Title.TABLE_NAME)
    Cursor getAllCursor();

    @Query("SELECT * FROM "+Title.TABLE_NAME+" WHERE title LIKE :name LIMIT 1")
    Title findByName(String name);

    /**
     * Inserts a title into the table.
     *
     */
    @Insert
    Single<Long> insert(Title title);

    /**
     * Inserts multiple titles into the database
     *
     */
    @Insert
    Completable insertAll(List<Title> titles);


    /**
     * Counts the number of titles in the table.
     * @return The number of titles.
     */
    @Query("SELECT COUNT(*) FROM " + Title.TABLE_NAME)
    Single<Integer> count();

//    @Delete
//    void delete(User user);

//    @Query("SELECT * FROM title WHERE id IN (:titleIds)")
//    List<Title> loadAllByIds(int[] titleIds);
}
