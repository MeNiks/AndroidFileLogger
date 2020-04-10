package com.niks.filelog

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SimpleSQLiteQuery
import com.niks.filelog.db.AppDatabase
import com.niks.filelog.db.LogDWO
import com.niks.filelog.db.LogDWODao
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

object FileLogHelper {

    private val compositeDisposable = CompositeDisposable()

    private val logMessageSubject: PublishSubject<LogDWO> = PublishSubject.create()

    private lateinit var context: Context

    private val db: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .setJournalMode(RoomDatabase.JournalMode.AUTOMATIC)
            .fallbackToDestructiveMigration()
            .build()
    }

    private val logDWODao: LogDWODao by lazy {
        db.logDWODao()
    }

    fun initialize(context: Context) {
        this.context = context
        compositeDisposable
            .add(
                logMessageSubject
                    .observeOn(Schedulers.io())
                    .switchMap { logDWO ->
                        Observable.just(logDWODao.insertAll(logDWO))
                    }
                    .subscribe()
            )
    }

    fun log(message: String, tag: String = COMMON_TAG, longInfo: String = "") {
        logMessageSubject.onNext(
            LogDWO(
                message = message,
                tag = tag,
                longInfo = longInfo
            )
        )
    }

    fun getAllLogs(tag: String, timeStampSortOrder: String, limit: Int = 10): Observable<List<LogDWO>> {
        return logDWODao
            .getAllLogs(
                SimpleSQLiteQuery(
                    "SELECT * FROM ${LogDWO.TABLE_NAME} " +
                        "WHERE ${LogDWO.TAG} = '$tag' " +
                        "ORDER BY ${LogDWO.TIMESTAMP} $timeStampSortOrder LIMIT $limit"
                )
            )
            .toObservable()
    }

    fun getAllLogs(timeStampSortOrder: String, limit: Int = 10): Observable<List<LogDWO>> {
        return logDWODao
            .getAllLogs(
                SimpleSQLiteQuery(
                    "SELECT * FROM ${LogDWO.TABLE_NAME} " +
                        "ORDER BY ${LogDWO.TIMESTAMP} $timeStampSortOrder LIMIT $limit"
                )
            )
            .toObservable()
    }

    fun getAllTags() = logDWODao.getAllTags()

    fun writeToFile(text: String) {
        try {
            val bufferedWriter = BufferedWriter(FileWriter(getLogFile(context), true))
            bufferedWriter.append(text)
            bufferedWriter.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLogFile(context: Context): File {
        val directoryPath = File(context.cacheDir, "logs")
        if (!directoryPath.exists()) {
            directoryPath.mkdir()
        }
        val logFile = File(directoryPath, "request.log")
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
        return logFile
    }

    const val COMMON_TAG = "ALL"
}