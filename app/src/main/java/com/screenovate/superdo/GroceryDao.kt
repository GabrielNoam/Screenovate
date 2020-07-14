package com.screenovate.superdo

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * GroceryDao
 * @author Gabriel Noam
 */
@Dao
interface GroceryDao {
    @Query("SELECT COUNT(*) FROM grocery")
    fun getCount(): Int?

    @Query("SELECT * FROM grocery")
    suspend fun loadAll(): MutableList<Grocery>

    @Query("SELECT * FROM grocery")
    fun getAll(): LiveData<List<Grocery>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg grocery: Grocery): LongArray

    @Query("DELETE FROM grocery")
    suspend fun deleteAll()
}