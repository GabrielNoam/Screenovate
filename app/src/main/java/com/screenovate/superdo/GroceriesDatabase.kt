package com.screenovate.superdo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * GroceriesDatabase
 * @author Gabriel Noam
 */
@Database(entities = [Grocery::class], version = 1)
abstract class GroceriesDatabase : RoomDatabase() {

    companion object {
        private const val DB_NAME = "Grocery"
        private var db: GroceriesDatabase? = null

        fun getInstance(context: Context): GroceriesDatabase =
            db.singleton(this) {
                buildDatabase(context).also { db = it}
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context, GroceriesDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }

    abstract fun groceryDao(): GroceryDao
}