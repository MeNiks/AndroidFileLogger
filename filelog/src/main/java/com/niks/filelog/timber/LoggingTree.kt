package com.niks.filelog.timber

import com.niks.filelog.FileLogHelper
import timber.log.Timber

class LoggingTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        FileLogHelper.log(message = message, tag = tag ?: FileLogHelper.COMMON_TAG)
    }

}
