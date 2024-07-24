

package com.owncloud.android.domain.capabilities.model

data class OCCapability(
    val id: Int? = null,
    var accountName: String?,
    val versionMajor: Int,
    val versionMinor: Int,
    val versionMicro: Int,
    val versionString: String?,
    val versionEdition: String?,
    val corePollInterval: Int,
    val davChunkingVersion: String,
    val filesSharingApiEnabled: CapabilityBooleanType,
    val filesSharingPublicEnabled: CapabilityBooleanType,
    val filesSharingPublicPasswordEnforced: CapabilityBooleanType,
    val filesSharingPublicPasswordEnforcedReadOnly: CapabilityBooleanType,
    val filesSharingPublicPasswordEnforcedReadWrite: CapabilityBooleanType,
    val filesSharingPublicPasswordEnforcedUploadOnly: CapabilityBooleanType,
    val filesSharingPublicExpireDateEnabled: CapabilityBooleanType,
    val filesSharingPublicExpireDateDays: Int,
    val filesSharingPublicExpireDateEnforced: CapabilityBooleanType,
    val filesSharingPublicUpload: CapabilityBooleanType,
    val filesSharingPublicMultiple: CapabilityBooleanType,
    val filesSharingPublicSupportsUploadOnly: CapabilityBooleanType,
    val filesSharingResharing: CapabilityBooleanType,
    val filesSharingFederationOutgoing: CapabilityBooleanType,
    val filesSharingFederationIncoming: CapabilityBooleanType,
    val filesSharingUserProfilePicture: CapabilityBooleanType,
    val filesBigFileChunking: CapabilityBooleanType,
    val filesUndelete: CapabilityBooleanType,
    val filesVersioning: CapabilityBooleanType,
    val filesPrivateLinks: CapabilityBooleanType,
    val filesAppProviders: AppProviders?,
    val spaces: Spaces?,
    val passwordPolicy: PasswordPolicy?,
) {
    fun isChunkingAllowed(): Boolean {
        val doubleChunkingVersion = davChunkingVersion.toDoubleOrNull()
        return (filesBigFileChunking.isTrue && doubleChunkingVersion != null && doubleChunkingVersion >= 1.0)
    }

    fun isFetchingAvatarAllowed(): Boolean {
        return filesSharingUserProfilePicture.isTrue || filesSharingUserProfilePicture.isUnknown
    }

    fun isOpenInWebAllowed(): Boolean = filesAppProviders?.openWebUrl?.isNotBlank() ?: false

    fun isSpacesAllowed(): Boolean = spaces?.enabled == true

    fun isSpacesProjectsAllowed(): Boolean = spaces?.projects == true

    data class AppProviders(
        val enabled: Boolean,
        val version: String,
        val appsUrl: String?,
        val openUrl: String?,
        val openWebUrl: String?,
        val newUrl: String?,
    )

    data class Spaces(
        val enabled: Boolean,
        val projects: Boolean,
        val shareJail: Boolean,
    )

    data class PasswordPolicy(
        val maxCharacters: Int?,
        val minCharacters: Int?,
        val minDigits: Int?,
        val minLowercaseCharacters: Int?,
        val minSpecialCharacters: Int?,
        val minUppercaseCharacters: Int?,
    )
}


enum class CapabilityBooleanType constructor(val value: Int) {
    UNKNOWN(-1),
    FALSE(0),
    TRUE(1);

    val isUnknown: Boolean
        get() = value == -1

    val isFalse: Boolean
        get() = value == 0

    val isTrue: Boolean
        get() = value == 1

    companion object {
        const val capabilityBooleanTypeUnknownString = "-1"

        fun fromValue(value: Int): CapabilityBooleanType =
            when (value) {
                0 -> FALSE
                1 -> TRUE
                else -> UNKNOWN
            }

        fun fromBooleanValue(boolValue: Boolean): CapabilityBooleanType =
            if (boolValue) {
                TRUE
            } else {
                FALSE
            }
    }
}
