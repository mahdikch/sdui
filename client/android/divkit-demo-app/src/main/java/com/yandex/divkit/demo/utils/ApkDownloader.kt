package com.yandex.divkit.demo.utils

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.FileProvider
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.yandex.divkit.demo.R
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.GZIPInputStream

class ApkDownloader(private val context: Context) {

    private var dialog: AppCompatDialog? = null
    private var progressBar: LinearProgressIndicator? = null
    private var statusView: TextView? = null
    private var percentView: TextView? = null
    private var cancelButton: Button? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var closedByUser = false
    private var downloadFinished = false
    private val cancelled = AtomicBoolean(false)
    private var downloadFuture: Future<*>? = null

    fun downloadAndInstall(apkUrl: String) {
        if (apkUrl.isBlank()) {
            Toast.makeText(context, "آدرس به‌روزرسانی نامعتبر است", Toast.LENGTH_SHORT).show()
            return
        }

        val existing = apkDestinationFile(context)
        if (pendingInstallAfterPermission && isInstallableApk(context, existing)) {
            cancelActiveDownload(context, deleteFile = false)
            closedByUser = false
            cancelled.set(false)
            downloadFinished = true
            downloadInProgress = false
            activeInstance = this
            showSheet()
            updateProgress(100, "فایل آماده است. در حال بررسی اجازه نصب...")
            mainHandler.post {
                cancelButton?.text = "بستن"
                launchInstaller(existing)
            }
            return
        }

        cancelActiveDownload(context, deleteFile = true)
        closedByUser = false
        cancelled.set(false)
        downloadFinished = false
        pendingInstallAfterPermission = false
        downloadInProgress = true
        activeInstance = this
        showSheet()
        setIndeterminate("در حال شروع دانلود...")

        downloadFuture = executor.submit {
            try {
                downloadToFile(apkUrl)
            } catch (e: Exception) {
                if (!cancelled.get() && !closedByUser) {
                    Log.e(TAG, "Download failed", e)
                    fail("دانلود ناموفق: ${e.message ?: "خطای شبکه"}")
                }
            }
        }
    }

    private fun downloadToFile(apkUrl: String) {
        val destination = apkDestinationFile(context)
        val partial = File(destination.parentFile, "$APK_FILE_NAME.part")
        destination.parentFile?.mkdirs()
        if (destination.exists()) destination.delete()
        if (partial.exists()) partial.delete()

        var connection: HttpURLConnection? = null
        try {
            var currentUrl = apkUrl
            var redirects = 0
            while (true) {
                if (cancelled.get() || closedByUser) return
                connection = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    connectTimeout = 60_000
                    readTimeout = 600_000
                    requestMethod = "GET"
                    setRequestProperty("Accept", "*/*")
                    setRequestProperty("Accept-Encoding", "identity")
                    setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Mobile Safari/537.36"
                    )
                    doInput = true
                }
                val code = connection.responseCode
                Log.d(TAG, "Download response $code for $currentUrl")
                if (code in 301..308) {
                    val location = connection.getHeaderField("Location")
                    connection.disconnect()
                    if (location.isNullOrBlank() || ++redirects > 5) {
                        fail("دانلود ناموفق (ریدایرکت نامعتبر)")
                        return
                    }
                    currentUrl = if (location.startsWith("http")) {
                        location
                    } else {
                        URL(URL(currentUrl), location).toString()
                    }
                    continue
                }
                if (code !in 200..299) {
                    fail("دانلود ناموفق (کد $code)")
                    return
                }
                break
            }

            val conn = connection ?: run {
                fail("اتصال برقرار نشد")
                return
            }

            val declaredLength = conn.contentLengthLong
            val encoding = conn.contentEncoding.orEmpty()
            val contentType = conn.contentType.orEmpty()
            Log.d(
                TAG,
                "contentLength=$declaredLength contentType=$contentType encoding=$encoding"
            )

