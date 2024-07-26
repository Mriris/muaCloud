
package com.owncloud.android.lib.resources.status

import android.os.Parcel
import android.os.Parcelable

class OwnCloudVersion(version: String) : Comparable<OwnCloudVersion>, Parcelable {




    private var mVersion: Int = 0
    var isVersionValid: Boolean = false
        set

    var isVersionHidden: Boolean = false

    val version: String
        get() = if (isVersionValid) {
            toString()
        } else {
            INVALID_ZERO_VERSION
        }

    val isServerVersionSupported: Boolean
        get() = mVersion >= MINIMUN_VERSION_SUPPORTED

    val isPublicSharingWriteOnlySupported: Boolean
        get() = mVersion >= MINIMUM_VERSION_WITH_WRITE_ONLY_PUBLIC_SHARING

    init {
        var versionToParse = version
        mVersion = 0
        isVersionValid = false
        isVersionHidden = version.isBlank()
        val countDots = versionToParse.length - versionToParse.replace(".", "").length

        for (i in countDots until MAX_DOTS) {
            versionToParse = "$versionToParse.0"
        }

        parseVersion(versionToParse)

    }

    override fun toString(): String {

        var versionToString = ((mVersion shr 8 * MAX_DOTS) % 256).toString()
        for (i in MAX_DOTS - 1 downTo 0) {


            versionToString = versionToString + "." + ((mVersion shr 8 * i) % 256).toString()
        }
        if (!isVersionValid) {
            versionToString += " INVALID"
        }
        return versionToString
    }

    override fun compareTo(other: OwnCloudVersion): Int {
        return if (other.mVersion == mVersion)
            0
        else if (other.mVersion < mVersion) 1 else -1
    }

    private fun parseVersion(version: String) {
        try {
            mVersion = getParsedVersion(version)
            isVersionValid = true

        } catch (e: Exception) {
            isVersionValid = false


        }
    }

    @Throws(NumberFormatException::class)
    private fun getParsedVersion(version: String): Int {
        var versionToParse = version
        var versionValue = 0

        versionToParse = versionToParse.replace("[^\\d.]".toRegex(), "")

        val nums = versionToParse.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var i = 0
        while (i < nums.size && i <= MAX_DOTS) {
            versionValue += Integer.parseInt(nums[i])
            if (i < nums.size - 1) {
                versionValue = versionValue shl 8
            }
            i++
        }

        return versionValue
    }

    override fun describeContents(): Int {
        return super.hashCode()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(mVersion)
        dest.writeInt(if (isVersionValid) 1 else 0)
    }

    companion object {
        private const val MINIMUN_VERSION_SUPPORTED = 0xA000000 // 10.0.0

        private const val MINIMUM_VERSION_WITH_WRITE_ONLY_PUBLIC_SHARING = 0xA000100 // 10.0.1

        private const val INVALID_ZERO_VERSION = "0.0.0"

        private const val MAX_DOTS = 3
    }
}
