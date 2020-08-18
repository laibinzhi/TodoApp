package com.lbz.todoapp.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * @author: laibinzhi
 * @date: 2020-08-14 15:22
 * @github: https://github.com/laibinzhi
 * @blog: https://www.laibinzhi.top/
 */
object TimeUtils {

    fun formatDate(
        formatString: String,
        dateToFormat: Date
    ): String {
        val simpleDateFormat = SimpleDateFormat(formatString)
        return simpleDateFormat.format(dateToFormat)
    }

}

const val DATE_TIME_FORMAT_12_HOUR = "yyyy-MM-dd  a h:mm"
const val DATE_TIME_FORMAT_24_HOUR = "yyyy-MM-dd  k:mm"
const val DATE_TIME_FORMAT_YEAR_MONTH_DAY = "yyyy-MM-dd"
