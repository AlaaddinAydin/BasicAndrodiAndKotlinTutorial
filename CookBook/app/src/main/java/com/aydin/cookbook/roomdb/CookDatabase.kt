package com.aydin.cookbook.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aydin.cookbook.model.Cook

@Database(entities = [Cook::class], version = 1)
abstract class CookDatabase : RoomDatabase() {
    abstract fun cookDao(): CookDAO
}