package com.lbz.todoapp.data

import androidx.lifecycle.LiveData
import com.lbz.todoapp.db.TaskDao
import com.lbz.todoapp.model.Task

/**
 * @author: laibinzhi
 * @date: 2020-08-14 11:58
 * @github: https://github.com/laibinzhi
 * @blog: https://www.laibinzhi.top/
 */
// 在构造器中声明Dao的私有属性，通过Dao而不是整个数据库，因为只需要访问Dao
class TaskRepository(private val taskDao: TaskDao) {

    // Room在单独的线程上执行所有查询
    // 观察到的LiveData将在数据更改时通知观察者。
    val allWords: LiveData<List<Task>> = taskDao.getAllTask()

    fun insert(task: Task) {
        taskDao.insert(task)
    }

    fun remove(task: Task) {
        taskDao.remove(task)
    }

    fun updateWorkIdByName(work_manager_uuid: String, name: String) {
        val update = taskDao.updateWorkIdByName(work_manager_uuid, name)
    }

}