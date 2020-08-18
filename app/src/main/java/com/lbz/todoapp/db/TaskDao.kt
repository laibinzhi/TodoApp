package com.lbz.todoapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.lbz.todoapp.model.Task

/**
 * @author: laibinzhi
 * @date: 2020-08-14 11:57
 * @github: https://github.com/laibinzhi
 * @blog: https://www.laibinzhi.top/
 */
@Dao
interface TaskDao {

    @Query("SELECT * from task_table")
    fun getAllTask(): LiveData<List<Task>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(task: Task)

    @Query("DELETE FROM task_table")
    fun deleteAll()

    @Delete
    fun remove(task: Task)

    @Query("UPDATE task_table SET work_manager_uuid =:work_manager_uuid WHERE name=:name")
    fun updateWorkIdByName(work_manager_uuid: String, name: String)

}