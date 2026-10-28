package com.example.ui.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive shapes with playful, dynamic corner radiuses.
 */
val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

/**
 * Shape morphing targets for note cards:
 * Default card: smooth rounded corners.
 * Selected / active card: morphs to an expressive asymmetrical shape with a folded-corner feel.
 */
val NoteCardDefaultShape = RoundedCornerShape(20.dp)
val NoteCardSelectedShape = RoundedCornerShape(
    topStart = CornerSize(28.dp),
    topEnd = CornerSize(8.dp),
    bottomEnd = CornerSize(28.dp),
    bottomStart = CornerSize(28.dp)
)

val FabMenuShape = RoundedCornerShape(20.dp)
val ToolbarPillShape = RoundedCornerShape(50)
val SearchBarExpressiveShape = RoundedCornerShape(24.dp)
val ChipExpressiveShape = RoundedCornerShape(14.dp)
