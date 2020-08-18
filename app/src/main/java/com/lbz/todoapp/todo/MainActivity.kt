package com.lbz.todoapp.todo

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkManager
import com.lbz.todoapp.R
import com.lbz.todoapp.TaskViewModel
import com.lbz.todoapp.model.Task
import com.lbz.todoapp.newtodo.AddTaskActivity
import com.lbz.todoapp.utils.ItemTouchHelperClass
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var wordViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adapter = TaskListAdapter(this)
        recyclerview.adapter = adapter
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.itemAnimator = DefaultItemAnimator()
        recyclerview.setHasFixedSize(true)
        adapter.setOnItemEventListener(object : TaskListAdapter.OnItemEventListener {
            override fun onItemRemoved(task: Task) {
                Toast.makeText(baseContext, "删除" + task.name + "成功", Toast.LENGTH_SHORT).show()
                wordViewModel.remove(task)
                if (!TextUtils.isEmpty(task.work_manager_uuid)) {
                    WorkManager.getInstance (this@MainActivity)
                        .cancelWorkById(UUID.fromString(task.work_manager_uuid))
                }
            }

            override fun onItemClick(task: Task) {
                Toast.makeText(
                    baseContext,
                    "点击" + task.name + task.work_manager_uuid,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        val itemTouchHelperClass = ItemTouchHelperClass(adapter)
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperClass)
        itemTouchHelper.attachToRecyclerView(recyclerview)

        // 从ViewModelProvider获取新的或现有的ViewModel。
        wordViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        // 在getAllTask返回的LiveData上添加观察者。
        // 当观察到的数据更改并且Acticity处于前台时，将触发onChanged（）方法。
        wordViewModel.allWords.observe(this, Observer { words ->
            // Update the cached copy of the words in the adapter.
            words?.let {
                if (it.isEmpty()) {
                    toDoEmptyView.visibility = View.VISIBLE
                    recyclerview.visibility = View.GONE
                    WorkManager.getInstance(this@MainActivity).cancelAllWork()
                } else {
                    toDoEmptyView.visibility = View.GONE
                    recyclerview.visibility = View.VISIBLE
                    adapter.setTasks(it)
                }
            }
        })

        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, AddTaskActivity::class.java)
            startActivity(intent)
        }

    }
}