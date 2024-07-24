
package com.owncloud.android.testutil

import com.owncloud.android.domain.user.model.UserAvatar
import com.owncloud.android.domain.user.model.UserInfo
import com.owncloud.android.domain.user.model.UserQuota

val OC_USER_INFO = UserInfo(
    id = "admin",
    displayName = "adminOc",
    email = null
)

val OC_USER_QUOTA = UserQuota(
    accountName = OC_ACCOUNT_NAME,
    used = 80_000,
    available = 200_000
)

val OC_USER_AVATAR = UserAvatar(
    byteArrayOf(1, 2, 3, 4, 5, 6),
    eTag = "edcdc7d39dc218d197c269c8f75ab0f4",
    mimeType = "image/png"
)
