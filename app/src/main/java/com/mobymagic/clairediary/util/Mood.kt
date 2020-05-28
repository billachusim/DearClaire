package com.mobymagic.clairediary.util

data class Mood(val id: Int, val name: String) {

    override fun toString(): String {
        return name
    }

    companion object {

        const val NO_MODE_ID = -1

        val MOODS = listOf(
                Mood(NO_MODE_ID, "Select mood"),
                Mood(1, "Feeling happy \uD83D\uDE0A"),
                Mood(2, "Feeling sad \uD83D\uDE14"),
                Mood(3, "Feeling excited \uD83D\uDE01"),
                Mood(4, "Falling in love ♥"),
                Mood(5, "Falling out of love \uD83D\uDC94"),
                Mood(6, "Feeling depressed \uD83D\uDE22"),
                Mood(7, "Feeling motivated \uD83D\uDC83"),
                Mood(8, "Feeling anxious \uD83D\uDE1F"),
                Mood(10, "Feeling sick \uD83E\uDD22"),
                Mood(11, "Feeling afraid \uD83D\uDE28"),
                Mood(12, "Feeling surprised \uD83D\uDE32"),
                Mood(13, "Feeling jealous \uD83D\uDE44"),
                Mood(14, "Feeling upside-down \uD83D\uDE43"),
                Mood(15, "Feeling embarrassed \uD83D\uDE33"),
                Mood(16, "Feeling gingered \uD83D\uDCAA"),
                Mood(17, "Feeling fly \uD83D\uDC7C"),
                Mood(18, "Feeling claire \uD83C\uDF38")
        )

        fun getMood(moodId: Int?): Mood {
            for (mood in MOODS) {
                if (mood.id == moodId) {
                    return mood
                }
            }

            return MOODS.first()
        }
    }
}