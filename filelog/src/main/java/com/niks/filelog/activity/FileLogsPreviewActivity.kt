package com.niks.filelog.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.niks.filelog.FileLogHelper
import com.niks.filelog.R
import com.niks.filelog.db.LogDWO
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.activity_file_logs_previewer.*
import kotlinx.android.synthetic.main.apply_operations.view.*
import kotlinx.android.synthetic.main.item_summary_view.view.*
import kotlinx.android.synthetic.main.item_webview.view.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileLogsPreviewActivity : AppCompatActivity() {

    private var tagsList = arrayListOf<String>()
    private val compositeDisposable = CompositeDisposable()
    private val updateUiSubject: BehaviorSubject<ApplyOperations> = BehaviorSubject.create()

    private lateinit var tagsSpinnerAdapter: ArrayAdapter<String>
    private lateinit var timeStampSortAdapter: ArrayAdapter<String>
    private lateinit var limitAdapter: ArrayAdapter<String>

    val applyOperations = ApplyOperations()

    private lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_file_logs_previewer)

        adapter = MyAdapter(this, layoutInflater)

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
        refreshTv
            .setOnClickListener {
                updateUI()
            }

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
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
        dialogView.tagsSpinner.setSelection(tagsSpinnerAdapter.getPosition(applyOperations.tag))
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
        dialogView.timeStampSpinner.setSelection(timeStampSortAdapter.getPosition(applyOperations.timeStampSortOrder))
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
        dialogView.limitSpinner.setSelection(limitAdapter.getPosition(applyOperations.limit.toString()))
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
                .observeOn(AndroidSchedulers.mainThread())
                .retry()
                .subscribe({ logDwoList ->
                    adapter.setList(logDwoList)
                }, {

                }),
            FileLogHelper.getAllTags()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ tagsList: List<String> ->
                    if (this.tagsList.size != tagsList.size) {
                        tagsSpinnerAdapter.clear()
                        tagsSpinnerAdapter.addAll(tagsList)
                        this.tagsList.clear()
                        this.tagsList.addAll(tagsList)
                    }
                }, {})
        )
        updateUI()
    }

    private fun updateUI() {
        updateUiSubject.onNext(applyOperations)
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }

    class MyAdapter(
        private val activity: Activity,
        private val layoutInflater: LayoutInflater
    ) : RecyclerView.Adapter<ViewHolder>() {
        private val listData: ArrayList<LogDWO> = arrayListOf()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(layoutInflater.inflate(R.layout.item_summary_view, null, false))
        }

        override fun getItemCount(): Int {
            return listData.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val logDwo = listData[position]
            holder.itemView.itemTextView.text = HtmlCompat.fromHtml(
                "<b>" + logDwo.timestamp.readableDate() + " : " + "</b>" + logDwo.message,
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            holder.itemView.previewIv.setOnClickListener {
                showLongMessage(logDwo.longInfo, logDwo.timestamp)
            }
            if (logDwo.longInfo.isNotBlank()) {
                holder.itemView.previewIv.visibility = View.VISIBLE
            } else {
                holder.itemView.previewIv.visibility = View.GONE
            }

            holder.itemView.shareIv.setOnClickListener {
                if (logDwo.longInfo.isNotBlank()) {
                    FileLogHelper.writeToFile(logDwo.longInfo)
                } else {
                    FileLogHelper.writeToFile(logDwo.message)
                }

                val contentUri = FileProvider.getUriForFile(activity, "com.niks.filelog.FileProvider", FileLogHelper.getLogFile(activity))
                val shareIntent = Intent()

                shareIntent.action = Intent.ACTION_SEND
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                shareIntent.setDataAndType(contentUri, activity.contentResolver.getType(contentUri))
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.app_name) + "(Logs) :" + logDwo.timestamp.readableDate())
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
                activity.startActivity(Intent.createChooser(shareIntent, "Choose an app"))
            }
        }

        private fun showLongMessage(message: String, timestamp: Long) {
            if (message.isBlank())
                return

            val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            val viewGroup = activity.findViewById<ViewGroup>(R.id.content)
            val dialogView: View = layoutInflater.inflate(R.layout.item_webview, viewGroup, false)

            val dialog = builder.setView(dialogView).create()
            dialog.setTitle(activity.getString(R.string.message))
            dialog.show()

            val webView = dialogView.itemWebView
            if (message.isBlank()) {
                webView.loadData("None", "text/html", "UTF-8")
            } else {
                webView.loadData(
                    "<div style='white-space: nowrap'>" + timestamp.readableDate() + " : " + message + "</div>",
                    "text/html",
                    "UTF-8"
                )
            }
        }

        fun setList(listData: List<LogDWO>) {
            this.listData.clear()
            this.listData.addAll(listData)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
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

