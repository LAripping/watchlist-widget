package com.laripping.watchlistwidget;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Title.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    /**
     * @return The DAO for the Cheese table.
     */
    @SuppressWarnings("WeakerAccess")
    public abstract TitleDao titleDao();

    /** The only instance */
    private static AppDatabase sInstance;

    /**
     * Gets the singleton instance of AppDatabase.
     *
     * @param context The context.
     * @return The singleton instance of AppDatabase.
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = Room
                    .databaseBuilder(context.getApplicationContext(), AppDatabase.class, "wwdb")
                    .build();
        }
        return sInstance;
    }

    /**
     * Inserts the movies into the database
     */
    private void populateDatabase() {
//        if (cheese().count() == 0) {
//            runInTransaction(new Runnable() {
//                @Override
//                public void run() {
//                    Cheese cheese = new Cheese();
//                    for (int i = 0; i < Cheese.CHEESES.length; i++) {
//                        cheese.name = Cheese.CHEESES[i];
//                        cheese().insert(cheese);
//                    }
//                }
//            });
//        }
    }
}
