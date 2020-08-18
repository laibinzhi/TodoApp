package com.lbz.todoapp.model

import androidx.room.TypeConverter
import java.util.*

/**
 * @author: laibinzhi
 * @date: 2020-08-14 15:02
 * @github: https://github.com/laibinzhi
 * @blog: https://www.laibinzhi.top/
 */
class DateConverter {

    @TypeConverter
    fun revertDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun converterDate(date: Date?): Long? {
        return date?.time
    }

}