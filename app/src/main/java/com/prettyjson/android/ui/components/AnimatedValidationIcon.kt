package com.prettyjson.android.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Animated validation icon that morphs between checkmark and warning
 */
@Composable
fun AnimatedValidationIcon(
    isValid: Boolean?,
    modifier: Modifier = Modifier
) {
    // Animated transition between valid and invalid states
    val targetIcon = when (isValid) {
        true -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        false -> Icons.Default.Warning to Color(0xFFF44336)
        null -> null to Color(0xFF9E9E9E)
    }
    
    AnimatedContent(
        targetState = isValid,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
            fadeOut(animationSpec = tween(300))
        },
        label = "validation_icon"
    ) { currentState ->
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            when (currentState) {
                true -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Valid",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier
                            .size(24.dp)
                            .scale(animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "scale"
                            ).value)
                    )
                }
                false -> {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Invalid",
                        tint = Color(0xFFF44336),
                        modifier = Modifier
                            .size(24.dp)
                            .scale(animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "scale"
                            ).value)
                            .rotate(animateFloatAsState(
                                targetValue = if (currentState == false) 0f else 180f,
                                animationSpec = tween(500),
                                label = "rotate"
                            ).value)
                    )
                }
                null -> {
                    Icon(
                        imageVector = Icons.Default.Help,
                        contentDescription = "Unknown",
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}


