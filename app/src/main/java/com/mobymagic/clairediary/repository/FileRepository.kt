package com.mobymagic.clairediary.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.StorageReference
import com.mobymagic.clairediary.AppExecutors
import com.mobymagic.clairediary.R
import com.mobymagic.clairediary.util.AndroidUtil
import com.mobymagic.clairediary.vo.Resource
import id.zelory.compressor.Compressor
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.math.roundToInt

class FileRepository(
        private val appExecutors: AppExecutors,
        private val androidUtil: AndroidUtil,
        private val storageRef: StorageReference,
        private val compressor: Compressor
) {

    private lateinit var fileWrappers: List<FileWrapper>
    private var fileUploadPointer: Int = 0
    private val fileUploadLiveData = MutableLiveData<Resource<List<FileWrapper>>>()

    fun uploadFiles(fileWrappers: List<FileWrapper>): LiveData<Resource<List<FileWrapper>>> {
        Timber.d("Uploading files: %s", fileWrappers)
        this.fileWrappers = fileWrappers
        fileUploadPointer = 0

        if (fileWrappers.isEmpty()) {
            Timber.e("File wrapper list can't be empty")
            throw IllegalArgumentException("File wrapper list cannot be empty")
        }

        uploadNextFile()
        return fileUploadLiveData
    }

    private fun uploadNextFile() {
        Timber.d("Uploading next file. Pointer: %d", fileUploadPointer)
        // Check if there are any more files to upload
        if (fileUploadPointer >= fileWrappers.size) {
            Timber.i("All files uploaded")
            fileUploadLiveData.value = Resource.success(fileWrappers)
            return
        }

        // Get the next file to upload
        val fileWrapper = fileWrappers[fileUploadPointer]
        Timber.d("File to upload: %s", fileWrapper)
        fileUploadLiveData.value = Resource.loading(androidUtil.getString(R.string.file_uploading))

        // Check if file is an uncompressed photo
        if (!fileWrapper.compressed && fileWrapper.fileType == FileType.PHOTO) {
            Timber.d("File is not compressed and it's a photo")

            appExecutors.diskIO().execute {
                Timber.d("Compressing photo")
                fileWrapper.file = compressor.compressToFile(fileWrapper.file)
                fileWrapper.compressed = true
                Timber.d("Photo compressed")

                appExecutors.mainThread().execute {
                    uploadFile(fileWrapper)
                }
            }
        } else {
            uploadFile(fileWrapper)
        }
    }

    private fun uploadFile(fileWrapper: FileWrapper) {
        Timber.d("Uploading file: %s", fileWrapper)
        // Generate a random name for the new file
        val randomFileStorageName =
                UUID.randomUUID().toString() + getFileExtension(fileWrapper.file)
        Timber.d("Random file storage name: %s", randomFileStorageName)

        val storageFileRef =
                storageRef.child(fileWrapper.fileType.type).child(randomFileStorageName)
        val uploadTask = storageFileRef.putFile(Uri.fromFile(fileWrapper.file))

        // Listen for progress
        uploadTask.addOnProgressListener({ taskSnapshot ->
            Timber.d(
                    "Upload progress, transferred: %d, total: %d", taskSnapshot.bytesTransferred,
                    taskSnapshot.totalByteCount
            )

            var percentUploaded =
                    taskSnapshot.bytesTransferred.toFloat() / taskSnapshot.totalByteCount.toFloat()
            percentUploaded *= 100F

            fileUploadLiveData.value = Resource.loading(
                    androidUtil
                            .getString(R.string.file_uploading_with_percent, percentUploaded.roundToInt())
            )
        })

        // Listen for error
        uploadTask.addOnFailureListener({ e ->
            Timber.e(e, "Error uploading file")
            fileUploadLiveData.value = Resource.error(
                    androidUtil.getString(R.string.file_upload_error),
                    fileWrappers
            )
        })

        // Listen for success
        uploadTask.addOnSuccessListener({ taskSnapshot ->
            Timber.d("File upload success: %s", taskSnapshot)
            Timber.d("Getting file url")

            storageFileRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        Timber.d("Uploaded file uri: %s", uri)
                        fileWrapper.uploaded = true
                        fileWrapper.uploadUrl = uri

                        // Increase file upload pointer
                        fileUploadPointer++
                        // Then try to upload the next file
                        uploadNextFile()
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "Error getting file url")
                        fileUploadLiveData.value = Resource.error(
                                androidUtil.getString(R.string.file_upload_error),
                                fileWrappers
                        )
                    }
        })

    }

    private fun getFileExtension(file: File): String {
        val path = file.absolutePath
        return if (path.contains(".")) {
            path.substring(path.lastIndexOf("."))
        } else {
            ""
        }
    }

    data class FileWrapper(
            var file: File,
            var fileType: FileType,
            var compressed: Boolean = false,
            var uploaded: Boolean = false,
            var uploadUrl: Uri? = null
    )

    enum class FileType(val type: String) {
        PHOTO("photo"), AUDIO("audio")
    }

}