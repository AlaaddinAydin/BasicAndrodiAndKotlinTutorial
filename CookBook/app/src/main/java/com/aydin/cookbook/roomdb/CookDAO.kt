package com.aydin.cookbook.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.aydin.cookbook.model.Cook
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface CookDAO {
    @Query("SELECT * FROM Cook")
    fun getAll() : Flowable<List<Cook>>
    @Query("SELECT * FROM Cook WHERE id = :id")
    fun findById(id :Int) : Flowable<Cook>
    @Insert
    fun insert(cook: Cook) : Completable
    @Delete
    fun delete(cook: Cook) : Completable
}