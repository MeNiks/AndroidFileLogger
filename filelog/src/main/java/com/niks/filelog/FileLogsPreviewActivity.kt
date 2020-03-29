package com.niks.filelog

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_file_logs_previewer.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileLogsPreviewActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private val displayLogsForTagSubject: PublishSubject<Pair<String, String>> = PublishSubject.create()

    private lateinit var tagsSpinnerAdapter: ArrayAdapter<String>
    private var timeStampSortOrder = "ASC"
    private var tag = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_logs_previewer)

        FileLogHelper.initialize(this.applicationContext)

        tagsSpinnerAdapter = getAdapter()
        tagsSpinner.adapter = tagsSpinnerAdapter
        tagsSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) { // your code here
                if (!tag.equals(tagsSpinnerAdapter.getItem(position) ?: "", true)) {
                    tag = tagsSpinnerAdapter.getItem(position) ?: ""
                    displayLogsForTagSubject.onNext(tag to timeStampSortOrder)
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) { // your code here
            }
        }

        val timeStampSortAdapter = getAdapter()
        timeStampSortAdapter.add("ASC")
        timeStampSortAdapter.add("DESC")
        timeStampSpinner.adapter = timeStampSortAdapter
        timeStampSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) { // your code here
                if (!timeStampSortOrder.equals(timeStampSortAdapter.getItem(position) ?: "", true)) {
                    timeStampSortOrder = timeStampSortAdapter.getItem(position) ?: ""
                    displayLogsForTagSubject.onNext(tag to timeStampSortOrder)
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) { // your code here
            }
        }
    }

    private fun getAdapter(): ArrayAdapter<String> {
        val spinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayListOf<String>())
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return spinnerAdapter
    }

    override fun onStart() {
        super.onStart()
        compositeDisposable.addAll(
            displayLogsForTagSubject
                .filter { (tag, _) -> tag.isNotBlank() }
                .observeOn(Schedulers.io())
                .switchMap { (tag, order) ->
                    if (tag.equals(FileLogHelper.COMMON_TAG, true)) {
                        FileLogHelper
                            .getAllLogs(timeStampSortOrder = order)
                    } else
                        FileLogHelper
                            .getAllLogs(tag = tag, timeStampSortOrder = order)
                }

                .map { logDwoList ->
                    logDwoList.joinToString(separator = "", transform = { logDow -> logDow.timestamp.readableDate() + " : " + logDow.message + "<br><br>" })
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { text ->
                    if (text.isBlank()) {
                        webView.loadData("None", "text/html", "UTF-8")
                    } else {
                        webView.loadData("<p>$text</p>", "text/html", "UTF-8")
                    }
                },
            FileLogHelper
                .getAllTags()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { tagsList ->
                    tagsSpinnerAdapter.clear()
                    tagsSpinnerAdapter.addAll(tagsList)
                }
        )
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }
}

fun Long.readableDate(pattern: String = "dd-MM-yyyy hh:mm:ss"): String {
    var timeL = this
    if (timeL == -1L) {
        timeL = System.currentTimeMillis()
    }
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timeL))
}