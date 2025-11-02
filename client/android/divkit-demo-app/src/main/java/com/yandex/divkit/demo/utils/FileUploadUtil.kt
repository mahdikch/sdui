package com.yandex.divkit.demo.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Utility class for uploading recording files to the police service.
 * 
 * Usage examples in JSON:
 * 
 * 1. Upload only:
 * {
 *   "actions": [
 *     {
 *       "log_id": "upload_recording_files",
 *       "url": "div-action://upload_recording?id=12345"
 *     }
 *   ]
 * }
 * 
 * 2. Upload and call service (with all call_service parameters):
 * {
 *   "actions": [
 *     {
 *       "log_id": "upload_and_call_service",
 *       "url": "div-action://upload_and_call_service?id=12345&path=processRecordings&username=@{username}&additionalParam=@{someValue}"
 *     }
 *   ]
 * }
 * 
 * Both actions will:
 * 1. Find all recording files with ID "12345" in the Music directory (MP4 format)
 * 2. Upload each file to https://aminafkar.police.ir/service/restclientadmin/rest-service?serviceId=1108011523
 * 3. Save the uploaded file IDs to database with key "12345uploaded_recorded"
 * 4. Database value format: "{11080111383,11080111382,11080111383}"
 * 
 * The upload_and_call_service action additionally:
 * 5. Processes ALL query parameters (just like call_service)
 * 6. Supports variable expressions like @{username}, @{someValue}
 * 7. Calls the service with ALL parameters + uploaded file IDs
 * 8. Service receives: path=processRecordings, username=actualValue, additionalParam=actualValue, 12345uploaded_recorded={11080111383,11080111382,11080111383}
 */
data class UploadResponse(
    val success: Boolean,
    val fileId: String?,
    val error: String?
)

class FileUploadUtil {
    
    companion object {
        private const val TAG = "FileUploadUtil"
        private const val UPLOAD_URL = "https://aminafkar.police.ir/service/restclientadmin/rest-service?serviceId=1108011523"
        
        // Create OkHttpClient that bypasses SSL certificate validation
        private fun createUnsafeOkHttpClient(): OkHttpClient {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            val sslSocketFactory = sslContext.socketFactory

            return OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .build()
        }
        
        suspend fun

                uploadFile(
            context: Context,
            file: File,
            token: String
        ): UploadResponse = withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting file upload: ${file.absolutePath}")
                Log.d(TAG, "File size: ${file.length()} bytes")
                Log.d(TAG, "Token: $token")
                
                val client = createUnsafeOkHttpClient()
                
                // Create multipart body
                val ksunServiceParam = " {\"body\":{},\"header\":{},\"form\":{},\"queryParam\":{}}"
                val quotedKsunServiceParam = "\"$ksunServiceParam\""
                val quotedTicket = "\"$token\""
                Log.d(TAG, "ksunServiceParam: $ksunServiceParam")
                Log.d(TAG, "quoted ksunServiceParam: $quotedKsunServiceParam")
                Log.d(TAG, "ticket: $token")
                Log.d(TAG, "quoted ticket: $quotedTicket")
                
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file", 
                        file.name,
                        file.asRequestBody("video/mp4".toMediaType())
                    )
                    .addFormDataPart(
                        "ksunServiceParam",
                        ksunServiceParam
                    )
                    .addFormDataPart(
                        "ticket",
                        quotedTicket
                    )
                    .build()
                
                val request = Request.Builder()
                    .url(UPLOAD_URL)
                    .post(requestBody)
                    .addHeader("Cookie", "SESSIONID=C8DFE0265E30D208EA18DE9FF8CFD78B; SID=942f056d-a115-11f0-a483-00505691334a")
                    .build()
                
