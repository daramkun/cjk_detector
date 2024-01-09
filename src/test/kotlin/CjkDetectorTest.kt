import `in`.daram.cjk_detector.Detector
import `in`.daram.cjk_detector.Language
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CjkDetectorTest {
    private val cjkDetector: Detector = Detector()

    @Test
    fun detectTest() {
        Assertions.assertEquals(cjkDetector.detect("안녕하세요."), Language.Korean)
        Assertions.assertEquals(cjkDetector.detect("おはよう"), Language.Japanese)
        Assertions.assertEquals(cjkDetector.detect("你好"), Language.UnknownChinese)
        Assertions.assertEquals(cjkDetector.detect("您好"), Language.UnknownChinese)
        Assertions.assertEquals(cjkDetector.detect("日本語でGoogle翻訳機で翻訳してみました"), Language.Japanese)
        Assertions.assertEquals(cjkDetector.detect("This is english text."), Language.Unknown)
        Assertions.assertEquals(cjkDetector.detect("ประโยคนี้เป็นภาษาไทย"), Language.Unknown)
        Assertions.assertEquals(cjkDetector.detect("Deze zin is in het Nederlands."), Language.Unknown)
    }
}