
package com.owncloud.android.data.extensions

import java.io.File
import java.io.IOException


fun File.moveRecursively(
    target: File,
    overwrite: Boolean = false,
    onError: (File, IOException) -> OnErrorAction = { _, exception -> throw exception }
): Boolean {
    if (!exists()) {
        return onError(this, NoSuchFileException(file = this, reason = "The source file doesn't exist.")) !=
                OnErrorAction.TERMINATE
    }
    try {

        for (src in walkTopDown().onFail { f, e -> if (onError(f, e) == OnErrorAction.TERMINATE) throw TerminateException(f) }) {
            if (!src.exists()) {
                if (onError(src, NoSuchFileException(file = src, reason = "The source file doesn't exist.")) ==
                    OnErrorAction.TERMINATE
                )
                    return false
            } else {
                val relPath = src.toRelativeString(this)
                val dstFile = File(target, relPath)
                if (dstFile.exists() && !(src.isDirectory && dstFile.isDirectory)) {
                    val stillExists = if (!overwrite) true else {
                        if (dstFile.isDirectory)
                            !dstFile.deleteRecursively()
                        else
                            !dstFile.delete()
                    }

                    if (stillExists) {
                        if (onError(
                                dstFile, FileAlreadyExistsException(
                                    file = src,
                                    other = dstFile,
                                    reason = "The destination file already exists."
                                )
                            ) == OnErrorAction.TERMINATE
                        )
                            return false

                        continue
                    }
                }

                if (src.isDirectory) {
                    dstFile.mkdirs()
                } else {
                    try {
                        if (src.copyTo(dstFile, overwrite).length() != src.length()) {
                            if (onError(
                                    src,
                                    IOException("Source file wasn't copied completely, length of destination file differs.")
                                ) == OnErrorAction.TERMINATE
                            )
                                return false
                        } else {
                            src.delete()
                        }
                    } catch (e: IOException) {
                        src.delete()
                        dstFile.delete()
                    }
                }
            }
        }

        return true

    } catch (e: TerminateException) {
        return false
    }
}
private class TerminateException(file: File) : FileSystemException(file)
