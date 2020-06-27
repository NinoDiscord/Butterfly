package i18n

import dev.augu.nino.butterfly.i18n.I18nLanguage
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * Translates to the B language
 */
internal fun actualBLanguageTranslator(s: String): String {
    val builder: StringBuilder = StringBuilder()
    for (c in s) {
        if (c.isLetter()) {
            builder.append('b')
        }
        builder.append(c)
    }
    return builder.toString()
}

class I18nLanguageTests : DescribeSpec({

    describe("I18n Language Tests") {
        val theBLanguage = I18nLanguage(
            mapOf(
                "simple" to "bsbibmbpblbe",
                "two words" to "btbwbo bwbobrbdbs",
                "one key" to "bobnbe \${key}",
                "multiple keys" to "\${key1} bmbublbtbibpblbe \${key2} bkbebybs \${key3}"
            )
        )

        it("should translate simple no key tests") {
            theBLanguage.translate("simple").shouldBe(actualBLanguageTranslator("simple"))
            theBLanguage.translate("two words").shouldBe(actualBLanguageTranslator("two words"))
        }

        it("should throw on invalid key") {
            shouldThrow<IllegalArgumentException> {
                theBLanguage.translate("what?")
            }
        }

        it("should translate one key with the key argument") {
            theBLanguage.translate("one key", mapOf("key" to "bybebebt!"))
                .shouldBe(actualBLanguageTranslator("one yeet!"))
        }

        it("should translate multiple keys with the correct arguments") {
            theBLanguage.translate(
                "multiple keys",
                mapOf("key1" to "bybebebt", "key2" to "bsbibmbpblbe", "key3" to "brbebe?")
            )
                .shouldBe(actualBLanguageTranslator("yeet multiple simple keys ree?"))
        }

        it("should add translate with ? if an argument was not fullfilled") {
            theBLanguage.translate("one key").shouldBe(actualBLanguageTranslator("one ?"))
            theBLanguage.translate("multiple keys").shouldBe(actualBLanguageTranslator("? multiple ? keys ?"))
            theBLanguage.translate("multiple keys", mapOf("key1" to "!"))
                .shouldBe(actualBLanguageTranslator("! multiple ? keys ?"))
            theBLanguage.translate("multiple keys", mapOf("key2" to "!", "key3" to "blbl"))
                .shouldBe(actualBLanguageTranslator("? multiple ! keys ll"))
        }
    }

})