package com.lbz.todoapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.lbz.todoapp.data.TaskRepository
import com.lbz.todoapp.db.TaskRoomDatabase
import com.lbz.todoapp.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author: laibinzhi
 * @date: 2020-08-14 11:58
 * @github: https://github.com/laibinzhi
 * @blog: https://www.laibinzhi.top/
 */
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository

    // 使用LiveData并缓存getAllTask返回的内容有几个好处：
    // - 每当Room数据库有更新的时候通知观察者，而不是轮询更新
    //   数据变化适时更新UI。
    // - 存储库通过ViewModel与UI完全隔离。
    val allWords: LiveData<List<Task>>

    init {
        val taskDao = TaskRoomDatabase.getDatabase(application, viewModelScope).taskDao()
        repository = TaskRepository(taskDao)
        allWords = repository.allWords
    }

    /**
     * 启动新的协程以非阻塞方式插入数据
     */
    fun insert(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        try {
            repository.insert(task)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun remove(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        try {
            repository.remove(task)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateWorkIdByName(work_manager_uuid: String, name: String) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateWorkIdByName(work_manager_uuid, name)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

}