

package com.owncloud.android.utils

import androidx.annotation.StringDef
const val NO_MDM_RESTRICTION_YET = "NO_MDM_RESTRICTION_YET"

const val CONFIGURATION_LOCK_DELAY_TIME = "lock_delay_time_configuration"
const val CONFIGURATION_SERVER_URL = "server_url_configuration"
const val CONFIGURATION_SERVER_URL_INPUT_VISIBILITY = "server_url_input_visibility_configuration"
const val CONFIGURATION_ALLOW_SCREENSHOTS = "allow_screenshots_configuration"
const val CONFIGURATION_OAUTH2_OPEN_ID_SCOPE = "oauth2_open_id_scope"
const val CONFIGURATION_OAUTH2_OPEN_ID_PROMPT = "oauth2_open_id_prompt"
const val CONFIGURATION_DEVICE_PROTECTION = "device_protection"
const val CONFIGURATION_REDACT_AUTH_HEADER_LOGS = "redact_auth_header_logs_configuration"
const val CONFIGURATION_SEND_LOGIN_HINT_AND_USER = "send_login_hint_and_user_configuration"

@StringDef(
    NO_MDM_RESTRICTION_YET,
    CONFIGURATION_LOCK_DELAY_TIME,
    CONFIGURATION_SERVER_URL,
    CONFIGURATION_SERVER_URL_INPUT_VISIBILITY,
    CONFIGURATION_ALLOW_SCREENSHOTS,
    CONFIGURATION_OAUTH2_OPEN_ID_SCOPE,
    CONFIGURATION_OAUTH2_OPEN_ID_PROMPT,
    CONFIGURATION_DEVICE_PROTECTION,
    CONFIGURATION_REDACT_AUTH_HEADER_LOGS,
    CONFIGURATION_SEND_LOGIN_HINT_AND_USER,
)
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class MDMConfigurations
