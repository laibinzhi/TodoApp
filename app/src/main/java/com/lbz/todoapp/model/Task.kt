package com.lbz.todoapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.*

/**
 * @author: laibinzhi
 * @date: 2020-08-14 11:56
 * @github: https://github.com/laibinzhi
 * @blog: https://www.laibinzhi.top/
 */
@Entity(tableName = "task_table")
@TypeConverters(DateConverter::class)
data class Task(
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "desc")
    val desc: String,
    @ColumnInfo(name = "time")
    val time: Date?,
    @ColumnInfo(name = "hasReminder")
    val hasReminder: Boolean,//是否有提醒
    @ColumnInfo(name = "color")
    val color: Int
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
    @ColumnInfo(name = "work_manager_uuid")
    var work_manager_uuid: String = ""
}