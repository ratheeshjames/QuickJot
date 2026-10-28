package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Default Brand Colors
val PrimaryLight = Color(0xFF6750A4)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFEADDFF)
val OnPrimaryContainerLight = Color(0xFF21005D)

val SecondaryLight = Color(0xFF625B71)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE8DEF8)
val OnSecondaryContainerLight = Color(0xFF1D192B)

val TertiaryLight = Color(0xFF7D5260)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFFD8E4)
val OnTertiaryContainerLight = Color(0xFF31111D)

val BackgroundLight = Color(0xFFFEF7FF)
val OnBackgroundLight = Color(0xFF1D1B20)
val SurfaceLight = Color(0xFFFEF7FF)
val OnSurfaceLight = Color(0xFF1D1B20)
val SurfaceVariantLight = Color(0xFFE7E0EC)
val OnSurfaceVariantLight = Color(0xFF49454F)
val OutlineLight = Color(0xFF79747E)
val OutlineVariantLight = Color(0xFFCAC4D0)

// Dark Theme Colors
val PrimaryDark = Color(0xFFD0BCFF)
val OnPrimaryDark = Color(0xFF381E72)
val PrimaryContainerDark = Color(0xFF4F378B)
val OnPrimaryContainerDark = Color(0xFFEADDFF)

val SecondaryDark = Color(0xFFCCC2DC)
val OnSecondaryDark = Color(0xFF332D41)
val SecondaryContainerDark = Color(0xFF4A4458)
val OnSecondaryContainerDark = Color(0xFFE8DEF8)

val TertiaryDark = Color(0xFFEFB8C8)
val OnTertiaryDark = Color(0xFF492532)
val TertiaryContainerDark = Color(0xFF633B48)
val OnTertiaryContainerDark = Color(0xFFFFD8E4)

val BackgroundDark = Color(0xFF141218)
val OnBackgroundDark = Color(0xFFE6E0E9)
val SurfaceDark = Color(0xFF141218)
val OnSurfaceDark = Color(0xFFE6E0E9)
val SurfaceVariantDark = Color(0xFF49454F)
val OnSurfaceVariantDark = Color(0xFFCAC4D0)
val OutlineDark = Color(0xFF938F99)
val OutlineVariantDark = Color(0xFF49454F)

/**
 * 12 Note Label Colors mapped to tonal containers, readable in both light and dark themes.
 */
