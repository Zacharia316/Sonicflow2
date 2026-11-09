package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioAccentPill
import com.example.ui.theme.StudioAccentPillText
import com.example.ui.theme.StudioBgGradientEnd
import com.example.ui.theme.StudioBgGradientStart
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioPillInactive
import com.example.ui.theme.StudioPillInactiveText

/**
 * Gradient background container matching the user reference images
 * (warm blush pink to soft mauve lavender).
 */
@Composable
fun StudioScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        StudioBgGradientStart,
                        Color(0xFFE8DBEE),
                        StudioBgGradientEnd
                    )
                )
            ),
        content = content
    )
}

/**
 * Soft card with smooth rounded corners, semi-translucent surface, and subtle border.
 */
@Composable
fun StudioCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = StudioCardBg,
    borderColor: Color = StudioCardBorder,
    elevation: Dp = 1.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .shadow(elevation, shape, clip = false)
            .clip(shape)
            .background(backgroundColor)
            .border(0.8.dp, borderColor, shape)
            .then(clickModifier)
            .padding(contentPadding),
        content = content
    )
}

/**
 * Pill button for filter tabs, mode selectors, and tags (Clean, Warm, Drill, Afrobeats, etc.)
 */
@Composable
fun StudioPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) StudioAccentPill else StudioPillInactive
    val textColor = if (isSelected) StudioAccentPillText else StudioPillInactiveText

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
