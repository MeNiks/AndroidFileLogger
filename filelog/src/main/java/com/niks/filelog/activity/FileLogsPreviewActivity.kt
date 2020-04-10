package com.niks.filelog.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.niks.filelog.FileLogHelper
import com.niks.filelog.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_file_logs_previewer.*
import kotlinx.android.synthetic.main.apply_operations.view.*
import java.text.SimpleDateFormat
import java.util.*

class FileLogsPreviewActivity : AppCompatActivity() {

    private var tagsList = arrayListOf<String>()
    private val compositeDisposable = CompositeDisposable()
    private val updateUiSubject: BehaviorSubject<ApplyOperations> = BehaviorSubject.create()

    private lateinit var tagsSpinnerAdapter: ArrayAdapter<String>
    private lateinit var timeStampSortAdapter: ArrayAdapter<String>
    private lateinit var limitAdapter: ArrayAdapter<String>

    val applyOperations = ApplyOperations()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_logs_previewer)

        FileLogHelper.initialize(this.applicationContext)

        tagsSpinnerAdapter = getAdapter()

        timeStampSortAdapter = getAdapter()
        timeStampSortAdapter.add("ASC")
        timeStampSortAdapter.add("DESC")


        limitAdapter = getAdapter()
        limitAdapter.add("5")
        limitAdapter.add("10")
        limitAdapter.add("30")
        for (i in 50..5000 step 50) {
            limitAdapter.add(i.toString())
        }

        changeFilterTv.setOnClickListener {
            showApplyOperationsDialog()
        }
    }

    private fun showApplyOperationsDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val viewGroup = findViewById<ViewGroup>(R.id.content)
        val dialogView: View = layoutInflater.inflate(R.layout.apply_operations, viewGroup, false)

        val dialog = builder.setView(dialogView).create()
        dialog.setTitle(getString(R.string.apply_filters))
        dialog.show()
        dialogView
            .applyFiltersTv
            .setOnClickListener {
                dialog.dismiss()
            }
        dialogView.tagsSpinner.adapter = tagsSpinnerAdapter
        dialogView.tagsSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                if (!applyOperations.tag.equals(tagsSpinnerAdapter.getItem(position) ?: "", true)) {
                    applyOperations.tag = tagsSpinnerAdapter.getItem(position) ?: ""
                    updateUiSubject.onNext(applyOperations)
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) { // your code here
            }
        }


        dialogView.timeStampSpinner.adapter = timeStampSortAdapter
        dialogView.timeStampSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                if (!applyOperations.timeStampSortOrder.equals(timeStampSortAdapter.getItem(position) ?: "", true)) {
                    applyOperations.timeStampSortOrder = timeStampSortAdapter.getItem(position) ?: ""
                    updateUiSubject.onNext(applyOperations)
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) { // your code here
            }
        }


        dialogView.limitSpinner.adapter = limitAdapter
        dialogView.limitSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                val selectedLimit = (limitAdapter.getItem(position) as String).toInt()
                if (applyOperations.limit != selectedLimit) {
                    applyOperations.limit = selectedLimit
                    updateUiSubject.onNext(applyOperations)
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
            updateUiSubject
                .filter { applyOperations -> applyOperations.tag.isNotBlank() }
                .observeOn(Schedulers.io())
                .switchMap { applyOperations ->
                    if (applyOperations.tag.equals(FileLogHelper.COMMON_TAG, true)) {
                        FileLogHelper.getAllLogs(
                            timeStampSortOrder = applyOperations.timeStampSortOrder,
                            limit = applyOperations.limit
                        )
                    } else
                        FileLogHelper.getAllLogs(
                            tag = applyOperations.tag,
                            timeStampSortOrder = applyOperations.timeStampSortOrder,
                            limit = applyOperations.limit
                        )
                }
                .map { logDwoList ->
                    logDwoList.joinToString(
                        separator = "",
                        transform = { logDow -> logDow.timestamp.readableDate() + " : " + logDow.message + "<br><br>" })
                }
                .observeOn(AndroidSchedulers.mainThread())
                .retry()
                .subscribe({ text ->
                    if (text.isBlank()) {
                        webView.loadData("None", "text/html", "UTF-8")
                    } else {
                        webView.loadData(
                            "<div style='white-space: nowrap;'>$text</div>",
                            "text/html",
                            "UTF-8"
                        )
                    }
                }, {

                }),
            FileLogHelper.getAllTags()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { tagsList: List<String> ->
                    if (this.tagsList.size != tagsList.size) {
                        tagsSpinnerAdapter.clear()
                        tagsSpinnerAdapter.addAll(tagsList)
                        this.tagsList.clear()
                        this.tagsList.addAll(tagsList)
                    }
                }
        )
        updateUiSubject.onNext(applyOperations)
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }
}

data class ApplyOperations(
    var tag: String = FileLogHelper.COMMON_TAG,
    var timeStampSortOrder: String = "ASC",
    var limit: Int = 10
)

fun Long.readableDate(pattern: String = "dd-MM-yyyy hh:mm:ss"): String {
    var timeL = this
    if (timeL == -1L) {
        timeL = System.currentTimeMillis()
    }
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timeL))
}