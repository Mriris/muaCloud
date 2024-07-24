
package com.owncloud.android.ui.preview

import android.accounts.Account
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.ui.fragment.FileFragment
import java.util.HashMap
import java.util.HashSet


class PreviewImagePagerAdapter(
    fragmentManager: FragmentManager,
    private val account: Account,
    private val mImageFiles: MutableList<OCFile>
) : FragmentStatePagerAdapter(fragmentManager) {

    private val mObsoleteFragments: MutableSet<Any>
    private val mObsoletePositions: MutableSet<Int>
    private val mDownloadErrors: MutableSet<Int>
    private val mCachedFragments: MutableMap<Int, FileFragment>

    init {
        mObsoleteFragments = HashSet()
        mObsoletePositions = HashSet()
        mDownloadErrors = HashSet()
        mCachedFragments = HashMap()
    }


    fun getFileAt(position: Int): OCFile = mImageFiles[position]

    override fun getItem(i: Int): Fragment {
        val file = mImageFiles[i]
        val fragment: Fragment
        when {
            file.isAvailableLocally -> {
                fragment = PreviewImageFragment.newInstance(file, account, mObsoletePositions.contains(i))
            }
            mDownloadErrors.contains(i) -> {
                fragment = FileDownloadFragment.newInstance(file, account, true)
                (fragment as FileDownloadFragment).setError(true)
                mDownloadErrors.remove(i)
            }
            else -> {
                fragment = FileDownloadFragment.newInstance(file, account, mObsoletePositions.contains(i))
            }
        }
        mObsoletePositions.remove(i)
        return fragment
    }

    fun getFilePosition(file: OCFile) = mImageFiles.indexOfFirst { file.id == it.id }

    override fun getCount() = mImageFiles.size

    override fun getPageTitle(position: Int): CharSequence = mImageFiles[position].fileName

    private fun updateFile(position: Int, file: OCFile) {
        val fragmentToUpdate = mCachedFragments[position]
        if (fragmentToUpdate != null) {
            mObsoleteFragments.add(fragmentToUpdate)
        }
        mObsoletePositions.add(position)
        mImageFiles[position] = file
    }

    private fun updateWithDownloadError(position: Int) {
        val fragmentToUpdate = mCachedFragments[position]
        if (fragmentToUpdate != null) {
            mObsoleteFragments.add(fragmentToUpdate)
        }
        mDownloadErrors.add(position)
    }

    fun clearErrorAt(position: Int) {
        val fragmentToUpdate = mCachedFragments[position]
        if (fragmentToUpdate != null) {
            mObsoleteFragments.add(fragmentToUpdate)
        }
        mDownloadErrors.remove(position)
    }

    override fun getItemPosition(`object`: Any): Int {
        if (mObsoleteFragments.contains(`object`)) {
            mObsoleteFragments.remove(`object`)
            return POSITION_NONE
        }
        return super.getItemPosition(`object`)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position)
        mCachedFragments[position] = fragment as FileFragment
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        mCachedFragments.remove(position)
        super.destroyItem(container, position, `object`)
    }

    fun pendingErrorAt(position: Int) = mDownloadErrors.contains(position)


    fun resetZoom() {
        val entries: Iterator<FileFragment> = mCachedFragments.values.iterator()
        while (entries.hasNext()) {
            val fileFragment = entries.next()
            if (fileFragment is PreviewImageFragment) {
                fileFragment.getImageView().setScale(1f, true)
            }
        }
    }

    fun onDownloadEvent(file: OCFile, action: String, success: Boolean) {
        val position = getFilePosition(file)
        if (position >= 0) {
            if (success) {
                updateFile(position, file)
            } else {
                updateWithDownloadError(position)
            }
            val fragment = mCachedFragments[position]
            if (fragment is FileDownloadFragment && success) {
                // trigger the creation of new PreviewImageFragment to replace current FileDownloadFragment
                // only if the download succeeded. If not trigger an error
                notifyDataSetChanged()
            } else fragment?.onSyncEvent(action, success, file)
        }
    }
}
