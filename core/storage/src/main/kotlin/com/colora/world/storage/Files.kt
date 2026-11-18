package com.colora.world.storage

import java.io.File
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.util.UUID

internal object Files2 {
    const val TMP_PREFIX = ".tmp-"

    /** temp file in the same directory → write → fsync → atomic rename → fsync directory. */
    fun writeAtomic(target: File, bytes: ByteArray) {
        val dir = target.parentFile
        dir.mkdirs()
        val tmp = File(dir, TMP_PREFIX + target.name + "-" + UUID.randomUUID())
        try {
            FileOutputStream(tmp).use { out ->
                out.write(bytes)
                out.flush()
                out.fd.sync()
            }
            try {
                Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } catch (e: AtomicMoveNotSupportedException) {
                throw IllegalStateException("filesystem does not support atomic rename", e)
            }
            syncDir(dir)
        } finally {
            if (tmp.exists()) tmp.delete()
        }
    }

    fun syncDir(dir: File) {
        // Directory fsync makes the rename durable on Linux/Android; not all platforms allow it.
        try { FileChannel.open(dir.toPath(), StandardOpenOption.READ).use { it.force(true) } } catch (_: Exception) {}
    }

    fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    fun sha256(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { inp ->
            val buf = ByteArray(64 * 1024)
            while (true) { val n = inp.read(buf); if (n < 0) break; md.update(buf, 0, n) }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    fun deleteTree(f: File) { if (f.exists()) f.walkBottomUp().forEach { it.delete() } }

    private val ID = Regex("^[A-Za-z0-9_-]{1,64}$")
    fun requireId(id: String) = require(ID.matches(id)) { "invalid id: $id" }
}
