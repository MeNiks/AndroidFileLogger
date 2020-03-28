package com.niks.filelog

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

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

    fun log(message: String, tag: String = COMMON_TAG) {
        logMessageSubject.onNext(
            LogDWO(
                message = message,
                tag = tag
            )
        )
    }

    fun getAllLogs(tag: String): Observable<List<LogDWO>> {
        return logDWODao
            .getAllLogs(tag)
    }

    fun getAllLogs(): Observable<List<LogDWO>> {
        return logDWODao
            .getAllLogs()
    }

    fun getAllTags() = logDWODao.getAllTags()

    const val COMMON_TAG = "ALL"
}