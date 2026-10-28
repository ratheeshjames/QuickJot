package com.example.data.backup

import android.content.Context
import android.net.Uri
import com.example.data.local.entities.ChecklistItemEntity
import com.example.data.local.entities.FolderEntity
import com.example.data.local.entities.NoteEntity
import com.example.data.local.models.NoteWithDetails
import com.example.domain.repository.NotesRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

sealed interface RestoreResult {
    data class Success(val notesCount: Int, val foldersCount: Int) : RestoreResult
    data class Error(val message: String) : RestoreResult
}

class BackupManager(
    private val notesRepository: NotesRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun generateBackupDto(): QuickJotBackupDto {
        val notes = notesRepository.getAllActiveNotesForBackup()
        val folders = notesRepository.allFoldersFlow.first()
        val tags = notesRepository.allTagsFlow.first()

        val backupNotes = notes.map { nwd ->
            BackupNoteItemDto(
                id = nwd.note.id,
                title = nwd.note.title,
                body = nwd.note.body,
                colorId = nwd.note.colorId,
                isPinned = nwd.note.isPinned,
                isArchived = nwd.note.isArchived,
                isLocked = nwd.note.isLocked,
                folderId = nwd.note.folderId,
                noteType = nwd.note.noteType,
                createdTimestamp = nwd.note.createdTimestamp,
                modifiedTimestamp = nwd.note.modifiedTimestamp,
                checklistItems = nwd.checklistItems.map {
                    BackupChecklistItemDto(it.text, it.isDone, it.orderIndex)
                },
                tagNames = nwd.tags.map { it.name }
            )
        }

        val backupFolders = folders.map {
            BackupFolderItemDto(it.id, it.name, it.parentId, it.colorId)
        }

        val backupTags = tags.map {
            BackupTagItemDto(it.id, it.name)
        }

        return QuickJotBackupDto(
            notes = backupNotes,
            folders = backupFolders,
            tags = backupTags
        )
    }

    suspend fun exportBackup(
        context: Context,
        uri: Uri,
        password: String? = null
    ): Boolean {
        return try {
            val dto = generateBackupDto()
            val rawJson = json.encodeToString(QuickJotBackupDto.serializer(), dto)
            val bytesToWrite = if (!password.isNullOrBlank()) {
                encrypt(rawJson.toByteArray(Charsets.UTF_8), password)
            } else {
                rawJson.toByteArray(Charsets.UTF_8)
            }

            context.contentResolver.openOutputStream(uri)?.use { os: OutputStream ->
                os.write(bytesToWrite)
                os.flush()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun restoreBackup(
        context: Context,
        uri: Uri,
        password: String? = null
    ): RestoreResult {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { `is`: InputStream ->
                `is`.readBytes()
            } ?: return RestoreResult.Error("Could not read backup file")

            val jsonString = if (!password.isNullOrBlank()) {
                val decrypted = decrypt(bytes, password)
                String(decrypted, Charsets.UTF_8)
            } else {
                try {
                    String(bytes, Charsets.UTF_8)
                } catch (_: Exception) {
                    return RestoreResult.Error("File appears to be encrypted. Please provide password.")
                }
            }

            val dto = json.decodeFromString(QuickJotBackupDto.serializer(), jsonString)

            // Restore folders
            val oldToNewFolderMap = mutableMapOf<Long, Long>()
            dto.folders.forEach { f ->
                val newId = notesRepository.createFolder(f.name, null, f.colorId)
                oldToNewFolderMap[f.id] = newId
            }
            // Update subfolder parent IDs
            dto.folders.filter { it.parentId != null }.forEach { f ->
                val newParent = oldToNewFolderMap[f.parentId]
                val currentNewId = oldToNewFolderMap[f.id]
                if (currentNewId != null && newParent != null) {
                    notesRepository.updateFolder(
                        FolderEntity(id = currentNewId, name = f.name, parentId = newParent, colorId = f.colorId)
                    )
                }
            }

            // Restore tags
            val tagMap = mutableMapOf<String, Long>()
            dto.tags.forEach { t ->
                val id = notesRepository.createTag(t.name)
                tagMap[t.name] = id
            }

            // Restore notes
            dto.notes.forEach { n ->
                val targetFolderId = n.folderId?.let { oldToNewFolderMap[it] }
                val noteEntity = NoteEntity(
                    id = 0,
                    title = n.title,
                    body = n.body,
                    colorId = n.colorId,
                    isPinned = n.isPinned,
                    isArchived = n.isArchived,
                    isLocked = n.isLocked,
                    folderId = targetFolderId,
                    noteType = n.noteType,
                    createdTimestamp = n.createdTimestamp,
                    modifiedTimestamp = n.modifiedTimestamp
                )
                val checkItems = n.checklistItems.map {
                    ChecklistItemEntity(text = it.text, isDone = it.isDone, orderIndex = it.orderIndex, noteId = 0)
                }
                val tagIds = n.tagNames.mapNotNull { tagMap[it] ?: run { null } }

                notesRepository.saveNote(
                    note = noteEntity,
                    checklistItems = checkItems,
                    tagIds = tagIds,
                    createVersionSnapshot = false
                )
            }

            RestoreResult.Success(dto.notes.size, dto.folders.size)
        } catch (e: Exception) {
            RestoreResult.Error(e.message ?: "Failed to restore backup")
        }
    }

    // Markdown & Text Exporters
    fun exportNoteAsMarkdown(noteWithDetails: NoteWithDetails): String {
        val note = noteWithDetails.note
        val sb = StringBuilder()
        if (note.title.isNotBlank()) {
            sb.append("# ").append(note.title).append("\n\n")
        }
        if (noteWithDetails.checklistItems.isNotEmpty()) {
            noteWithDetails.checklistItems.sortedBy { it.orderIndex }.forEach { item ->
                val mark = if (item.isDone) "[x]" else "[ ]"
                sb.append("- ").append(mark).append(" ").append(item.text).append("\n")
            }
            sb.append("\n")
        }
        if (note.body.isNotBlank()) {
            sb.append(note.body).append("\n\n")
        }
        if (noteWithDetails.tags.isNotEmpty()) {
            val tagsStr = noteWithDetails.tags.joinToString(" ") { "#${it.name}" }
            sb.append(tagsStr).append("\n")
        }
        return sb.toString()
    }

    fun exportNoteAsText(noteWithDetails: NoteWithDetails): String {
        val note = noteWithDetails.note
        val sb = StringBuilder()
        if (note.title.isNotBlank()) {
            sb.append(note.title).append("\n\n")
        }
        if (noteWithDetails.checklistItems.isNotEmpty()) {
            noteWithDetails.checklistItems.sortedBy { it.orderIndex }.forEach { item ->
                val mark = if (item.isDone) "✓" else "○"
                sb.append(mark).append(" ").append(item.text).append("\n")
            }
            sb.append("\n")
        }
        if (note.body.isNotBlank()) {
            sb.append(note.body)
        }
        return sb.toString()
    }

    // AES-256-GCM + PBKDF2 encryption
    private fun encrypt(data: ByteArray, password: String): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16).also { random.nextBytes(it) }
        val iv = ByteArray(12).also { random.nextBytes(it) }

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(data)

        // Header: [SALT 16B] [IV 12B] [CIPHERTEXT]
        return salt + iv + ciphertext
    }

    private fun decrypt(encrypted: ByteArray, password: String): ByteArray {
        if (encrypted.size < 28) throw IllegalArgumentException("Encrypted data is corrupted or too short")
        val salt = encrypted.copyOfRange(0, 16)
        val iv = encrypted.copyOfRange(16, 28)
        val ciphertext = encrypted.copyOfRange(28, encrypted.size)

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        return cipher.doFinal(ciphertext)
    }
}
