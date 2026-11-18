package com.colora.world.storage

import kotlinx.serialization.Serializable

/** Kinds of child-made documents. Settings, PIN and entitlements live elsewhere and are never in this store. */
enum class DocKind { ARTWORK, SCENE, CHARACTER, BOOK, STORY_PROGRESS, LAB }

@Serializable
data class ChildProfile(
    val id: String,
    val avatarId: String,
    val localAlias: String? = null,
    val skillMode: String = "explorer",
    val locale: String = "en",
    val createdAt: Long,
)

/** Immutable revision header. The payload digest is the validity marker. */
@Serializable
data class RevisionMeta(
    val revisionId: String,
    val documentId: String,
    val profileId: String,
    val kind: DocKind,
    val sequence: Long,
    val parentRevisionId: String? = null,
    val sourceId: String? = null,
    val sourceVersion: Int? = null,
    val createdAt: Long,
    val sizeBytes: Long,
    val sha256: String,
)

/** Current pointer of a document; moves only after the revision is durable and valid. */
@Serializable
data class DocPointer(
    val documentId: String,
    val profileId: String,
    val kind: DocKind,
    val currentRevisionId: String,
    val sequence: Long,
    val favorite: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class TrashRecord(val documentId: String, val profileId: String, val deletedAt: Long, val kind: DocKind)

data class RecoveryReport(
    val tempFilesRemoved: Int,
    val pointersRepaired: List<String>,
    val invalidRevisions: List<String>,
    val trashPurged: Int,
)
