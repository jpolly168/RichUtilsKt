@file:JvmName("RichUtils")
@file:JvmMultifileClass

package pyxis.uzuki.live.richutilskt.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


/**
 * Save Bitmap to file
 *
 * @param[bitmap] to save
 * @return path of file
 */
fun Context.saveBitmapToFile(bitmap: Bitmap): String? = try {
    saveBitmapToFile(this, bitmap)?.absolutePath
} catch (e: Exception) {
    ""
}

/**
 * get bitmap from filePath
 * @return Bitmap object
 */
fun String.getBitmap(): Bitmap? = if (TextUtils.isEmpty(this)) null else BitmapFactory.decodeFile(this)

/**
 * Make bitmap corner
 * @return Bitmap object
 */
fun Bitmap.toRoundCorner(radius: Float): Bitmap? {
    val width = this.width
    val height = this.height
    val bitmap = Bitmap.createBitmap(width, height, this.config)
    val paint = Paint()
    val canvas = Canvas(bitmap)
    val rect = Rect(0, 0, width, height)

    paint.isAntiAlias = true
    canvas.drawRoundRect(RectF(rect), radius, radius, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, rect, rect, paint)

    this.recycle()
    return bitmap
}

private fun saveBitmapToFile(context: Context, bitmap: Bitmap): File? {
    try {
        val photo = getOutputMediaFile(context)
        val out = FileOutputStream(photo)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.close()

        return photo
    } catch (exception: FileNotFoundException) {
        Log.e("FileNotFoundException", exception.message)
    } catch (exception: IOException) {
        Log.e("IOException", exception.message)
    }

    return null
}

private fun getOutputMediaFile(context: Context): File {
    val picName = UUID.randomUUID().toString().replace("-".toRegex(), "") + ".jpg"

    val folder = context.getExternalFilesDir(null)
    if (!folder.isDirectory) {
        folder.mkdirs()
    }

    return File(folder, picName)
}

/**
 * Convert Drawable to Bitmap in safe way
 *
 * @param[drawable] to convert
 * @return Bitmap object
 */
fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }

    var width = drawable.intrinsicWidth
    width = if (width > 0) width else 1
    var height = drawable.intrinsicHeight
    height = if (height > 0) height else 1

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}

/**
 * Request MediaScanning
 *
 * @param[url] to request
 */
fun Context.requestMediaScanner(url: String) {
    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    val contentUri = Uri.fromFile(File(url))
    mediaScanIntent.data = contentUri
    this.sendBroadcast(mediaScanIntent)
}

/**
 * Resizing image
 *
 * @param[width] desire width
 * @param[height] desire height
 * @param[mode] Resizing mode
 * @param[isExcludeAlpha] true - exclude alpha (copy as RGB_565) false - include alpha (copy as ARGB_888)
 */
@JvmOverloads fun Bitmap.resize(width: Int, height: Int, mode: ResizeMode = ResizeMode.AUTOMATIC, isExcludeAlpha: Boolean = false): Bitmap {
    var mWidth = width
    var mHeight = height
    var mMode = mode
    val sourceWidth = this.width
    val sourceHeight = this.height

    if (mode == ResizeMode.AUTOMATIC) {
        mMode = calculateResizeMode(sourceWidth, sourceHeight)
    }

    if (mMode == ResizeMode.FIT_TO_WIDTH) {
        mHeight = calculateHeight(sourceWidth, sourceHeight, width)
    } else if (mMode == ResizeMode.FIT_TO_HEIGHT) {
        mWidth = calculateWidth(sourceWidth, sourceHeight, height)
    }

    val config = if (isExcludeAlpha) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
    return Bitmap.createScaledBitmap(this, mWidth, mHeight, true).copy(config, true)
}

private fun calculateResizeMode(width: Int, height: Int): ResizeMode {
    if (ImageOrientation.getOrientation(width, height) === ImageOrientation.LANDSCAPE) {
        return ResizeMode.FIT_TO_WIDTH
    } else {
        return ResizeMode.FIT_TO_HEIGHT
    }
}

private fun calculateWidth(originalWidth: Int, originalHeight: Int, height: Int): Int {
    return Math.ceil(originalWidth / (originalHeight.toDouble() / height)).toInt()
}

private fun calculateHeight(originalWidth: Int, originalHeight: Int, width: Int): Int {
    return Math.ceil(originalHeight / (originalWidth.toDouble() / width)).toInt()
}

enum class ResizeMode {
    AUTOMATIC,
    FIT_TO_WIDTH,
    FIT_TO_HEIGHT,
    FIT_EXACT
}

private enum class ImageOrientation {
    PORTRAIT,
    LANDSCAPE;

    companion object {

        fun getOrientation(width: Int, height: Int): ImageOrientation {
            if (width >= height) {
                return ImageOrientation.LANDSCAPE
            } else {
                return ImageOrientation.PORTRAIT
            }
        }
    }

}