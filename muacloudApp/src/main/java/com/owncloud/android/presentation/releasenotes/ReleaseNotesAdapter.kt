

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

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.release_notes_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
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

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
