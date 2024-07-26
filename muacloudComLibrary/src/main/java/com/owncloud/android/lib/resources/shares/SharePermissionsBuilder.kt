
package com.owncloud.android.lib.resources.shares


class SharePermissionsBuilder {

        private var permissions = RemoteShare.READ_PERMISSION_FLAG    // READ is minimum permission


    fun setSharePermission(enabled: Boolean): SharePermissionsBuilder {
        updatePermission(RemoteShare.SHARE_PERMISSION_FLAG, enabled)
        return this
    }


    fun setUpdatePermission(enabled: Boolean): SharePermissionsBuilder {
        updatePermission(RemoteShare.UPDATE_PERMISSION_FLAG, enabled)
        return this
    }


    fun setCreatePermission(enabled: Boolean): SharePermissionsBuilder {
        updatePermission(RemoteShare.CREATE_PERMISSION_FLAG, enabled)
        return this
    }


    fun setDeletePermission(enabled: Boolean): SharePermissionsBuilder {
        updatePermission(RemoteShare.DELETE_PERMISSION_FLAG, enabled)
        return this
    }


    private fun updatePermission(permissionsFlag: Int, enable: Boolean) {
        if (enable) {

            permissions = permissions or permissionsFlag
        } else {

            permissions = permissions and permissionsFlag.inv()
        }
    }


    fun build(): Int = permissions
}
