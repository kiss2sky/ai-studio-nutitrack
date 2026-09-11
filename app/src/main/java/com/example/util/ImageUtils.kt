package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Log
import java.io.InputStream

object ImageUtils {
    private const val TAG = "ImageUtils"

    /**
     * Safely decodes a Uri to a software-backed, memory-safe Bitmap (max 1024x1024).
     * Prevents OOM and Hardware Bitmap getPixel crashes.
     */
    fun decodeUriToSafeBitmap(context: Context, uri: Uri, maxDimension: Int = 1024): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    val origW = info.size.width
                    val origH = info.size.height
                    if (origW > maxDimension || origH > maxDimension) {
                        val scale = maxOf(origW, origH).toFloat() / maxDimension
                        val targetW = (origW / scale).toInt().coerceAtLeast(1)
                        val targetH = (origH / scale).toInt().coerceAtLeast(1)
                        decoder.setTargetSize(targetW, targetH)
                    }
                }
            } else {
                decodeStreamWithSampling(context, uri, maxDimension)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to decode bitmap with primary method, falling back to sampled decode", e)
            try {
                decodeStreamWithSampling(context, uri, maxDimension)
            } catch (t: Throwable) {
                Log.e(TAG, "All bitmap decoding attempts failed", t)
                null
            }
        }
    }

    private fun decodeStreamWithSampling(context: Context, uri: Uri, maxDimension: Int): Bitmap? {
        return try {
            // First decode bounds only to compute sample size
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            var inSampleSize = 1
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            var sampledBitmap: Bitmap? = null
            context.contentResolver.openInputStream(uri)?.use { stream ->
                sampledBitmap = BitmapFactory.decodeStream(stream, null, decodeOptions)
            }

            if (sampledBitmap == null) return null

            // Correct EXIF orientation if present
            var rotationAngle = 0
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    rotationAngle = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                }
            } catch (e: Exception) {
                // Ignore exif error
            }

            if (rotationAngle != 0) {
                val matrix = Matrix().apply { postRotate(rotationAngle.toFloat()) }
                val rotated = Bitmap.createBitmap(
                    sampledBitmap!!, 0, 0,
                    sampledBitmap!!.width, sampledBitmap!!.height,
                    matrix, true
                )
                rotated
            } else {
                sampledBitmap
            }
        } catch (e: Throwable) {
            Log.e(TAG, "decodeStreamWithSampling error", e)
            null
        }
    }

    /**
     * Guarantees that the returned Bitmap is in software memory and safe for getPixel/compression
     */
    fun ensureSoftwareBitmap(bitmap: Bitmap): Bitmap {
        return try {
            if (bitmap.isRecycled) {
                return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
            } else {
                bitmap
            }
        } catch (e: Throwable) {
            bitmap
        }
    }
}
