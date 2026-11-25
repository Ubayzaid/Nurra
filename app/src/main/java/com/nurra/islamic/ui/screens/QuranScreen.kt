package com.nurra.islamic.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nurra.islamic.ui.components.AyahItemCard
import com.nurra.islamic.ui.components.ResumeQuranCard
import com.nurra.islamic.ui.components.SurahListItem
import com.nurra.islamic.viewmodel.QuranFilterTab
import com.nurra.islamic.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    viewModel: QuranViewModel,
    onNavigateToSurahDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val surahs by viewModel.surahs.collectAsState()
    val bookmarkedAyahs by viewModel.bookmarkedAyahs.collectAsState()
    val searchQuery by viewModel.searchState.collectAsState()
    val selectedTab by viewModel.tabState.collectAsState()
    val ibadahSummary by viewModel.ibadahSummaryState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Al-Qur'anul Karim",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Search Input Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("Cari nama surah atau nomor...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // 2. Resume Quran Card (Terakhir Dibaca) placed right BELOW Search Bar
            ResumeQuranCard(
                summary = ibadahSummary,
                onResumeReading = { surahId -> onNavigateToSurahDetail(surahId) },
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 3. PrimaryTabRow M3 Expressive
            PrimaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == QuranFilterTab.ALL,
                    onClick = { viewModel.setSelectedTab(QuranFilterTab.ALL) },
                    text = {
                        Text(
                            text = "Semua Surah (${surahs.size})",
                            fontWeight = if (selectedTab == QuranFilterTab.ALL) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == QuranFilterTab.BOOKMARKS,
                    onClick = { viewModel.setSelectedTab(QuranFilterTab.BOOKMARKS) },
                    text = {
                        Text(
                            text = "Tersimpan 🔖",
                            fontWeight = if (selectedTab == QuranFilterTab.BOOKMARKS) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Animated Tab Content (FadeIn + ScaleIn 0.95f -> FadeOut + ScaleOut 0.95f)
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    (fadeIn(tween(220)) + scaleIn(initialScale = 0.95f)) togetherWith
                    (fadeOut(tween(120)) + scaleOut(targetScale = 0.95f))
                },
                label = "QuranTabAnimation"
            ) { targetTab ->
                if (targetTab == QuranFilterTab.BOOKMARKS) {
                    val bookmarkedSurahs = surahs.filter { it.isBookmarked }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        // Section 1: Surah Tersimpan
                        if (bookmarkedSurahs.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Surah Tersimpan (${bookmarkedSurahs.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                )
                            }
                            items(
                                items = bookmarkedSurahs,
                                key = { "surah-${it.id}" }
                            ) { surah ->
                                SurahListItem(
                                    surah = surah,
                                    onClick = { onNavigateToSurahDetail(surah.id) },
                                    onBookmarkToggle = { viewModel.toggleSurahBookmark(surah.id) }
                                )
                            }
                        }

                        // Section 2: Ayat Tersimpan (Individual Verses Saved Separately)
                        if (bookmarkedAyahs.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Ayat Tersimpan (${bookmarkedAyahs.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                )
                            }
                            items(
                                items = bookmarkedAyahs,
                                key = { "ayah-${it.id}" }
                            ) { ayah ->
                                AyahItemCard(
                                    ayah = ayah,
                                    onBookmarkToggle = { viewModel.toggleAyahBookmark(ayah.id) },
                                    onPlayAudio = {
                                        onNavigateToSurahDetail(ayah.surahId)
                                    },
                                    onShare = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "${ayah.arabicText}\n\n\"${ayah.indonesianTranslation}\"\n\n(Ayat ${ayah.verseNumber}) via App Nurra")
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Bagikan Ayat"))
                                    }
                                )
                            }
                        }

                        if (bookmarkedSurahs.isEmpty() && bookmarkedAyahs.isEmpty()) {
                            item {
                                Text(
                                    text = "Belum ada Surah atau Ayat yang tersimpan.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(
                            items = surahs,
                            key = { it.id }
                        ) { surah ->
                            SurahListItem(
                                surah = surah,
                                onClick = { onNavigateToSurahDetail(surah.id) },
                                onBookmarkToggle = { viewModel.toggleSurahBookmark(surah.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
