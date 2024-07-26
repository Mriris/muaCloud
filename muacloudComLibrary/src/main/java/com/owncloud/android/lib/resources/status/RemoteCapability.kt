

package com.owncloud.android.lib.resources.status


data class RemoteCapability(
    var accountName: String = "",

    var versionMajor: Int = 0,
    var versionMinor: Int = 0,
    var versionMicro: Int = 0,
    var versionString: String = "",
    var versionEdition: String = "",

    var corePollinterval: Int = 0,

    val chunkingVersion: String = "",

    var filesSharingApiEnabled: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    var filesSharingPublicEnabled: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    var filesSharingPublicPasswordEnforced: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    var filesSharingPublicPasswordEnforcedReadOnly: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    var filesSharingPublicPasswordEnforcedReadWrite: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    var filesSharingPublicPasswordEnforcedUploadOnly: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    var filesSharingPublicExpireDateEnabled: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    var filesSharingPublicExpireDateDays: Int = 0,
    var filesSharingPublicExpireDateEnforced: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    var filesSharingPublicUpload: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    var filesSharingPublicMultiple: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    var filesSharingPublicSupportsUploadOnly: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    var filesSharingResharing: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    var filesSharingFederationOutgoing: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    var filesSharingFederationIncoming: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    var filesSharingUserProfilePicture: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,

    var filesBigFileChunking: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    var filesUndelete: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    var filesVersioning: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    val filesPrivateLinks: CapabilityBooleanType = CapabilityBooleanType.UNKNOWN,
    val filesAppProviders: List<RemoteAppProviders>?,

    val spaces: RemoteSpaces?,

    val passwordPolicy: RemotePasswordPolicy?,
) {

    enum class CapabilityBooleanType constructor(val value: Int) {
        UNKNOWN(-1),
        FALSE(0),
        TRUE(1);

        companion object {
            fun fromValue(value: Int): CapabilityBooleanType? {
                return when (value) {
                    -1 -> UNKNOWN
                    0 -> FALSE
                    1 -> TRUE
                    else -> null
                }
            }

            fun fromBooleanValue(boolValue: Boolean?): CapabilityBooleanType {
                return if (boolValue != null && boolValue) {
                    TRUE
                } else {
                    FALSE
                }
            }
        }
    }

    data class RemoteAppProviders(
        val enabled: Boolean,
        val version: String,
        val appsUrl: String?,
        val openUrl: String?,
        val openWebUrl: String?,
        val newUrl: String?,
    )

    data class RemoteSpaces(
        val enabled: Boolean,
        val projects: Boolean,
        val shareJail: Boolean,
    )

    data class RemotePasswordPolicy(
        val maxCharacters: Int?,
        val minCharacters: Int?,
        val minDigits: Int?,
        val minLowercaseCharacters: Int?,
        val minSpecialCharacters: Int?,
        val minUppercaseCharacters: Int?,
    )
}
