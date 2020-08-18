package com.lbz.todoapp.todo

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.format.DateFormat.is24HourFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.lbz.todoapp.R
import com.lbz.todoapp.model.Task
import com.lbz.todoapp.utils.DATE_TIME_FORMAT_12_HOUR
import com.lbz.todoapp.utils.DATE_TIME_FORMAT_24_HOUR
import com.lbz.todoapp.utils.ItemTouchHelperClass
import com.lbz.todoapp.utils.TimeUtils
import java.util.*

/**
 * @author: laibinzhi
 * @date: 2020-08-14 12:03
 * @github: https://github.com/laibinzhi
 * @blog: https://www.laibinzhi.top/
 */
class TaskListAdapter internal constructor(
    private val context: Context
) : RecyclerView.Adapter<TaskListAdapter.ViewHolder>(),
    ItemTouchHelperClass.ItemTouchHelperAdapter {

    interface OnItemEventListener {
        fun onItemRemoved(task: Task)
        fun onItemClick(task: Task)
    }

    fun setOnItemEventListener(listener: OnItemEventListener) {
        this.listener = listener
    }

    private lateinit var listener: OnItemEventListener

    private var tasks = emptyList<Task>() // Cached copy of words

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskItemView: TextView = itemView.findViewById(R.id.toDoListItemTextview)
        val mTimeTextView: TextView = itemView.findViewById(R.id.todoListItemTimeTextView)
        val mColorImageView: ImageView = itemView.findViewById(R.id.toDoListItemColorImageView)
        val rootView: LinearLayout = itemView.findViewById(R.id.listItemLinearLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.task_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = tasks[position]

        if (current.hasReminder && current.time != null) {
            holder.taskItemView.maxLines = 1
            holder.mTimeTextView.visibility = View.VISIBLE
        } else {
            holder.taskItemView.maxLines = 2
            holder.mTimeTextView.visibility = View.GONE
        }

        holder.taskItemView.text = current.name

        val myDrawable = TextDrawable.builder().beginConfig()
            .textColor(Color.WHITE)
            .useFont(Typeface.DEFAULT)
            .toUpperCase()
            .endConfig()
            .buildRound(current.name.substring(0, 1), current.color)

        holder.mColorImageView.setImageDrawable(myDrawable)
        current.time?.let { time ->
            holder.mTimeTextView.text = if (is24HourFormat(context)) TimeUtils.formatDate(
                DATE_TIME_FORMAT_24_HOUR,
                time
            ) else TimeUtils.formatDate(DATE_TIME_FORMAT_12_HOUR, time)

            var nowDate = Date()
            var reminderDate = current.time

            holder.mTimeTextView.setTextColor(
                if (reminderDate.before(nowDate)) ContextCompat.getColor(
                    context,
                    R.color.grey600
                ) else ContextCompat.getColor(context, R.color.colorAccent)
            )
        }
        holder.rootView.setOnClickListener {
            listener.onItemClick(current)
        }
    }

    internal fun setTasks(tasks: List<Task>) {
        this.tasks = tasks
        notifyDataSetChanged()
    }

    override fun getItemCount() = tasks.size

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(tasks, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(tasks, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemRemoved(position: Int) {
        listener.onItemRemoved(task = tasks[position])
    }

}