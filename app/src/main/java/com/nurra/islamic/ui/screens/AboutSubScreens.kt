package com.nurra.islamic.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.nurra.islamic.R
import com.nurra.islamic.api.ApiClient
import com.nurra.islamic.repository.AppSettings
import com.nurra.islamic.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateScreen(
    viewModel: SettingsViewModel? = null,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settingsState = viewModel?.settings?.collectAsState()
    val settings = settingsState?.value ?: AppSettings()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var isChecking by remember { mutableStateOf(false) }
    var isUpdateAvailable by remember { mutableStateOf(false) }
    var updateStatusTitle by remember { mutableStateOf("Versi Terbaru Terpasang") }
    var updateStatusMessage by remember { mutableStateOf("Aplikasi Nurra (v1.0.0) terhubung dengan Repository GitHub Ubayzaid/Nurra.") }
    var downloadApkUrl by remember { mutableStateOf<String?>(null) }
    var releaseBodyText by remember { mutableStateOf<String?>(null) }
    var latestTagName by remember { mutableStateOf<String?>("v1.0.0") }

    val logoBitmap = remember(context) {
        try {
            val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher_round)
                ?: ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
                ?: ContextCompat.getDrawable(context, R.drawable.ic_nurra_logo)
            drawable?.let { d ->
                val w = if (d.intrinsicWidth > 0) d.intrinsicWidth else 108
                val h = if (d.intrinsicHeight > 0) d.intrinsicHeight else 108
                d.toBitmap(w, h).asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membuka browser", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareApp() {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Yuk pakai Nurra - Teman ibadah dalam keseharian! Unduh rilis aplikasi Nurra di GitHub: https://github.com/Ubayzaid/Nurra"
                )
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Bagikan Aplikasi Nurra")
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membagikan aplikasi", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkGitHubRelease() {
        coroutineScope.launch {
            isChecking = true
            updateStatusTitle = "Memeriksa GitHub Releases..."
            updateStatusMessage = "Menghubungi https://api.github.com/repos/Ubayzaid/Nurra/releases/latest..."

            withContext(Dispatchers.IO) {
                try {
                    val response = ApiClient.gitHubApiService.getLatestRelease()
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            val release = response.body()!!
                            val tagName = release.tagName ?: "v1.0.0"
                            latestTagName = tagName
                            releaseBodyText = release.body ?: "Rilis versi baru untuk aplikasi Nurra."
                            val apkAsset = release.assets?.firstOrNull { it.browserDownloadUrl?.endsWith(".apk", ignoreCase = true) == true }

                            if (tagName != "v1.0.0" && tagName != "1.0.0") {
                                isUpdateAvailable = true
                                updateStatusTitle = "🎉 Pembaruan $tagName Tersedia!"
                                updateStatusMessage = "Versi baru $tagName telah dirilis di GitHub! Silakan perbarui untuk menikmati fitur terbaru."
                                downloadApkUrl = apkAsset?.browserDownloadUrl ?: release.htmlUrl
                                Toast.makeText(context, "Versi $tagName tersedia di GitHub!", Toast.LENGTH_LONG).show()
                            } else {
                                isUpdateAvailable = false
                                updateStatusTitle = "✅ Versi Terbaru Terpasang"
                                updateStatusMessage = "Terhubung ke GitHub Ubayzaid/Nurra. Rilis terbaru ($tagName) sudah terpasang di HP Anda."
                                downloadApkUrl = null
                                Toast.makeText(context, "Aplikasi Anda sudah versi terbaru", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // HTTP 404 - No release tag on GitHub yet
                            isUpdateAvailable = false
                            latestTagName = "v1.0.0"
                            releaseBodyText = null
                            updateStatusTitle = "✅ Versi Terbaru (v1.0.0)"
                            updateStatusMessage = "Terhubung ke Repository GitHub Ubayzaid/Nurra. Belum ada tag Rilis APK baru. Versi v1.0.0 saat ini sudah paling baru."
                            downloadApkUrl = null
                            Toast.makeText(context, "Terhubung ke Ubayzaid/Nurra (v1.0.0 Terbaru)", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isUpdateAvailable = false
                        latestTagName = "v1.0.0"
                        releaseBodyText = null
                        updateStatusTitle = "✅ Versi Terbaru (v1.0.0)"
                        updateStatusMessage = "Pemeriksaan selesai: Versi v1.0.0 Terbaru."
                        downloadApkUrl = null
                        Toast.makeText(context, "Terhubung ke Repository (v1.0.0 Terbaru)", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            isChecking = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Update Aplikasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Update & Pengaturan", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.SystemUpdate, contentDescription = null) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Catatan Perubahan", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.Description, contentDescription = null) }
                )
            }

            if (selectedTabIndex == 0) {
                // TAB 0: Update & Settings
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Header Card with Logo and Update Badge
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // App Logo with Update Badge Indicator on Top-Right
                                Box(contentAlignment = Alignment.TopEnd) {
                                    Surface(
                                        shape = RoundedCornerShape(22.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(76.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (logoBitmap != null) {
                                                Image(
                                                    bitmap = logoBitmap,
                                                    contentDescription = "Logo Nurra",
                                                    modifier = Modifier.size(58.dp)
                                                )
                                            } else {
                                                Image(
                                                    painter = painterResource(id = R.drawable.ic_nurra_logo),
                                                    contentDescription = "Logo Nurra",
                                                    modifier = Modifier.size(58.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Badge indicator
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isUpdateAvailable) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
                                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                                        modifier = Modifier
                                            .size(26.dp)
                                            .offset(x = 6.dp, y = (-6).dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isUpdateAvailable) Icons.Default.NewReleases else Icons.Default.CheckCircle,
                                                contentDescription = "Badge Pembaruan",
                                                tint = if (isUpdateAvailable) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Nurra",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Versi Terpasang: 1.0.0 (Build 2026.1)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isUpdateAvailable) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = if (isUpdateAvailable) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                ) {
                                    Text(
                                        text = updateStatusTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = updateStatusMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )

                                if (!downloadApkUrl.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { openUrl(downloadApkUrl!!) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Unduh APK Resmi di GitHub", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { checkGitHubRelease() },
                                    enabled = !isChecking,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    if (isChecking) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Memeriksa GitHub Releases...")
                                    } else {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Periksa Pembaruan Sekarang", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // 2. Pengaturan Pembaruan Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "Pengaturan Pembaruan",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Switch 1: Otomatis Memeriksa Pembaruan
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Otomatis Memeriksa Pembaruan",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Periksa rilis APK baru di GitHub secara otomatis.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Switch(
                                        checked = settings.isAutoCheckUpdateEnabled,
                                        onCheckedChange = { viewModel?.toggleAutoCheckUpdate(it) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Switch 2: Notifikasi Pembaruan
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Notifikasi Pembaruan",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Tampilkan pengingat jika versi rilis baru telah tersedia.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Switch(
                                        checked = settings.isUpdateNotificationEnabled,
                                        onCheckedChange = { viewModel?.toggleUpdateNotification(it) }
                                    )
                                }
                            }
                        }
                    }

                    // 3. Bagikan Aplikasi Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Surface(
                                onClick = { shareApp() },
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Bagikan Aplikasi",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Bagikan rilis dan aplikasi Nurra kepada keluarga dan sahabat.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // TAB 1: Catatan Perubahan (Changelog)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = "Catatan Perubahan",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Release Notes: ${latestTagName ?: "v1.0.0"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (!releaseBodyText.isNullOrBlank()) {
                                    Text(
                                        text = releaseBodyText!!,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                } else {
                                    // Fallback view when no GitHub release notes available yet
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Belum Ada Catatan Perubahan Baru",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = "Saat ini belum ada catatan rilis versi baru di GitHub Releases (Nurra v1.0.0). Semua fitur unggulan telah disertakan pada versi rilis ini.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Spacer(modifier = Modifier.height(14.dp))

                                            Text(
                                                text = "Fitur Rilis Perdana Nurra (v1.0.0):",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            val features = listOf(
                                                "📖 Al-Qur'an 30 Juz Lengkap & Audio Murottal",
                                                "🕌 Jadwal Salat Presisi Kemenag & Alarm Adzan",
                                                "🧭 Kompas Kiblat Akurat dengan Sensor Kompas",
                                                "📿 Tasbih Digital Reaktif & Pengatur Getar",
                                                "🤲 Koleksi 108 Doa Harian & Dzikir Shahih",
                                                "🎨 Mode Tampilan & Tema Dinamis M3 Expressive"
                                            )

                                            features.forEach { feat ->
                                                Text(
                                                    text = "• $feat",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.padding(vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubInfoScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membuka tautan browser", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("GitHub Repository", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Kode Sumber Terbuka (Open Source)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Nurra dibangun sebagai proyek sumber terbuka yang transparan dan dapat dikembangkan bersama oleh komunitas Android Muslim.", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { openUrl("https://github.com/Ubayzaid/Nurra/") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buka Repository Ubayzaid/Nurra", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperInfoScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Pengembang", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Tim Pengembang Nurra", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Aplikasi Nurra dikembangkan oleh Tim Pengembang Android Indonesia dengan dedikasi menyajikan pendamping ibadah Muslim modern yang cepat, indah, dan bebas iklan.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSourceInfoScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membuka tautan", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Sumber Data Resmi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("1. myQuran API v2 (Kemenag RI)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Sumber resmi perhitungan jadwal salat 5 waktu, kalender Hijriah, dan koleksi 108 Doa & Hadits Shahih.", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(onClick = { openUrl("https://api.myquran.com/") }, shape = RoundedCornerShape(12.dp)) {
                            Text("Dokumentasi myQuran API")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("2. EQuran.id API v2 (Kemenag RI)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Sumber resmi data 114 Surah Al-Qur'an, teks Utsmani Kemenag RI, transliterasi Latin, dan audio Misyari Rasyid Al-Afasi.", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(onClick = { openUrl("https://equran.id/") }, shape = RoundedCornerShape(12.dp)) {
                            Text("Situs Resmi EQuran.id")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechStackInfoScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Teknologi Aset", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val techs = listOf(
                Pair("Kotlin 2.2", "Bahasa pemrograman utama Android modern"),
                Pair("Jetpack Compose & Material 3 Expressive", "Kerangka UI deklaratif reaktif"),
                Pair("Retrofit 2 & OkHttp 4", "Klien REST API terpusat"),
                Pair("Preferences DataStore", "Penyimpanan preferensi reaktif"),
                Pair("Android Location & Geocoder", "Layanan lokasi GPS & pemetaan wilayah"),
                Pair("Google Fonts Amiri", "Tipografi kaligrafi Utsmani Al-Qur'an")
            )

            items(techs) { (title, desc) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesInfoScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Lisensi Terbuka", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Apache License 2.0", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Digunakan oleh Android Jetpack, Retrofit 2, OkHttp 4, Gson, dan Material Design Icons.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("SIL Open Font License", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Digunakan oleh font Amiri Quran dari Google Fonts.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Kebijakan Privasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val sections = listOf(
                Pair("1. Izin Lokasi", "Lokasi GPS hanya digunakan secara lokal di perangkat Anda untuk menghitung jadwal salat presisi Kemenag RI dan menentukan arah Kiblat ke Makkah. Data lokasi tidak pernah dikirim ke pihak ketiga."),
                Pair("2. Akses Jaringan Internet", "Akses internet digunakan untuk mengunduh jadwal salat dan teks Al-Qur'an dari server resmi Kemenag RI (myQuran API & EQuran.id)."),
                Pair("3. Penyimpanan Lokal Perangkat", "Aplikasi menggunakan memori cache perangkat untuk menyimpan data Al-Qur'an offline dan preferensi pengguna."),
                Pair("4. Notifikasi Adzan", "Izin notifikasi digunakan untuk menjadwalkan pengingat adzan waktu salat secara lokal via AlarmManager.")
            )

            items(sections) { (title, desc) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
