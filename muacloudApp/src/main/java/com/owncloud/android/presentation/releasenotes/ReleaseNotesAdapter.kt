

package com.owncloud.android.presentation.releasenotes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.owncloud.android.R
import com.owncloud.android.databinding.ReleaseNotesItemBinding

class ReleaseNotesAdapter : RecyclerView.Adapter<ReleaseNotesAdapter.ViewHolder>() {

    private val dataSet = ArrayList<ReleaseNote>()


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ReleaseNotesItemBinding.bind(view)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.release_notes_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {


        val releaseNote = dataSet[position]
        viewHolder.binding.run {
            titleReleaseNote.setText(releaseNote.title)
            subtitleReleaseNote.setText(releaseNote.subtitle)
            iconReleaseNote.setImageResource(releaseNote.type.drawableRes)
        }
    }

    fun setData(releaseNotes: List<ReleaseNote>) {
        dataSet.clear()
        dataSet.addAll(releaseNotes)
    }

    override fun getItemCount() = dataSet.size

}