                Log.d(TAG, "Sending request to: $UPLOAD_URL")
                Log.d(TAG, "Request headers: ${request.headers}")
                Log.d(TAG, "Request body type: ${requestBody.contentType()}")
                
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful && responseBody != null) {
                    // Parse JSON response to extract file ID
                    val fileId = parseFileIdFromResponse(responseBody)
                    if (fileId != null) {
                        Log.d(TAG, "Upload successful. File ID: $fileId")
                        UploadResponse(true, fileId, null)
                    } else {
                        Log.e(TAG, "Failed to parse file ID from response")
                        UploadResponse(false, null, "Failed to parse file ID from response")
                    }
                } else {
                    Log.e(TAG, "Upload failed. Response code: ${response.code}")
                    UploadResponse(false, null, "Upload failed with code: ${response.code}")
                }
                
            } catch (e: IOException) {
                Log.e(TAG, "Network error during upload", e)
                UploadResponse(false, null, "Network error: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during upload", e)
                UploadResponse(false, null, "Unexpected error: ${e.message}")
            }
        }
        
        private fun parseFileIdFromResponse(responseBody: String): String? {
            return try {
                // Simple JSON parsing to extract the file ID
                // Looking for pattern: "id": "11080111383"
                val idPattern = """"id":\s*"([^"]+)"""".toRegex()
                val matchResult = idPattern.find(responseBody)
                matchResult?.groupValues?.get(1)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing file ID from response", e)
                null
            }
        }
        
        fun findRecordingFiles(context: Context, recordingId: String): List<File> {
            Log.d(TAG, "Searching for recording files with ID: $recordingId")
            
            val files = mutableListOf<File>()
            val musicDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC)
            
            if (musicDir?.exists() == true) {
                Log.d(TAG, "Searching in directory: ${musicDir.absolutePath}")
                
                // List all files in directory for debugging
                val allFiles = musicDir.listFiles()
                Log.d(TAG, "All files in directory (${allFiles?.size ?: 0}):")
                allFiles?.forEach { file ->
                    Log.d(TAG, "  - ${file.name} (${file.length()} bytes)")
                }
                
                musicDir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        // Look for files with recording ID in the name
                        val fileName = file.name
                        Log.d(TAG, "Checking file: $fileName")
                        
                        // Check for exact pattern: recorded_audio_{recordingId}_
                        if (fileName.contains("recorded_audio_${recordingId}_")) {
                            Log.d(TAG, "Found matching file with exact pattern: $fileName")
                            files.add(file)
                        }
                        // Also check for files that start with recorded_audio and contain the recordingId
                        else if (fileName.startsWith("recorded_audio_") && fileName.contains(recordingId)) {
                            Log.d(TAG, "Found matching file with flexible pattern: $fileName")
                            files.add(file)
                        }
                    }
                }
            } else {
                Log.w(TAG, "Music directory does not exist: ${musicDir?.absolutePath}")
            }
            
            Log.d(TAG, "Found ${files.size} files for recording ID: $recordingId")
            
            // If no files found with recording ID, try to find the most recent recording file
            if (files.isEmpty() && recordingId != null && musicDir?.exists() == true) {
                Log.d(TAG, "No files found with recording ID, looking for most recent recording file")
                
                val allRecordingFiles = mutableListOf<File>()
                musicDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.name.startsWith("recorded_audio_") && file.name.endsWith(".mp4")) {
                        allRecordingFiles.add(file)
                    }
                }
                
                // Sort by last modified time (most recent first)
                allRecordingFiles.sortByDescending { it.lastModified() }
                
                if (allRecordingFiles.isNotEmpty()) {
                    val mostRecentFile = allRecordingFiles.first()
                    Log.d(TAG, "Found most recent recording file: ${mostRecentFile.name} (${mostRecentFile.length()} bytes)")
                    Log.d(TAG, "Last modified: ${java.util.Date(mostRecentFile.lastModified())}")
                    files.add(mostRecentFile)
                }
            }
            
            files.forEach { file ->
                Log.d(TAG, "  - ${file.name} (${file.length()} bytes)")
            }
            return files
        }
    }
}
