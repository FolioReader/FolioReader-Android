package com.folioreader.util

import android.os.Environment
import java.io.File

/**
 * @author Tyler Sedlar
 */
object FontFinder {

    private var sysFonts: Map<String, File>? = null

    @JvmStatic
    fun getSystemFonts(): Map<String, File> {
        if (sysFonts != null) {
            return sysFonts!!
        }

        val fonts = HashMap<String, File>()

        val sysFontDir = File("/system/fonts/")
        val fontSuffix = ".ttf"

        // Collect system fonts
        for (fontFile in sysFontDir.listFiles()) {
            val fontName: String = fontFile.name
            if (fontName.endsWith(fontSuffix)) {
                val key = fontName.subSequence(0, fontName.lastIndexOf(fontSuffix)).toString()
                fonts[key] = fontFile
            }
        }

        sysFonts = fonts

        return fonts
    }

    @JvmStatic
    fun getUserFonts(): Map<String, File> {
        val fonts = HashMap<String, File>()

        val fontDirs = arrayOf(
            File(Environment.getExternalStorageDirectory(), "Fonts/")
        )
        val fontSuffix = ".ttf"

        fontDirs.forEach { fontDir ->
            // Collect user fonts
            if (fontDir.exists() && fontDir.isDirectory) {
                fontDir.walkTopDown()
                    .filter { f -> f.name.endsWith(fontSuffix) }
                    .forEach { fontFile ->
                        val fontName = fontFile.name
                        val key =
                            fontName.subSequence(0, fontName.lastIndexOf(fontSuffix)).toString()
                        fonts[key] = fontFile
                    }
            }
        }
        return fonts
    }

    @JvmStatic
    fun getFontFile(key: String): File? {
        val system = getSystemFonts()
        val user = getUserFonts()

        return when {
            system.containsKey(key) -> system[key]
            user.containsKey(key) -> user[key]
            else -> null
        }
    }

    @JvmStatic
    fun isSystemFont(key: String): Boolean {
        return getSystemFonts().containsKey(key)
    }
}