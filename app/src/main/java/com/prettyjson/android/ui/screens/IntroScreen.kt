package com.prettyjson.android.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * 3-screen intro tutorial: Paste → Beautify → Save → Share
 */
@Composable
fun IntroScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Progress indicators with animation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val isSelected = pagerState.currentPage == index
                val animatedWidth by animateDpAsState(
                    targetValue = if (isSelected) 32.dp else 8.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "indicator_width"
                )
                val animatedAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.4f,
                    animationSpec = tween(300),
                    label = "indicator_alpha"
                )
                
                Box(
                    modifier = Modifier
                        .width(animatedWidth)
                        .height(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = animatedAlpha),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
                if (index < 2) Spacer(modifier = Modifier.width(12.dp))
            }
        }
        
        // Pager content with fade animation
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
                },
                label = "page_content"
            ) { currentPage ->
                when (currentPage) {
                    0 -> IntroPage1()
                    1 -> IntroPage2()
                    2 -> IntroPage3()
                }
            }
        }
        
        // Centered Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pagerState.currentPage > 0) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Previous")
                }
            }
            
            if (pagerState.currentPage < 2) {
                Button(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.defaultMinSize(minWidth = 120.dp)
                ) {
                    Text("Next")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.defaultMinSize(minWidth = 160.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Get Started", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun IntroPage1() {
    val infiniteTransition = rememberInfiniteTransition(label = "icon_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .size(120.dp)
                .scale(scale),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ContentPaste,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            "Paste Your JSON",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Simply paste your JSON data or load it from a file or URL. The app automatically detects JSON in your clipboard!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun IntroPage2() {
    val infiniteTransition = rememberInfiniteTransition(label = "icon_animation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "icon_rotation"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .graphicsLayer { rotationZ = rotation },
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            "Beautify & Validate",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Your JSON is automatically formatted and validated. View syntax highlighting, sort keys, or minify your JSON with just one tap.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun IntroPage3() {
    val infiniteTransition = rememberInfiniteTransition(label = "icon_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .size(120.dp)
                .scale(scale),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.tertiaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            "Save & Share",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Save your JSON to favorites, share it with others, or export to different formats. Access your recent files instantly.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
    }
}

