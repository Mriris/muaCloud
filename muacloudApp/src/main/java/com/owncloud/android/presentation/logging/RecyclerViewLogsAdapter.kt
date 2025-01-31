
package com.owncloud.android.presentation.logging

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.owncloud.android.R
import com.owncloud.android.databinding.LogListItemBinding
import com.owncloud.android.extensions.toLegibleStringSize
import java.io.File

class RecyclerViewLogsAdapter(
    private val listener: Listener,
    private val context: Context,
) : RecyclerView.Adapter<RecyclerViewLogsAdapter.ViewHolder>() {

    private val logsList = ArrayList<File>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.log_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val log = logsList[position]
        holder.binding.apply {
            textViewTitleActivityLogsList.text = log.name
            textViewSubtitleActivityLogsList.text = log.toLegibleStringSize(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                imageViewDownloadActivityLogsList.isVisible = false
            }
            imageViewShareActivityLogsList.setOnClickListener {
                listener.share(log)
            }
            imageViewDeleteActivityLogsList.setOnClickListener {
                listener.delete(log, logsList.last() == log)
            }
            imageViewDownloadActivityLogsList.setOnClickListener {
                listener.download(log)
            }
            layoutContainerActivityLogsList.setOnClickListener {
                listener.open(log)
            }
        }
    }

    fun setData(logs: List<File>) {
        val diffCallback = LoggingDiffUtil(logsList, logs)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        logsList.clear()
        logsList.addAll(logs)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = logsList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = LogListItemBinding.bind(itemView)
    }

    interface Listener {
        fun share(file: File)
        fun delete(file: File, isLastLogFileDeleted: Boolean)
        fun open(file: File)
        fun download(file: File)
    }
}
