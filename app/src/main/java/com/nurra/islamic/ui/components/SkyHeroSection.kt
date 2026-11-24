package com.nurra.islamic.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nurra.islamic.model.DailyPrayerSchedule
import com.nurra.islamic.viewmodel.LiveCountdownState
import java.util.Calendar
import kotlin.random.Random

enum class SkyPhase {
    SUBUH_FAJAR,   // 03:00 - 05:59 (Fajr / Dawn)
    PAGI_SIANG,    // 06:00 - 14:59 (Daylight / Noon)
    ASAR_SENJA,    // 15:00 - 18:30 (Sunset / Dusk)
    MAGHRIB_ISYA   // 18:31 - 02:59 (Night / Isha)
}

@Composable
fun SkyHeroSection(
    prayerSchedule: DailyPrayerSchedule?,
    liveCountdown: LiveCountdownState = LiveCountdownState(),
    onLocationClick: () -> Unit,
    onQiblaClick: () -> Unit,
    overrideSkyPhase: SkyPhase? = null,
    modifier: Modifier = Modifier
) {
    // Current Sky Phase based on local hour
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val skyPhase = overrideSkyPhase ?: when (currentHour) {
        in 3..5 -> SkyPhase.SUBUH_FAJAR
        in 6..14 -> SkyPhase.PAGI_SIANG
        in 15..18 -> SkyPhase.ASAR_SENJA
        else -> SkyPhase.MAGHRIB_ISYA
    }

    // Infinite transition for battery-friendly twinkling stars
    val transition = rememberInfiniteTransition(label = "SkyStarTwinkle")
    val starAlphaBase by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StarAlpha"
    )

    // Dynamic Time-Based Sky Gradient Colors - High Contrast Ratios
    val targetTopColor = when (skyPhase) {
        SkyPhase.SUBUH_FAJAR -> Color(0xFF0F172A)
        SkyPhase.PAGI_SIANG -> Color(0xFF0369A1) // Deep Rich Blue for high contrast text
        SkyPhase.ASAR_SENJA -> Color(0xFF1E1B4B)
        SkyPhase.MAGHRIB_ISYA -> Color(0xFF030712)
    }

    val targetMiddleColor = when (skyPhase) {
        SkyPhase.SUBUH_FAJAR -> Color(0xFF1E293B)
        SkyPhase.PAGI_SIANG -> Color(0xFF0284C7)
        SkyPhase.ASAR_SENJA -> Color(0xFF9D174D)
        SkyPhase.MAGHRIB_ISYA -> Color(0xFF0C1B33)
    }

    val targetBottomColor = when (skyPhase) {
        SkyPhase.SUBUH_FAJAR -> Color(0xFF1E3A8A)
        SkyPhase.PAGI_SIANG -> Color(0xFF0F766E)
        SkyPhase.ASAR_SENJA -> Color(0xFFB45309)
        SkyPhase.MAGHRIB_ISYA -> MaterialTheme.colorScheme.background
    }

    val topColor by animateColorAsState(targetValue = targetTopColor, animationSpec = tween(1200), label = "TopSkyColor")
    val middleColor by animateColorAsState(targetValue = targetMiddleColor, animationSpec = tween(1200), label = "MidSkyColor")
    val bottomColor by animateColorAsState(targetValue = targetBottomColor, animationSpec = tween(1200), label = "BotSkyColor")

    val skyGradient = Brush.verticalGradient(
        colors = listOf(topColor, middleColor, bottomColor)
    )

    val starVisibilityFactor = when (skyPhase) {
        SkyPhase.SUBUH_FAJAR -> 0.4f
        SkyPhase.PAGI_SIANG -> 0.0f
        SkyPhase.ASAR_SENJA -> 0.3f
        SkyPhase.MAGHRIB_ISYA -> 1.0f
    }

    val moonVisibilityFactor = when (skyPhase) {
        SkyPhase.SUBUH_FAJAR -> 0.5f
        SkyPhase.PAGI_SIANG -> 0.0f
        SkyPhase.ASAR_SENJA -> 0.7f
        SkyPhase.MAGHRIB_ISYA -> 1.0f
    }

    // Full Width Sky Hero Container (Starts at Y=0, true edge-to-edge status bar)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(brush = skyGradient)
    ) {
        // Starry Night & Celestial Canvas Overlay
        Canvas(
            modifier = Modifier.matchParentSize()
        ) {
            // Draw Stars if night/dusk/fajr
            if (starVisibilityFactor > 0f) {
                val random = Random(42)
                for (i in 0..32) {
                    val x = random.nextFloat() * size.width
                    val y = random.nextFloat() * (size.height * 0.85f)
                    val radius = (1.5f + random.nextFloat() * 2.5f).dp.toPx()
                    val alpha = (((starAlphaBase + random.nextFloat() * 0.3f) % 1.0f) * starVisibilityFactor).coerceIn(0.0f, 1.0f)

                    if (alpha > 0.05f) {
                        drawCircle(
                            color = Color.White.copy(alpha = alpha),
                            radius = radius,
                            center = Offset(x, y)
                        )
                    }
                }
            }

            // Draw Sun Glow if Daytime
            if (skyPhase == SkyPhase.PAGI_SIANG) {
                val sunX = size.width - 56.dp.toPx()
                val sunY = 75.dp.toPx()
                drawCircle(
                    color = Color(0xFFFDE047).copy(alpha = 0.35f),
                    radius = 42.dp.toPx(),
                    center = Offset(sunX, sunY)
                )
                drawCircle(
                    color = Color(0xFFFEF08A),
                    radius = 24.dp.toPx(),
                    center = Offset(sunX, sunY)
                )
            }

            // Draw Crescent Moon Illustration
            if (moonVisibilityFactor > 0f) {
                val moonCenterX = size.width - 60.dp.toPx()
                val moonCenterY = size.height * 0.48f

                // Outer Glowing Aura Halo
                drawCircle(
                    color = Color(0xFFFBBF24).copy(alpha = 0.20f * moonVisibilityFactor),
                    radius = 42.dp.toPx(),
                    center = Offset(moonCenterX, moonCenterY)
                )
                drawCircle(
                    color = Color(0xFFFDE68A).copy(alpha = 0.35f * moonVisibilityFactor),
                    radius = 30.dp.toPx(),
                    center = Offset(moonCenterX, moonCenterY)
                )

                // Crescent Moon Body
                drawCircle(
                    color = Color(0xFFFEF08A).copy(alpha = moonVisibilityFactor),
                    radius = 22.dp.toPx(),
                    center = Offset(moonCenterX, moonCenterY)
                )

                // Crescent Moon Cut Shadow
                drawCircle(
                    color = middleColor.copy(alpha = moonVisibilityFactor),
                    radius = 18.dp.toPx(),
                    center = Offset(moonCenterX - 9.dp.toPx(), moonCenterY - 5.dp.toPx())
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Bar Row: Location Pill & Qibla Pill stacked vertically on the Left side
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Location Pill Badge (Top)
                    Surface(
                        onClick = onLocationClick,
                        shape = RoundedCornerShape(18.dp),
                        color = Color.Black.copy(alpha = 0.30f),
                        contentColor = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Lokasi",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = prayerSchedule?.location?.cityName ?: "Jakarta",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }

                    // Qibla Direction Pill Badge (Below Location)
                    Surface(
                        onClick = onQiblaClick,
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = "Arah Kiblat",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Arah Kiblat",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Hero Countdown Display (Real-Time Hours : Minutes : Seconds)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${liveCountdown.nextPrayerName} Dalam",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.90f),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // 1-LINE HORIZONTAL LIVE COUNTDOWN ROW WITH SECONDS
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = liveCountdown.hoursFormatted,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            lineHeight = 34.sp
                        )
                        Text(
                            text = "j ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = liveCountdown.minutesFormatted,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            lineHeight = 34.sp
                        )
                        Text(
                            text = "m ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = liveCountdown.secondsFormatted,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            lineHeight = 34.sp
                        )
                        Text(
                            text = "d",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                // Next Prayer Time Display
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.30f),
                    contentColor = Color.White,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.30f),
                        shape = RoundedCornerShape(16.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mosque,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = liveCountdown.nextPrayerName,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = liveCountdown.nextPrayerTime,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Time Journey Progress Timeline Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${liveCountdown.prevPrayerName} ${liveCountdown.prevPrayerTime.replace(" WIB", "")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "${liveCountdown.nextPrayerName} ${liveCountdown.nextPrayerTime.replace(" WIB", "")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { liveCountdown.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = Color.White.copy(alpha = 0.30f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
