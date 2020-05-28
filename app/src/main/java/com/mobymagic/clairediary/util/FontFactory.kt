package com.mobymagic.clairediary.util

import android.content.Context
import com.mobymagic.clairediary.vo.Font
import com.vstechlab.easyfonts.EasyFonts

class FontFactory(private val context: Context) {

    fun getFonts(): List<Font> {
        val fonts = mutableListOf<Font>()
        fonts.add(Font("Roboto Regular", EasyFonts.robotoRegular(context)))
        fonts.add(Font("Android Nation", EasyFonts.androidNation(context)))
        fonts.add(Font("Cac Champagne", EasyFonts.cac_champagne(context)))
        fonts.add(Font("Capture It", EasyFonts.captureIt(context)))
        fonts.add(Font("Caviar Dreams", EasyFonts.caviarDreams(context)))
        fonts.add(Font("Droid Robot", EasyFonts.droidRobot(context)))
        fonts.add(Font("Freedom", EasyFonts.freedom(context)))
        fonts.add(Font("Fun Raiser", EasyFonts.funRaiser(context)))
        fonts.add(Font("Green Avocado", EasyFonts.greenAvocado(context)))
        fonts.add(Font("Ostrich Rounded", EasyFonts.ostrichRounded(context)))
        fonts.add(Font("Tangerine Regular", EasyFonts.tangerineRegular(context)))
        fonts.add(Font("Walkway Black", EasyFonts.walkwayBlack(context)))
        fonts.add(Font("Wind Song", EasyFonts.windSong(context)))
        return fonts
    }

    fun getFontWithName(fontName: String?): Font {
        val fonts = getFonts()
        var fontWithName = fonts.find { font ->
            font.fontName == fontName
        }

        if (fontWithName == null) {
            fontWithName = fonts[0]
        }

        return fontWithName
    }

}