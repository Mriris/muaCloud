

package com.owncloud.android.extensions

import android.database.Cursor

fun Cursor.getStringFromColumnOrThrow(
    columnName: String
): String? = getString(getColumnIndexOrThrow(columnName))

fun Cursor.getStringFromColumnOrEmpty(
    columnName: String
): String = getColumnIndex(columnName).takeUnless { it < 0 }?.let { getString(it) }.orEmpty()

fun Cursor.getIntFromColumnOrThrow(
    columnName: String
): Int = getInt(getColumnIndexOrThrow(columnName))

fun Cursor.getLongFromColumnOrThrow(
    columnName: String
): Long = getLong(getColumnIndexOrThrow(columnName))
