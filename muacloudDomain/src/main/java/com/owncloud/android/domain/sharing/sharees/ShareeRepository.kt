

package com.owncloud.android.domain.sharing.sharees

import com.owncloud.android.domain.sharing.sharees.model.OCSharee

interface ShareeRepository {
    fun getSharees(
        searchString: String,
        page: Int,
        perPage: Int,
        accountName: String,
    ): List<OCSharee>
}
