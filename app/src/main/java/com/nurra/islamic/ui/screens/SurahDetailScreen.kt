package com.nurra.islamic.ui.screens

import android.content.Intent
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nurra.islamic.ui.components.AyahItemCard
import com.nurra.islamic.ui.theme.ArabicHeaderTextStyle
import com.nurra.islamic.viewmodel.QuranViewModel

// Helper extension to convert integers to Eastern Arabic Numerals (e.g. 1 -> ﴿١﴾, 2 -> ﴿٢﴾)
private fun Int.toArabicNumerals(): String {
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return this.toString().map { arabicDigits[it - '0'] }.joinToString("")
}

private fun sanitizeQuranicArabicText(rawText: String): String {
    return rawText
        .replace("ࣖ", "")
        .replace("ۙ", "")
        .replace("ۚ", "")
        .replace("ۗ", "")
        .replace("ۛ", "")
        .replace("ۜ", "")
        .replace("۠", "")
        .replace("ۡ", "")
        .replace("ۢ", "")
        .replace("[\\u08D0-\\u08FF]".toRegex(), "")
        .trim()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahDetailScreen(
    surahId: Int,
    viewModel: QuranViewModel,
    quranFontSizeSp: Float = 24f,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(surahId) {
        viewModel.selectSurah(surahId)
    }

    val surah by viewModel.currentSurahDetail.collectAsState()
    val ayahs by viewModel.currentAyahs.collectAsState()

    var isPureArabicMode by remember { mutableStateOf(false) }
    var isPlayingAudio by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(surahId) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isPlayingAudio = false
        }
    }

    fun toggleSurahAudio() {
        val currentSurah = surah ?: return
        if (isPlayingAudio) {
            mediaPlayer?.pause()
            isPlayingAudio = false
            Toast.makeText(context, "Audio surah dijeda", Toast.LENGTH_SHORT).show()
        } else {
            try {
                if (mediaPlayer == null) {
                    val formattedNumber = "%03d".format(currentSurah.id)
                    val audioUrl = "https://cdn.equran.id/audio-full/Misyari-Rasyid-Al-Afasi/$formattedNumber.mp3"

                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(audioUrl)
                        setOnPreparedListener {
                            start()
                            isPlayingAudio = true
                            Toast.makeText(context, "Memutar Audio Surah ${currentSurah.nameLatin}", Toast.LENGTH_SHORT).show()
                        }
                        setOnCompletionListener {
                            isPlayingAudio = false
                            Toast.makeText(context, "Audio Surah selesai", Toast.LENGTH_SHORT).show()
                        }
                        prepareAsync()
                    }
                } else {
                    mediaPlayer?.start()
                    isPlayingAudio = true
                    Toast.makeText(context, "Melanjutkan audio", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memutar audio: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = surah?.nameLatin ?: "Detail Surah",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${surah?.translationIndonesian ?: ""} • ${surah?.verseCount ?: 0} Ayat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    // Mode Toggle Pill Button (Placed Right Beside Bookmark)
                    Surface(
                        onClick = {
                            isPureArabicMode = !isPureArabicMode
                            val statusMsg = if (isPureArabicMode) "Mode Mushaf (Teks Arab Bersambung)" else "Mode Per-Ayat (Arab + Latin + Terjemahan)"
                            Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isPureArabicMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isPureArabicMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = if (isPureArabicMode) "Mode Mushaf" else "Per-Ayat",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = if (isPureArabicMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Bookmark Button
                    IconButton(
                        onClick = {
                            surah?.let { viewModel.toggleSurahBookmark(it.id) }
                        }
                    ) {
                        Icon(
                            imageVector = if (surah?.isBookmarked == true) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (surah?.isBookmarked == true) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 110.dp) // Generous bottom padding for floating bar
            ) {
                // Surah Header Bismillah Card
                item {
                    surah?.let { s ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    )
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = s.nameArabic,
                                        style = ArabicHeaderTextStyle,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${s.nameLatin} (${s.translationIndonesian})",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Surah Ke-${s.id} • ${s.verseCount} Ayat • ${s.revelationType}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Bismillah Header (Except Surah 9 At-Tawbah)
                                    if (s.id != 9) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                            style = ArabicHeaderTextStyle.copy(fontSize = 26.sp, lineHeight = 48.sp),
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Mode Mushaf: Combined Continuous Arabic Text Flow Block with Gold Verse Ornaments
                if (isPureArabicMode && ayahs.isNotEmpty()) {
                    item {
                        val goldColor = MaterialTheme.colorScheme.secondary

                        val combinedArabicText = buildAnnotatedString {
                            ayahs.forEach { ayah ->
                                append(sanitizeQuranicArabicText(ayah.arabicText))
                                append(" ")
                                withStyle(
                                    style = SpanStyle(
                                        color = goldColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = (quranFontSizeSp * 0.9f).sp
                                    )
                                ) {
                                    append("﴿${ayah.verseNumber.toArabicNumerals()}﴾")
                                }
                                append(" ")
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 24.dp)
                            ) {
                                Text(
                                    text = combinedArabicText,
                                    style = ArabicHeaderTextStyle.copy(
                                        fontSize = (quranFontSizeSp * 1.25f).sp,
                                        lineHeight = (quranFontSizeSp * 2.3f).sp,
                                        textAlign = TextAlign.End,
                                        textDirection = TextDirection.ContentOrRtl
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                } else {
                    // Mode Per-Ayat: Separate Cards for Verse-by-Verse Reading
                    items(
                        items = ayahs,
                        key = { it.id }
                    ) { ayah ->
                        AyahItemCard(
                            ayah = ayah,
                            fontSizeSp = quranFontSizeSp,
                            isPureArabicMode = false,
                            onBookmarkToggle = { viewModel.toggleAyahBookmark(ayah.id) },
                            onPlayAudio = {
                                Toast.makeText(context, "Memutar audio Ayat ${ayah.verseNumber}", Toast.LENGTH_SHORT).show()
                            },
                            onShare = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "${ayah.arabicText}\n\n\"${ayah.indonesianTranslation}\"\n\n(${surah?.nameLatin}: ${ayah.verseNumber}) via App Nurra")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Bagikan Ayat"))
                            }
                        )
                    }
                }
            }

            // FLOATING SURAH NAVIGATION & AUDIO PLAYER DOCK AT BOTTOM
            surah?.let { s ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(32.dp)
                        ),
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Surah Stepper Button (<)
                        IconButton(
                            onClick = {
                                if (s.id > 1) {
                                    mediaPlayer?.stop()
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                    isPlayingAudio = false
                                    viewModel.selectSurah(s.id - 1)
                                }
                            },
                            enabled = s.id > 1,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                                contentDescription = "Surah Sebelumnya",
                                tint = if (s.id > 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Center Audio Play Button & Surah Title
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { toggleSurahAudio() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Putar Surah",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = s.nameLatin,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isPlayingAudio) "Memutar Misyari Al-Afasi" else "${s.verseCount} Ayat • Putar Audio",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }

                        // Next Surah Stepper Button (>)
                        IconButton(
                            onClick = {
                                if (s.id < 114) {
                                    mediaPlayer?.stop()
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                    isPlayingAudio = false
                                    viewModel.selectSurah(s.id + 1)
                                }
                            },
                            enabled = s.id < 114,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                                contentDescription = "Surah Berikutnya",
                                tint = if (s.id < 114) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
