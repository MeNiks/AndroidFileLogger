package com.niks.filelog

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Observable

@Dao
interface LogDWODao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg obj: LogDWO): List<Long>

    @Query("SELECT * FROM ${LogDWO.TABLE_NAME} ORDER BY ${LogDWO.TIMESTAMP} DESC")
    fun getAllLogs(): Observable<List<LogDWO>>

    @Query("SELECT * FROM ${LogDWO.TABLE_NAME} WHERE ${LogDWO.TAG} = :tag ORDER BY ${LogDWO.TIMESTAMP} DESC")
    fun getAllLogs(tag: String): Observable<List<LogDWO>>

    @Query("SELECT ${LogDWO.TAG} FROM ${LogDWO.TABLE_NAME} GROUP BY ${LogDWO.TAG} ORDER BY ${LogDWO.TIMESTAMP} DESC")
    fun getAllTags(): Observable<List<String>>
}