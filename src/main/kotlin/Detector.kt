package `in`.daram.cjk_detector

import java.io.File
import java.io.FileNotFoundException

class Detector {
    private var _koreanUnicodeBlocks: ArrayList<Character.UnicodeBlock> = arrayListOf(
        Character.UnicodeBlock.HANGUL_JAMO,
        Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_A,
        Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_B,
        Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO,
        Character.UnicodeBlock.HANGUL_SYLLABLES,
    )
    private var _koreanRanges: ArrayList<Range> = arrayListOf(
        // Half-width and Full-width Forms Unicode
        Range(0xFFA1, 0xFFBE),
        Range(0xFFC2, 0xFFC7),
        Range(0xFFCA, 0xFFCF),
        Range(0xFFD2, 0xFFD7),
        Range(0xFFDA, 0xFFDC),
    )

    private var _japaneseUnicodeBlocks: ArrayList<Character.UnicodeBlock> = arrayListOf(
        Character.UnicodeBlock.HIRAGANA,
        Character.UnicodeBlock.KATAKANA,
        Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS,
        Character.UnicodeBlock.KANA_EXTENDED_A,
        Character.UnicodeBlock.KANA_SUPPLEMENT,
    )
    private var _japaneseRanges: ArrayList<Range> = arrayListOf(
        // Half-width and Full-width Forms Unicode
        Range(0xFF66, 0xFF9D),
    )

    private var _hanjaUnicodeBlocks: ArrayList<Character.UnicodeBlock> = arrayListOf(
        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,
        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B,
        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C,
        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D,
        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E,
        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F,
    )

    private var _ignoreRanges: ArrayList<Range> = arrayListOf(
        Range(0x21, 0x2F),
        Range(0x30, 0x39),
        Range(0x3A, 0x40),
        Range(0x5B, 0x60),
        Range(0x7B, 0x7E),
    )

    private var _chineseTraditionalCharacters = ArrayList<Char>(2048)
    private var _chineseSimplifiedCharacters = ArrayList<Char>(2048)

    init {
        loadFromFile(_chineseTraditionalCharacters, "/traditional_chinese.txt")
        loadFromFile(_chineseSimplifiedCharacters, "/simplified_chinese.txt")
    }

    fun detect(text: CharSequence): Language {
        val count = Count()

        for (ch in text) {
            if (_ignoreRanges.any { r-> r.isIn(ch) }) {
                continue
            }

            val unicodeBlock = Character.UnicodeBlock.of(ch)
            if (_koreanUnicodeBlocks.contains(unicodeBlock) || _koreanRanges.any { r -> r.isIn(ch) }) {
                count.increaseKorean()
            } else if (_japaneseUnicodeBlocks.contains(unicodeBlock) || _japaneseRanges.any { r -> r.isIn(ch) }) {
                count.increaseJapanese()
            } else if (_hanjaUnicodeBlocks.contains(unicodeBlock)) {
                if (_chineseTraditionalCharacters.contains(ch)) {
                    count.increaseChineseTraditional()
                } else if (_chineseSimplifiedCharacters.contains(ch)) {
                    count.increaseChineseSimplified()
                } else {
                    count.increaseUnknownChinese()
                }
            } else {
                count.increaseUnknown()
            }
        }

        return count.calc()
    }

    fun isKorean(text: String): Boolean {
        return detect(text) == Language.Korean
    }

    fun isJapanese(text: String): Boolean {
        return detect(text) == Language.Japanese
    }

    fun isChineseTraditional(text: String): Boolean {
        return detect(text) == Language.ChineseTraditional
    }

    fun isChineseSimplified(text: String): Boolean {
        return detect(text) == Language.ChineseSimplified
    }

    private class Range {
        var start: Int
        var end: Int

        constructor(v: Int) {
            start = v
            end = v
        }

        constructor(s: Int, e: Int) {
            start = s
            end = e
        }

        fun isIn(ch: Char): Boolean {
            return (ch.code) in (start..end)
        }
    }

    private class Count {
        private var korean: Float = 0.0f
        private var japanese: Float = 0.0f
        private var chineseTraditional: Float = 0.0f
        private var chineseSimplified: Float = 0.0f
        private var unknownChinese: Float = 0.0f
        private var unknown: Float = 0.0f

        fun increaseKorean() {
            ++korean
        }

        fun increaseJapanese() {
            ++japanese
        }
        fun addJapanese(unit: Float) {
            japanese += unit
        }

        fun increaseChineseTraditional() {
            ++chineseTraditional
        }
        fun increaseChineseSimplified() {
            ++chineseSimplified
        }

        fun increaseUnknownChinese() {
            ++unknownChinese
        }

        fun increaseUnknown() {
            unknown += 0.1f
        }

        fun calc(): Language {
            val k = totalKorean
            val j = totalJapanese
            val t = totalChineseTraditional
            val s = totalChineseSimplified
            val u = totalUnknown

            if (k > j && k > t && k > s && k > u) {
                return Language.Korean
            } else if (j > k && j > t && j > s && j > u) {
                return Language.Japanese
            } else if (t > k && t > j && t > s && t > u) {
                return Language.ChineseTraditional
            } else if (s > k && s > j && s > t && s > u) {
                return Language.ChineseSimplified
            } else if (t > u && s > u && t == s) {
                return Language.UnknownChinese
            }

            return Language.Unknown
        }

        private val totalKorean: Int
            get() {
                return (korean * 10).toInt()
            }

        private val totalJapanese: Int
            get() {
                return ((japanese + unknownChinese) * 10).toInt()
            }

        private val totalChineseTraditional: Int
            get() {
                return ((chineseTraditional + unknownChinese) * 10).toInt()
            }

        private val totalChineseSimplified: Int
            get() {
                return ((chineseSimplified + unknownChinese) * 10).toInt()
            }

        private val totalUnknown: Int
            get() {
                return (unknown * 10).toInt()
            }
    }

    private fun loadFromFile(target: ArrayList<Char>, filename: String) {
        val file = File(Detector::class.java.getResource(filename)?.toURI() ?: throw FileNotFoundException())
        val reader = file.bufferedReader()

        for (line in reader.lines()) {
            val ch = Integer.valueOf(line, 16).toChar()
            target.add(ch)
        }
    }
}