package com.lbz.todoapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lbz.todoapp.model.Task
import kotlinx.coroutines.CoroutineScope

/**
 * @author: laibinzhi
 * @date: 2020-08-14 11:57
 * @github: https://github.com/laibinzhi
 * @blog: https://www.laibinzhi.top/
 */
@Database(entities = [Task::class], version = 1)
abstract class TaskRoomDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskRoomDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): TaskRoomDatabase {
            // 如果INSTANCE为null，返回此INSTANCE，否则，创建database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskRoomDatabase::class.java,
                    "task_database"
                )
                    // 如果没有迁移数据库，则擦除并重建而不是迁移。
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}