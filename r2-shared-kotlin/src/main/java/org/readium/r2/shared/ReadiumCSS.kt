package org.readium.r2.shared



const val FONT_SIZE_REF = "fontSize"
const val FONT_FAMILY_REF = "fontFamily"
const val FONT_OVERRIDE_REF = "fontOverride"
const val APPEARANCE_REF = "appearance"
const val SCROLL_REF = "scroll"
const val PUBLISHER_DEFAULT_REF = "advancedSettings"
const val TEXT_ALIGNMENT_REF = "textAlign"
const val COLUMN_COUNT_REF = "colCount"
const val WORD_SPACING_REF = "wordSpacing"
const val LETTER_SPACING_REF = "letterSpacing"
const val PAGE_MARGINS_REF = "pageMargins"
const val LINE_HEIGHT_REF = "lineHeight"

const val FONT_SIZE_NAME = "--USER__$FONT_SIZE_REF"
const val FONT_FAMILY_NAME = "--USER__$FONT_FAMILY_REF"
const val FONT_OVERRIDE_NAME = "--USER__$FONT_OVERRIDE_REF"
const val APPEARANCE_NAME = "--USER__$APPEARANCE_REF"
const val SCROLL_NAME = "--USER__$SCROLL_REF"
const val PUBLISHER_DEFAULT_NAME = "--USER__$PUBLISHER_DEFAULT_REF"
const val TEXT_ALIGNMENT_NAME = "--USER__$TEXT_ALIGNMENT_REF"
const val COLUMN_COUNT_NAME = "--USER__$COLUMN_COUNT_REF"
const val WORD_SPACING_NAME = "--USER__$WORD_SPACING_REF"
const val LETTER_SPACING_NAME = "--USER__$LETTER_SPACING_REF"
const val PAGE_MARGINS_NAME = "--USER__$PAGE_MARGINS_REF"
const val LINE_HEIGHT_NAME = "--USER__$LINE_HEIGHT_REF"



// List of strings that can identify the name of a CSS custom property
// Also used for storing UserSettings in UserDefaults
enum class ReadiumCSSName(val ref: String) {
    fontSize("--USER__fontSize"),
    fontFamily("--USER__fontFamily"),
    fontOverride("--USER__fontOverride"),
    appearance("--USER__appearance"),
    scroll("--USER__scroll"),
    publisherDefault("--USER__advancedSettings"),
    textAlignment("--USER__textAlign"),
    columnCount("--USER__colCount"),
    wordSpacing("--USER__wordSpacing"),
    letterSpacing("--USER__letterSpacing"),
    pageMargins("--USER__pageMargins"),
    lineHeight("--USER__lineHeight"),
    paraIndent("--USER__paraIndent"),
    hyphens("--USER__bodyHyphens"),
    ligatures("--USER__ligatures");

    companion object {
        fun ref(name: String): ReadiumCSSName = ReadiumCSSName.valueOf(name)
    }

}


