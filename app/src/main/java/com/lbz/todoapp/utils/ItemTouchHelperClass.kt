package com.lbz.todoapp.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * @author: laibinzhi
 * @date: 2020-08-14 14:43
 * @github: https://github.com/laibinzhi
 * @blog: https://www.laibinzhi.top/
 */
class ItemTouchHelperClass(private var adapter: ItemTouchHelperAdapter) : ItemTouchHelper.Callback() {

    interface ItemTouchHelperAdapter {
        fun onItemMoved(fromPosition: Int, toPosition: Int)

        fun onItemRemoved(position: Int)
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val upFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(upFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onItemMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.onItemRemoved(viewHolder.adapterPosition)
    }

}