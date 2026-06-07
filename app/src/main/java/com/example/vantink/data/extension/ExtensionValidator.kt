package com.example.vantink.data.extension

import com.example.vantink.domain.model.Extension
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

interface ExtensionValidator {
    suspend fun validateExtension(extension: Extension): ValidationResult
    suspend fun validateExtensions(extensions: List<Extension>): List<ValidationResult>
}

data class ValidationResult(
    val extension: Extension,
    val isValid: Boolean,
    val errorMessage: String? = null,
    val lastChecked: Long = System.currentTimeMillis()
)

class ExtensionValidatorImpl(
    private val client: OkHttpClient
) : ExtensionValidator {

    override suspend fun validateExtension(extension: Extension): ValidationResult {
        return try {
            // Check if icon URL is accessible
            if (extension.iconUrl.isNotEmpty()) {
                val iconValid = checkUrl(extension.iconUrl)
                if (!iconValid) {
                    return ValidationResult(
                        extension = extension,
                        isValid = false,
                        errorMessage = "Icon URL not accessible"
                    )
                }
            }

            // Check if APK URL is accessible
            if (extension.apkUrl.isNotEmpty()) {
                val apkValid = checkUrl(extension.apkUrl)
                if (!apkValid) {
                    return ValidationResult(
                        extension = extension,
                        isValid = false,
                        errorMessage = "APK URL not accessible"
                    )
                }
            }

            // Check if base URL is valid (for non-directory extensions)
            if (extension.baseUrl.isNotEmpty() && !extension.baseUrl.contains("://")) {
                return ValidationResult(
                    extension = extension,
                    isValid = false,
                    errorMessage = "Invalid base URL format"
                )
            }

            ValidationResult(
                extension = extension,
                isValid = true
            )
        } catch (e: Exception) {
            ValidationResult(
                extension = extension,
                isValid = false,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }

    override suspend fun validateExtensions(extensions: List<Extension>): List<ValidationResult> {
        return extensions.map { validateExtension(it) }
    }

    private fun checkUrl(url: String): Boolean {
        return try {
            val request = Request.Builder()
                .url(url)
                .head()
                .build()

            val timeoutClient = client.newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()

            timeoutClient.newCall(request).execute().use { response ->
                response.code in 200..399 // Accept 2xx and 3xx status codes
            }
        } catch (e: Exception) {
            false
        }
    }
}

