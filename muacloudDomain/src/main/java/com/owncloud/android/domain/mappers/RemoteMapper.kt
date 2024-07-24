

package com.owncloud.android.domain.mappers

interface RemoteMapper<Model, Remote> {

    fun toModel(remote: Remote?): Model?

    fun toRemote(model: Model?): Remote?
}
