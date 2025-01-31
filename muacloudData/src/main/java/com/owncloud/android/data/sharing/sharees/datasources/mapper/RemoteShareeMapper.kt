

package com.owncloud.android.data.sharing.sharees.datasources.mapper

import com.owncloud.android.domain.mappers.RemoteMapper
import com.owncloud.android.domain.sharing.sharees.model.OCSharee
import com.owncloud.android.domain.sharing.shares.model.ShareType
import com.owncloud.android.lib.resources.shares.responses.ShareeItem
import com.owncloud.android.lib.resources.shares.responses.ShareeOcsResponse

class RemoteShareeMapper : RemoteMapper<List<OCSharee>, ShareeOcsResponse> {
    private fun mapShareeItemToOCSharee(item: ShareeItem, isExactMatch: Boolean) =
        OCSharee(
            label = item.label,
            shareType = ShareType.fromValue(item.value.shareType)!!,
            shareWith = item.value.shareWith,
            additionalInfo = item.value.additionalInfo ?: "",
            isExactMatch = isExactMatch
        )

    override fun toModel(remote: ShareeOcsResponse?): List<OCSharee> {
        val exactMatches = remote?.exact?.getFlatRepresentation()?.map {
            mapShareeItemToOCSharee(it, isExactMatch = true)
        }
        val nonExactMatches = remote?.getFlatRepresentationWithoutExact()?.map {
            mapShareeItemToOCSharee(it, isExactMatch = false)
        }
        return ArrayList<OCSharee>().apply {
            if (exactMatches != null) {
                addAll(exactMatches)
            }
            if (nonExactMatches != null) {
                addAll(nonExactMatches)
            }
        }
    }

    override fun toRemote(model: List<OCSharee>?): ShareeOcsResponse? = null
}