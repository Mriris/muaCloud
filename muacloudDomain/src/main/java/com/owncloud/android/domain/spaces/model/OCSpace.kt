

package com.owncloud.android.domain.spaces.model

data class OCSpace(
    val accountName: String,
    val driveAlias: String,
    val driveType: String,
    val id: String,
    val lastModifiedDateTime: String?,
    val name: String,
    val owner: SpaceOwner?,
    val quota: SpaceQuota?,
    val root: SpaceRoot,
    val webUrl: String,
    val description: String?,
    val special: List<SpaceSpecial>?,
) {
    val isPersonal get() = driveType == DRIVE_TYPE_PERSONAL
    val isProject get() = driveType == DRIVE_TYPE_PROJECT

    val isDisabled get() = root.deleted?.state == DRIVE_DISABLED

    fun getSpaceSpecialImage(): SpaceSpecial? {
        val imageSpecial = special?.filter {
            it.specialFolder.name == "image"
        }
        return imageSpecial?.firstOrNull()
    }

    companion object {
        const val DRIVE_TYPE_PERSONAL = "personal"
        const val DRIVE_TYPE_PROJECT = "project"
        private const val DRIVE_DISABLED = "trashed"

        // https://github.com/owncloud/web/blob/2ab7e6da432cb344cd86f3451eea1678461c5c90/packages/web-client/src/helpers/space/types.ts#L11
        const val SPACE_ID_SHARES = "a0ca6a90-a365-4782-871e-d44447bbc668\$a0ca6a90-a365-4782-871e-d44447bbc668"
    }
}

data class SpaceOwner(
    val user: SpaceUser
)

data class SpaceQuota(
    val remaining: Long?,
    val state: String?,
    val total: Long,
    val used: Long?,
)

data class SpaceRoot(
    val eTag: String?,
    val id: String,
    val webDavUrl: String,
    val deleted: SpaceDeleted?,
)

data class SpaceSpecial(
    val eTag: String,
    val file: SpaceFile,
    val id: String,
    val lastModifiedDateTime: String,
    val name: String,
    val size: Int,
    val specialFolder: SpaceSpecialFolder,
    val webDavUrl: String
)

data class SpaceDeleted(
    val state: String,
)

data class SpaceUser(
    val id: String
)

data class SpaceFile(
    val mimeType: String
)

data class SpaceSpecialFolder(
    val name: String
)
