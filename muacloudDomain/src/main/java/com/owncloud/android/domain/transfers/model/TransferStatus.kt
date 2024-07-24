

package com.owncloud.android.domain.transfers.model

enum class TransferStatus constructor(val value: Int) {
    TRANSFER_QUEUED(value = 0),
    TRANSFER_IN_PROGRESS(value = 1),
    TRANSFER_FAILED(value = 2),
    TRANSFER_SUCCEEDED(value = 3);

    companion object {
        fun fromValue(value: Int): TransferStatus =
            when (value) {
                0 -> TRANSFER_QUEUED
                1 -> TRANSFER_IN_PROGRESS
                2 -> TRANSFER_FAILED
                else -> TRANSFER_SUCCEEDED
            }
    }
}
