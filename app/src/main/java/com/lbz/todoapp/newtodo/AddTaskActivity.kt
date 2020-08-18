package com.lbz.todoapp.newtodo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.lbz.todoapp.R
import com.lbz.todoapp.TaskViewModel
import com.lbz.todoapp.model.Task
import com.lbz.todoapp.utils.DATE_TIME_FORMAT_YEAR_MONTH_DAY
import com.lbz.todoapp.utils.TimeUtils
import com.lbz.todoapp.work.NotifyWork
import com.lbz.todoapp.work.NotifyWork.Companion.NOTIFICATION_ID
import com.lbz.todoapp.work.NotifyWork.Companion.TASK_TITLE
import kotlinx.android.synthetic.main.activity_add_task.*
import java.lang.System.currentTimeMillis
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author: laibinzhi
 * @date: 2020-08-14 12:11
 * @github: https://github.com/laibinzhi
 * @blog: https://www.laibinzhi.top/
 */
class AddTaskActivity : AppCompatActivity() {

    private lateinit var wordViewModel: TaskViewModel

    private lateinit var mUserReminderDate: Date

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        initActionBar()

        // 从ViewModelProvider获取新的或现有的ViewModel。
        wordViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)
        mUserReminderDate = Date()

        button_save.setOnClickListener {
            saveTask()
        }

        switch_btn.setOnCheckedChangeListener { _, isChecked ->
            toDoEnterDateLinearLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        newTodoDateEditText.setOnClickListener {
            openDataSelectDialog()
        }

        newTodoTimeEditText.setOnClickListener {
            openTimeSelectDialog()
        }

        setReminderTextView()

    }

    private fun initActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.elevation = 0F
        supportActionBar?.title = getString(R.string.add_task)
    }

    private fun saveTask() {
        if (!TextUtils.isEmpty(edit_task.text)) {
            val name = edit_task.text.toString()
            val task = Task(
                name,
                "",
                mUserReminderDate,
                switch_btn.isChecked,
                ColorGenerator.MATERIAL.randomColor
            )
            wordViewModel.insert(task)
            if (switch_btn.isChecked) {
                createNotifyWork(task)
            }
            finish()
        } else {
            Toast.makeText(
                applicationContext,
                R.string.empty_not_saved,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun openDataSelectDialog() {
        val date = Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, month, day -> setDate(year, month, day) },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.minDate = currentTimeMillis()
        datePickerDialog.show()
    }

    private fun openTimeSelectDialog() {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val minute: Int = calendar.get(Calendar.MINUTE)
        TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, hour, minute -> setTime(hour, minute) },
            hour,
            minute,
            DateFormat.is24HourFormat(this)
        ).show()
    }

    private fun setTime(hour: Int, minute: Int) {
        val dateFormat =
            if (DateFormat.is24HourFormat(this)) {
                "k:mm"
            } else {
                "a h:mm"
            }
        val calender = Calendar.getInstance()
        calender.time = mUserReminderDate

        val year: Int = calender.get(Calendar.YEAR)
        val month: Int = calender.get(Calendar.MONTH)
        val day: Int = calender.get(Calendar.DAY_OF_MONTH)
        calender.set(year, month, day, hour, minute, 0)
        mUserReminderDate = calender.time
        newTodoTimeEditText.setText(TimeUtils.formatDate(dateFormat, mUserReminderDate))
        setReminderTextView()
    }

    private fun setDate(year: Int, month: Int, day: Int) {
        val calender = Calendar.getInstance()
        calender.set(year, month, day)
        mUserReminderDate = calender.time
        newTodoDateEditText.setText(
            TimeUtils.formatDate(DATE_TIME_FORMAT_YEAR_MONTH_DAY, calender.time)
        )
        setReminderTextView()
    }

    private fun setReminderTextView() {
        if (mUserReminderDate == null) {
            mUserReminderDate = Date()
        }
        val dateString = TimeUtils.formatDate(DATE_TIME_FORMAT_YEAR_MONTH_DAY, mUserReminderDate);
        var timeString: String = ""
        var amPmString: String = ""
        if (DateFormat.is24HourFormat(this)) {
            timeString = TimeUtils.formatDate("k:mm", mUserReminderDate)
        } else {
            timeString = TimeUtils.formatDate("h:mm", mUserReminderDate)
            amPmString = TimeUtils.formatDate("a", mUserReminderDate)
        }
        val finalString = String.format(
            resources.getString(R.string.remind_date_and_time),
            dateString,
            amPmString,
            timeString
        )
        newToDoDateTimeReminderTextView.text = finalString
        newTodoTimeEditText.setText(timeString + amPmString)
    }

    private fun createNotifyWork(task: Task) {
        val customTime = mUserReminderDate.time
        val currentTime = currentTimeMillis()
        if (customTime > currentTime) {
            val data = Data.Builder().putInt(NOTIFICATION_ID, (0 until 100000).random())
                .putString(TASK_TITLE, task.name).build()
            val delay = customTime - currentTime
            scheduleNotification(delay, data,task)
        }
    }

    private fun scheduleNotification(delay: Long, data: Data,task: Task) {
        val notificationWork = OneTimeWorkRequest.Builder(NotifyWork::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(data).build()
        task.work_manager_uuid = notificationWork.id.toString()
        wordViewModel.updateWorkIdByName(notificationWork.id.toString(),task.name)
        val instanceWorkManager = WorkManager.getInstance(this)
        instanceWorkManager.beginWith(notificationWork).enqueue()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish() // back button
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}