            var raw: InputStream = BufferedInputStream(conn.inputStream)
            // Some servers ignore Accept-Encoding and still send gzip.
            if (encoding.contains("gzip", ignoreCase = true)) {
                raw = GZIPInputStream(raw)
            }

            var downloaded = 0L
            var lastPercent = -1
            FileOutputStream(partial).use { output ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    if (cancelled.get() || closedByUser) {
                        partial.delete()
                        return
                    }
                    val read = raw.read(buffer)
                    if (read < 0) break
                    output.write(buffer, 0, read)
                    downloaded += read
                    if (declaredLength > 0L && !encoding.contains("gzip", ignoreCase = true)) {
                        val percent =
                            ((downloaded * 100L) / declaredLength).toInt().coerceIn(0, 100)
                        if (percent != lastPercent) {
                            lastPercent = percent
                            updateProgress(percent, "در حال دانلود به‌روزرسانی...")
                        }
                    } else if (downloaded % (512 * 1024) == 0L) {
                        setIndeterminate("در حال دانلود... (${downloaded / (1024 * 1024)} MB)")
                    }
                }
                output.flush()
                try {
                    output.fd.sync()
                } catch (_: Exception) {
                }
            }
            try {
                raw.close()
            } catch (_: Exception) {
            }

            if (cancelled.get() || closedByUser) {
                partial.delete()
                return
            }

            // Truncated download is the most common cause of "problem parsing the package".
            if (declaredLength > 0L &&
                !encoding.contains("gzip", ignoreCase = true) &&
                downloaded != declaredLength
            ) {
                partial.delete()
                fail("دانلود ناقص است (${downloaded / 1024}KB از ${declaredLength / 1024}KB)")
                return
            }

            if (partial.length() < MIN_APK_BYTES) {
                partial.delete()
                fail("فایل دانلودشده خیلی کوچک است (${partial.length()} بایت)")
                return
            }

            // If body was gzip without header, detect and unwrap.
            if (isGzipFile(partial)) {
                val ungzipped = File(destination.parentFile, "$APK_FILE_NAME.ungzip")
                try {
                    GZIPInputStream(partial.inputStream()).use { gis ->
                        FileOutputStream(ungzipped).use { out ->
                            gis.copyTo(out)
                            out.fd.sync()
                        }
                    }
                    partial.delete()
                    if (!ungzipped.renameTo(partial) && ungzipped.exists()) {
                        ungzipped.copyTo(partial, overwrite = true)
                        ungzipped.delete()
                    }
                } catch (e: Exception) {
                    partial.delete()
                    ungzipped.delete()
                    fail("خطا در باز کردن فایل فشرده: ${e.message}")
                    return
                }
            }

            if (destination.exists()) destination.delete()
            if (!partial.renameTo(destination)) {
                partial.copyTo(destination, overwrite = true)
                partial.delete()
            }

            Log.d(TAG, "Saved APK " + destination.absolutePath + " size=" + destination.length())

            if (!isInstallableApk(context, destination)) {
                val peek = peekHeaderHex(destination)
                Log.e(TAG, "APK validation failed. header=" + peek + " size=" + destination.length())
                destination.delete()
                fail("فایل APK قابل نصب نیست (خراب یا ناقص)")
                return
            }

            downloadFinished = true
            downloadInProgress = false
            updateProgress(100, "دانلود کامل شد. در حال نصب...")
            mainHandler.post {
                cancelButton?.text = "بستن"
                launchInstaller(destination)
            }
        } finally {
            try {
                connection?.disconnect()
            } catch (_: Exception) {
            }
        }
    }

    private fun launchInstaller(apkFile: File) {
        if (!isInstallableApk(context, apkFile)) {
            pendingInstallAfterPermission = false
            fail("فایل نصب معتبر نیست؛ دوباره دانلود کنید")
            return
        }
        if (!hasInstallPermission()) {
            pendingInstallAfterPermission = true
            activeInstance = this
            downloadFinished = true
            openInstallPermissionSettings()
            updateProgress(
                100,
                "اجازه نصب را فعال کنید و به برنامه برگردید — نیازی به دانلود مجدد نیست"
            )
            mainHandler.post {
                cancelButton?.text = "بستن"
                Toast.makeText(
                    context,
                    "اجازه نصب را روشن کنید، سپس به برنامه برگردید تا نصب ادامه یابد",
                    Toast.LENGTH_LONG
                ).show()
            }
            return
        }

        pendingInstallAfterPermission = false
        updateProgress(100, "در حال آماده‌سازی نصب‌کننده...")
        // Stream APK into PackageInstaller ourselves — FileProvider content URIs
        // often cause "problem parsing the package" on OEM installers even when
        // getPackageArchiveInfo succeeds on the same file.
        executor.execute {
            try {
                installWithPackageSession(apkFile)
            } catch (e: Exception) {
                Log.e(TAG, "PackageInstaller session failed, trying legacy Intent", e)
                mainHandler.post {
                    try {
                        installWithLegacyIntent(apkFile)
                    } catch (e2: Exception) {
                        Log.e(TAG, "Legacy install also failed", e2)
                        fail("خطا در نصب: ${e2.message ?: e.message}")
                    }
                }
            }
        }
    }

    private fun installWithPackageSession(apkFile: File) {
        val appContext = context.applicationContext
        val installer = appContext.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_REQUIRED)
        }
        val sessionId = installer.createSession(params)
        Log.d(TAG, "Created install session $sessionId for ${apkFile.length()} bytes")

        installer.openSession(sessionId).use { session ->
            FileInputStream(apkFile).use { input ->
                session.openWrite("base.apk", 0, apkFile.length()).use { output ->
                    val buffer = ByteArray(256 * 1024)
                    var written = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        written += read
                    }
                    session.fsync(output)
                    Log.d(TAG, "Wrote $written bytes into install session")
                }
            }

            val action = "${appContext.packageName}.APK_INSTALL_RESULT"
            registerInstallReceiver(appContext, action)

            val intent = Intent(action).setPackage(appContext.packageName)
            val piFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(
                appContext,
                sessionId,
                intent,
                piFlags
            )
            session.commit(pendingIntent.intentSender)
        }
        mainHandler.post {
            updateProgress(100, "تأیید نصب را در صفحه بعد بزنید")
        }
    }

    private fun registerInstallReceiver(appContext: Context, action: String) {
        if (installReceiverRegistered) return
        val filter = IntentFilter(action)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, Int.MIN_VALUE)
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                Log.d(TAG, "Install result status=$status message=$message")
                when (status) {
                    PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                        val confirmIntent = if (Build.VERSION.SDK_INT >= 33) {
                            intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(Intent.EXTRA_INTENT)
                        }
                        if (confirmIntent != null) {
                            confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            try {
                                appContext.startActivity(confirmIntent)
                                activeInstance?.updateProgress(100, "صفحه تأیید نصب باز شد")
                            } catch (e: Exception) {
                                Log.e(TAG, "Could not start confirm intent", e)
                                activeInstance?.fail("باز کردن صفحه نصب ممکن نشد")
                            }
                        } else {
                            activeInstance?.fail("صفحه تأیید نصب دریافت نشد")
                        }
                    }
                    PackageInstaller.STATUS_SUCCESS -> {
                        activeInstance?.updateProgress(100, "نصب با موفقیت انجام شد")
                        Toast.makeText(appContext, "به‌روزرسانی نصب شد", Toast.LENGTH_LONG).show()
                    }
                    PackageInstaller.STATUS_FAILURE,
                    PackageInstaller.STATUS_FAILURE_ABORTED,
                    PackageInstaller.STATUS_FAILURE_BLOCKED,
                    PackageInstaller.STATUS_FAILURE_CONFLICT,
                    PackageInstaller.STATUS_FAILURE_INCOMPATIBLE,
                    PackageInstaller.STATUS_FAILURE_INVALID,
                    PackageInstaller.STATUS_FAILURE_STORAGE -> {
                        val msg = message.orEmpty()
                        if (isSignatureMismatch(status, msg)) {
                            activeInstance?.handleSignatureMismatch()
                        } else {
                            activeInstance?.fail(
                                "نصب ناموفق بود" + (message?.let { ": $it" } ?: " (کد $status)")
                            )
                        }
                    }
                    else -> {
                        if (status != Int.MIN_VALUE) {
                            val msg = message.orEmpty()
                            if (isSignatureMismatch(status, msg)) {
                                activeInstance?.handleSignatureMismatch()
                            } else {
                                activeInstance?.fail("نصب ناموفق بود (کد $status)")
                            }
                        }
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            appContext.registerReceiver(receiver, filter)
        }
        installReceiverRegistered = true
        installReceiver = receiver
    }

    private fun installWithLegacyIntent(apkFile: File) {
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        Log.d(TAG, "Legacy install uri=$apkUri")
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, APK_MIME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newRawUri("apk", apkUri)
        }
        grantUriToInstallers(apkUri)
        context.startActivity(installIntent)
        updateProgress(100, "نصب‌کننده باز شد")
    }

    private fun handleSignatureMismatch() {
        downloadFinished = true
        downloadInProgress = false
        pendingInstallAfterPermission = false
        val apkFile = apkDestinationFile(context)
        val publicCopy = copyApkToPublicDownloads(apkFile)
        val publicPath = publicCopy?.absolutePath ?: "Downloads"
        val text =
            "امضای نسخه جدید با نسخه نصب‌شده یکی نیست.\n" +
                "نسخه دیباگ با APK سرور کلید متفاوت دارد.\n" +
                "۱) این برنامه را حذف کنید\n" +
                "۲) از Downloads نصب کنید:\n" +
                publicPath
        mainHandler.post {
            statusView?.text = text
            percentView?.text = "!"
            cancelButton?.text = "بستن"
            Toast.makeText(
                context,
                "امضا متفاوت است — برنامه را حذف و از Downloads نصب کنید",
                Toast.LENGTH_LONG
            ).show()
            openAppDetailsForUninstall()
        }
    }

    private fun copyApkToPublicDownloads(source: File): File? {
        if (!source.exists()) return null
        return try {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!dir.exists()) dir.mkdirs()
            val dest = File(dir, "phplus_update.apk")
            source.copyTo(dest, overwrite = true)
            try {
                android.media.MediaScannerConnection.scanFile(
                    context,
                    arrayOf(dest.absolutePath),
                    arrayOf(APK_MIME),
                    null
                )
            } catch (_: Exception) {
            }
            Log.d(TAG, "Copied APK to public Downloads: " + dest.absolutePath)
            dest
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy APK to public Downloads", e)
            null
        }
    }

    private fun openAppDetailsForUninstall() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    private fun resumeInstallAfterPermission() {
        if (closedByUser && dialog == null) {
            closedByUser = false
            downloadFinished = true
            showSheet()
            updateProgress(100, "اجازه دریافت شد. در حال نصب...")
            mainHandler.post { cancelButton?.text = "بستن" }
        }
        val apkFile = apkDestinationFile(context)
        if (!isInstallableApk(context, apkFile)) {
            pendingInstallAfterPermission = false
            fail("فایل دانلودشده دیگر موجود نیست؛ دوباره دانلود کنید")
            return
        }
        updateProgress(100, "اجازه دریافت شد. در حال نصب...")
        launchInstaller(apkFile)
    }

    private fun grantUriToInstallers(uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        val candidates = listOf(
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",
            "com.samsung.android.packageinstaller",
            "com.miui.packageinstaller",
            "com.google.android.permissioncontroller"
        )
        for (pkg in candidates) {
            try {
                context.grantUriPermission(pkg, uri, flags)
            } catch (_: Exception) {
            }
        }
        try {
            val resInfo = context.packageManager.queryIntentActivities(
                Intent(Intent.ACTION_VIEW).setDataAndType(uri, APK_MIME),
                0
            )
            for (info in resInfo) {
                try {
                    context.grantUriPermission(info.activityInfo.packageName, uri, flags)
                } catch (_: Exception) {
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun hasInstallPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true
        return context.packageManager.canRequestPackageInstalls()
    }

    private fun openInstallPermissionSettings() {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (_: Exception) {
            }
        }
    }

    private fun showSheet() {
        val activity = context as? Activity
        if (activity == null || activity.isFinishing) {
            Toast.makeText(context, "در حال دانلود به‌روزرسانی...", Toast.LENGTH_SHORT).show()
            return
        }
        if (dialog?.isShowing == true) return

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_update_download, null)
        progressBar = view.findViewById(R.id.progressUpdate)
        statusView = view.findViewById(R.id.txtUpdateStatus)
        percentView = view.findViewById(R.id.txtUpdatePercent)
        cancelButton = view.findViewById(R.id.btnUpdateCancel)
        cancelButton?.setOnClickListener {
            if (downloadFinished || pendingInstallAfterPermission) {
                dismissSheetOnly(clearPending = !pendingInstallAfterPermission)
            } else {
                cancelDownloadAndClose()
            }
        }

        dialog = AppCompatDialog(context, R.style.LoadingDialogTheme).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            setContentView(view)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setDimAmount(0.45f)
                setGravity(Gravity.BOTTOM)
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
            }
            show()
        }
    }

    private fun cancelDownloadAndClose() {
        closedByUser = true
        cancelled.set(true)
        downloadFuture?.cancel(true)
        downloadFuture = null
        downloadInProgress = false
        pendingInstallAfterPermission = false
        try {
            val dir = apkDestinationFile(context).parentFile
            apkDestinationFile(context).delete()
            File(dir, "$APK_FILE_NAME.part").delete()
        } catch (_: Exception) {
        }
        if (activeInstance === this) activeInstance = null
        dismissSheetOnly(clearPending = true)
    }

    private fun dismissSheetOnly(clearPending: Boolean = true) {
        closedByUser = true
        if (clearPending) pendingInstallAfterPermission = false
        try {
            dialog?.dismiss()
        } catch (_: Exception) {
        }
        dialog = null
        if (clearPending && activeInstance === this) activeInstance = null
    }

    private fun updateProgress(percent: Int, status: String) {
        if (closedByUser && dialog == null) return
        mainHandler.post {
            progressBar?.isIndeterminate = false
            progressBar?.setProgressCompat(percent, true)
            percentView?.text = "$percent٪"
            statusView?.text = status
        }
    }

    private fun setIndeterminate(status: String) {
        if (closedByUser && dialog == null) return
        mainHandler.post {
            progressBar?.isIndeterminate = true
            percentView?.text = "…"
            statusView?.text = status
        }
    }

    private fun fail(message: String) {
        if (closedByUser && dialog == null) return
        downloadFinished = true
        downloadInProgress = false
        downloadFuture = null
        mainHandler.post {
            statusView?.text = message
            percentView?.text = "!"
            cancelButton?.text = "بستن"
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val TAG = "ApkDownloader"
        private const val APK_MIME = "application/vnd.android.package-archive"
        private const val APK_FILE_NAME = "ph_update.apk"
        private const val MIN_APK_BYTES = 50_000L

        @Volatile
        private var downloadInProgress: Boolean = false

        @Volatile
        private var pendingInstallAfterPermission: Boolean = false

        @Volatile
        private var activeInstance: ApkDownloader? = null

        @Volatile
        private var installReceiverRegistered: Boolean = false

        private var installReceiver: BroadcastReceiver? = null

        private val executor: ExecutorService = Executors.newSingleThreadExecutor()

        private fun isSignatureMismatch(status: Int, message: String): Boolean {
            if (status == PackageInstaller.STATUS_FAILURE_INCOMPATIBLE) return true
            val m = message.lowercase()
            return m.contains("update_incompatible") ||
                m.contains("signatures do not match") ||
                m.contains("signature") && m.contains("match")
        }

        private fun apkDestinationFile(context: Context): File {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.filesDir
            return File(dir, APK_FILE_NAME)
        }

        private fun isGzipFile(file: File): Boolean {
            return try {
                file.inputStream().use { input ->
                    val b = ByteArray(2)
                    input.read(b) == 2 && b[0] == 0x1f.toByte() && b[1] == 0x8b.toByte()
                }
            } catch (_: Exception) {
                false
            }
        }

        private fun isZipLocalHeader(file: File): Boolean {
            return try {
                file.inputStream().use { input ->
                    val header = ByteArray(4)
                    input.read(header) == 4 &&
                        header[0] == 0x50.toByte() &&
                        header[1] == 0x4B.toByte() &&
                        header[2] == 0x03.toByte() &&
                        header[3] == 0x04.toByte()
                }
            } catch (_: Exception) {
                false
            }
        }

        private fun peekHeaderHex(file: File): String {
            return try {
                file.inputStream().use { input ->
                    val header = ByteArray(8)
                    val n = input.read(header)
                    header.take(n.coerceAtLeast(0)).joinToString(" ") { b ->
                        "%02x".format(b)
                    }
                }
            } catch (_: Exception) {
                "?"
            }
        }

        /** Real package parse check — catches truncated/corrupt APKs that still start with PK. */
        private fun isInstallableApk(context: Context, file: File): Boolean {
            if (!file.exists() || file.length() < MIN_APK_BYTES) return false
            if (!isZipLocalHeader(file)) return false
            return try {
                val pm = context.packageManager
                val info = if (Build.VERSION.SDK_INT >= 33) {
                    pm.getPackageArchiveInfo(
                        file.absolutePath,
                        PackageManager.PackageInfoFlags.of(0)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageArchiveInfo(file.absolutePath, 0)
                }
                if (info == null) {
                    Log.e(TAG, "getPackageArchiveInfo returned null")
                    false
                } else {
                    Log.d(
                        TAG,
                        "APK ok package=${info.packageName} version=${info.versionName}"
                    )
                    true
                }
            } catch (e: Exception) {
                Log.e(TAG, "getPackageArchiveInfo failed", e)
                false
            }
        }

        fun onHostResumed(context: Context) {
            if (!pendingInstallAfterPermission) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                !context.packageManager.canRequestPackageInstalls()
            ) {
                return
            }
            val file = apkDestinationFile(context.applicationContext)
            if (!isInstallableApk(context, file)) {
                pendingInstallAfterPermission = false
                return
            }

            val instance = activeInstance
            if (instance != null) {
                instance.closedByUser = false
                instance.cancelled.set(false)
                instance.resumeInstallAfterPermission()
            } else {
                pendingInstallAfterPermission = false
                val downloader = ApkDownloader(context)
                activeInstance = downloader
                downloader.closedByUser = false
                downloader.downloadFinished = true
                downloader.showSheet()
                downloader.updateProgress(100, "اجازه دریافت شد. در حال نصب...")
                downloader.mainHandler.post {
                    downloader.cancelButton?.text = "بستن"
                    downloader.launchInstaller(file)
                }
            }
        }

        fun cancelActiveDownload(context: Context? = null, deleteFile: Boolean = true) {
            val instance = activeInstance
            if (instance != null) {
                instance.closedByUser = true
                instance.cancelled.set(true)
                instance.downloadFuture?.cancel(true)
                instance.downloadFuture = null
                instance.mainHandler.post {
                    try {
                        instance.dialog?.dismiss()
                    } catch (_: Exception) {
                    }
                    instance.dialog = null
                }
            }
            downloadInProgress = false
            if (deleteFile) {
                pendingInstallAfterPermission = false
                val ctx = context ?: instance?.context
                activeInstance = null
                if (ctx != null) {
                    try {
                        val dest = apkDestinationFile(ctx.applicationContext)
                        dest.delete()
                        File(dest.parentFile, "$APK_FILE_NAME.part").delete()
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }
}
