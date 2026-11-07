package com.lazybonez.app

import android.os.SystemClock
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.lazy.LazyListState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random
import java.io.File

@Composable
internal fun EmojiSlotMachine(onBack: () -> Unit) {
    val context = LocalContext.current
    val modelFile = remember(context) {
        val modelDirectory = context.getExternalFilesDir("models") ?: File(context.filesDir, "models")
        modelDirectory.mkdirs()
        File(modelDirectory, LOCAL_MODEL_FILE_NAME)
    }
    var catalog by remember { mutableStateOf<List<EmojiEntry>>(emptyList()) }
    var loadFailed by remember { mutableStateOf(false) }
    var spinning by remember { mutableStateOf(false) }
    var stopRequestedAt by remember { mutableStateOf<Long?>(null) }
    var spinId by remember { mutableIntStateOf(0) }
    var reelOrders by remember { mutableStateOf(List(3) { emptyList<EmojiEntry>() }) }
    var reelPhases by remember { mutableStateOf(List(3) { 0 }) }
    var modelPresent by remember(modelFile) { mutableStateOf(modelFile.isFile) }
    var generatingPlot by remember { mutableStateOf(false) }
    var generatedPlot by remember { mutableStateOf("") }
    var generationError by remember { mutableStateOf("") }
    val winners = remember { mutableStateListOf<EmojiEntry?>(null, null, null) }
    val generator = remember(modelFile) { LocalPlotGenerator(modelFile) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(generator) {
        onDispose { generator.close() }
    }

    LaunchedEffect(context) {
        runCatching { EmojiCatalog.load(context) }
            .onSuccess { catalog = it }
            .onFailure { loadFailed = true }
    }

    val settled = winners.all { it != null }
    val canSpin = catalog.isNotEmpty() && !spinning
    val resultsReady = settled && !spinning
    val onSpinAction = {
        if (!spinning) {
            reelOrders = List(3) { catalog.shuffled() }
            reelPhases = List(3) { Random.nextInt(0, catalog.size) }
            winners.indices.forEach { winners[it] = null }
            generatingPlot = false
            generatedPlot = ""
            generationError = ""
            stopRequestedAt = null
            spinning = true
            spinId += 1
        } else {
            stopRequestedAt = SystemClock.elapsedRealtime()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Obsidian, NightSurface, Obsidian)))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = BoneWhite,
                    ),
                ) { Text("‹  BACK", letterSpacing = 1.sp) }
                Spacer(Modifier.weight(1f))
                Text("LAZY BONEZ 💀", color = BoneWhite.copy(alpha = 0.55f), fontSize = 12.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "PLOTIFY",
                color = BoneWhite,
                fontSize = 36.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "SPIN UP A NIGHTMARE",
                color = Ectoplasm,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(24.dp))

            if (catalog.isEmpty()) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (loadFailed) {
                        Text("The emoji catalog could not be loaded.", color = BoneWhite)
                    } else {
                        CircularProgressIndicator(color = Ectoplasm)
                        Spacer(Modifier.height(12.dp))
                        Text("Loading the emoji catalog…", color = BoneWhite.copy(alpha = 0.72f))
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.Black.copy(alpha = 0.38f))
                        .border(1.dp, BoneWhite.copy(alpha = 0.13f), RoundedCornerShape(22.dp))
                        .padding(horizontal = 10.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(3) { index ->
                        EmojiReel(
                            modifier = Modifier.weight(1f),
                            index = index,
                            entries = reelOrders[index],
                            phase = reelPhases[index],
                            spinId = spinId,
                            spinning = spinning,
                            stopRequestedAt = stopRequestedAt,
                            previousLocked = index == 0 || winners[index - 1] != null,
                            winner = winners[index],
                            onLocked = { result ->
                                winners[index] = result
                                if (winners.all { it != null }) {
                                    spinning = false
                                    generatedPlot = ""
                                    generationError = ""
                                    modelPresent = modelFile.isFile
                                    if (modelPresent) {
                                        generatingPlot = true
                                        val selectedEntries = winners.filterNotNull().toList()
                                        coroutineScope.launch {
                                            try {
                                                generatedPlot = generator.generate(selectedEntries)
                                            } catch (error: Exception) {
                                                generationError = error.message
                                                    ?: "The local model could not generate a plot."
                                                modelPresent = modelFile.isFile
                                            } finally {
                                                generatingPlot = false
                                            }
                                        }
                                    } else {
                                        generationError = "Local model file not found. Copy it to the path below."
                                    }
                                }
                            },
                        )
                    }
                }

                if (resultsReady) {
                    Spacer(Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        when {
                            generatingPlot -> Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Ectoplasm,
                                    strokeWidth = 2.dp,
                                )
                                Text(
                                    "LOADING MODEL & WRITING…",
                                    color = BoneWhite.copy(alpha = 0.78f),
                                    fontSize = 11.sp,
                                )
                            }
                            generatedPlot.isNotBlank() -> Text(
                                text = generatedPlot,
                                color = BoneWhite,
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = 25.sp,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            )
                            generationError.isNotBlank() -> Text(
                                text = "$generationError\n${modelFile.absolutePath}",
                                color = if (modelPresent) BloodRed else BoneWhite.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = if (spinning) "LET THE REELS ROLL…" else "THREE EMOJIS. ONE BAD IDEA.",
                        color = BoneWhite.copy(alpha = 0.62f),
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.height(64.dp).wrapContentHeight(Alignment.CenterVertically),
                    )
                }
            }
        }
        if (catalog.isNotEmpty()) {
            Button(
                enabled = !generatingPlot && (canSpin || (spinning && stopRequestedAt == null)),
                onClick = onSpinAction,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (spinning) NightSurface else BloodRed,
                    contentColor = BoneWhite,
                    disabledContainerColor = NightSurface.copy(alpha = 0.6f),
                    disabledContentColor = BoneWhite.copy(alpha = 0.45f),
                ),
                border = if (spinning) BorderStroke(1.dp, BloodRed) else null,
            ) {
                Text(
                    text = when {
                        resultsReady -> "TAKE ANOTHER STAB AT IT"
                        stopRequestedAt != null -> "FATE AWAITS..."
                        spinning -> "GIVE ME SOME SUGAR, BABY"
                        else -> "SPIN FOR INSPIRATION"
                    },
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun EmojiReel(
    modifier: Modifier,
    index: Int,
    entries: List<EmojiEntry>,
    phase: Int,
    spinId: Int,
    spinning: Boolean,
    stopRequestedAt: Long?,
    previousLocked: Boolean,
    winner: EmojiEntry?,
    onLocked: (EmojiEntry) -> Unit,
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val rowHeight = 54.dp
    val rowHeightPx = with(density) { rowHeight.toPx() }
    val currentSpinning by rememberUpdatedState(spinning)
    val currentStopTime by rememberUpdatedState(stopRequestedAt)
    val currentPreviousLocked by rememberUpdatedState(previousLocked)
    val currentOnLocked by rememberUpdatedState(onLocked)
    val speedMs = 42 + index * 8

    LaunchedEffect(spinId, entries, phase) {
        if (entries.isEmpty()) return@LaunchedEffect
        val centerStart = entries.size * 15 + phase
        listState.scrollToItem(centerStart - 2)

        while (isActive && currentSpinning) {
            if (currentStopTime != null && currentPreviousLocked) {
                if (index > 0) delay(STOP_STAGGER_MS)
                break
            }
            listState.animateScrollBy(
                value = -rowHeightPx,
                animationSpec = tween(durationMillis = speedMs, easing = LinearEasing),
            )
        }

        if (!currentSpinning) return@LaunchedEffect
        repeat(DECELERATION_STEPS) { step ->
            listState.animateScrollBy(
                value = -rowHeightPx,
                animationSpec = tween(
                    durationMillis = 85 + step * 60,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
        val selectedIndex = (listState.firstVisibleItemIndex + 2) % entries.size
        currentOnLocked(entries[selectedIndex])
    }

    Box(
        modifier = modifier
            .height(rowHeight * VISIBLE_ROWS)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF171717)),
    ) {
        LazyColumn(
            state = listState,
            userScrollEnabled = false,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(count = entries.size * REEL_CYCLES, key = { it }) { itemIndex ->
                val entry = entries[itemIndex % entries.size]
                val isSelected = winner != null && itemIndex == listState.firstVisibleItemIndex + 2
                Box(
                    modifier = Modifier.fillMaxWidth().height(rowHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = entry.symbol,
                        modifier = Modifier.offset(y = 6.dp),
                        fontSize = 34.sp,
                        color = BoneWhite.copy(alpha = if (winner != null && !isSelected) 0.24f else 1f),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(rowHeight)
                .zIndex(1f)
                .border(
                    width = if (winner != null) 2.dp else 1.dp,
                    color = if (winner != null) BloodRed else Ectoplasm.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp),
                ),
        )
        Text(
            text = "${index + 1}",
            color = BoneWhite.copy(alpha = 0.35f),
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 7.dp),
        )
    }
}

private const val VISIBLE_ROWS = 5
private const val REEL_CYCLES = 32
private const val DECELERATION_STEPS = 4
private const val STOP_STAGGER_MS = 90L
