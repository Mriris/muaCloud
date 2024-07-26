
package com.owncloud.android.lib.resources.webfinger.responses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WebFingerResponse(
    val subject: String,
    val links: List<LinkItem>?
)

@JsonClass(generateAdapter = true)
data class LinkItem(
    val rel: String,
    val href: String,
)
