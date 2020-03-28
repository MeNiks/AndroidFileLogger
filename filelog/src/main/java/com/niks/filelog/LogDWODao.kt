package com.niks.filelog

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.reactivex.Observable

@Dao
interface LogDWODao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg obj: LogDWO): List<Long>

    @RawQuery(observedEntities = [LogDWO::class])
    fun getAllLogs(query: SupportSQLiteQuery): Observable<List<LogDWO>>

    @Query("SELECT ${LogDWO.TAG} FROM ${LogDWO.TABLE_NAME} GROUP BY ${LogDWO.TAG} ORDER BY ${LogDWO.TIMESTAMP} DESC")
    fun getAllTags(): Observable<List<String>>
}