enum class NoteColor(
    val id: Int,
    val label: String,
    val lightContainer: Color,
    val lightOnContainer: Color,
    val lightBorder: Color,
    val darkContainer: Color,
    val darkOnContainer: Color,
    val darkBorder: Color
) {
    DEFAULT(
        id = 0,
        label = "Default",
        lightContainer = Color(0xFFF7F2FA),
        lightOnContainer = Color(0xFF1D1B20),
        lightBorder = Color(0xFFE6E0E9),
        darkContainer = Color(0xFF211F26),
        darkOnContainer = Color(0xFFE6E0E9),
        darkBorder = Color(0xFF2B2930)
    ),
    CORAL(
        id = 1,
        label = "Coral",
        lightContainer = Color(0xFFFFE8E4),
        lightOnContainer = Color(0xFF5D1914),
        lightBorder = Color(0xFFFFCDC5),
        darkContainer = Color(0xFF3B1B18),
        darkOnContainer = Color(0xFFFFDAD4),
        darkBorder = Color(0xFF532420)
    ),
    AMBER(
        id = 2,
        label = "Amber",
        lightContainer = Color(0xFFFFF0D4),
        lightOnContainer = Color(0xFF533400),
        lightBorder = Color(0xFFFFDE9F),
        darkContainer = Color(0xFF382300),
        darkOnContainer = Color(0xFFFFDF9E),
        darkBorder = Color(0xFF503300)
    ),
    LEMON(
        id = 3,
        label = "Lemon",
        lightContainer = Color(0xFFFEF7C8),
        lightOnContainer = Color(0xFF4A4500),
        lightBorder = Color(0xFFFBEFA3),
        darkContainer = Color(0xFF312F00),
        darkOnContainer = Color(0xFFE8E279),
        darkBorder = Color(0xFF464300)
    ),
    MINT(
        id = 4,
        label = "Mint",
        lightContainer = Color(0xFFE0F7E9),
        lightOnContainer = Color(0xFF074526),
        lightBorder = Color(0xFFB8ECC8),
        darkContainer = Color(0xFF103320),
        darkOnContainer = Color(0xFFA5F2BE),
        darkBorder = Color(0xFF1B492F)
    ),
    TEAL(
        id = 5,
        label = "Teal",
        lightContainer = Color(0xFFD8F5F2),
        lightOnContainer = Color(0xFF00403C),
        lightBorder = Color(0xFFAEEAE4),
        darkContainer = Color(0xFF08312F),
        darkOnContainer = Color(0xFF90ECE4),
        darkBorder = Color(0xFF134542)
    ),
    OCEAN(
        id = 6,
        label = "Ocean",
        lightContainer = Color(0xFFE0F0FF),
        lightOnContainer = Color(0xFF003761),
        lightBorder = Color(0xFFB8DDFF),
        darkContainer = Color(0xFF0B2945),
        darkOnContainer = Color(0xFFA0CFFF),
        darkBorder = Color(0xFF173E66)
    ),
    LAVENDER(
        id = 7,
        label = "Lavender",
        lightContainer = Color(0xFFEFE8FF),
        lightOnContainer = Color(0xFF301C66),
        lightBorder = Color(0xFFDDD0FF),
        darkContainer = Color(0xFF281E48),
        darkOnContainer = Color(0xFFD5C4FF),
        darkBorder = Color(0xFF3B2E65)
    ),
    PLUM(
        id = 8,
        label = "Plum",
        lightContainer = Color(0xFFF9E4F5),
        lightOnContainer = Color(0xFF4D1446),
        lightBorder = Color(0xFFF2C8EC),
        darkContainer = Color(0xFF381434),
        darkOnContainer = Color(0xFFF7B7EE),
        darkBorder = Color(0xFF50204A)
    ),
    BLUSH(
        id = 9,
        label = "Blush",
        lightContainer = Color(0xFFFFE6EB),
        lightOnContainer = Color(0xFF5C1024),
        lightBorder = Color(0xFFFFCCD7),
        darkContainer = Color(0xFF3D111E),
        darkOnContainer = Color(0xFFFFB2C5),
        darkBorder = Color(0xFF551B2C)
    ),
    EARTH(
        id = 10,
        label = "Earth",
        lightContainer = Color(0xFFF2EAE1),
        lightOnContainer = Color(0xFF433022),
        lightBorder = Color(0xFFDFD1C2),
        darkContainer = Color(0xFF2F241C),
        darkOnContainer = Color(0xFFDEC3AE),
        darkBorder = Color(0xFF433428)
    ),
    SLATE(
        id = 11,
        label = "Slate",
        lightContainer = Color(0xFFE7ECEF),
        lightOnContainer = Color(0xFF24333C),
        lightBorder = Color(0xFFCCD7DC),
        darkContainer = Color(0xFF1E282E),
        darkOnContainer = Color(0xFFBDCCD4),
        darkBorder = Color(0xFF2D3C44)
    );

    fun container(isDark: Boolean): Color = if (isDark) darkContainer else lightContainer
    fun onContainer(isDark: Boolean): Color = if (isDark) darkOnContainer else lightOnContainer
    fun border(isDark: Boolean): Color = if (isDark) darkBorder else lightBorder

    companion object {
        fun fromId(id: Int): NoteColor = entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}

/**
 * Accent palette choices for personalization when dynamic color is disabled.
 */
enum class AccentColorChoice(val displayName: String, val primaryColor: Color) {
    PURPLE("Royal Purple", Color(0xFF6750A4)),
    BLUE("Electric Blue", Color(0xFF00639B)),
    EMERALD("Emerald Green", Color(0xFF006C4C)),
    ROSE("Crimson Rose", Color(0xFF984061)),
    AMBER("Warm Amber", Color(0xFF865300)),
    CHARCOAL("Deep Slate", Color(0xFF475D6B))
}
