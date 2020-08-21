package dev.augu.nino.butterfly.i18n

/**
 * A language
 *
 * This class offers translation tables for languages, this allows easy, language-agnostic internationalization.
 *
 * The format of the translation is: "Regular string ${key} continuing regular string".
 *
 * ## Example
 * @sample dev.augu.nino.butterfly.examples.LanguageBot
 * @property name The name of the language
 * @property translationTable the translation table to fetch format keys from
 */
open class I18nLanguage(val name: String, private val translationTable: Map<String, String>) {
    companion object {
        /**
         * Matches ${key}.
         *
         * Used in translate to match all ${KEY} and replace them with the current argument.
         */
        private val KEY_REGEX = Regex("[$]\\{([\\w.]+)}")
    }

    /**
     * Translates the key given.
     *
     * @param key the key of the format to translate
     * @param arguments the arguments to the format
     * @return the translated result
     */
    fun translate(key: String, arguments: Map<String, String> = mapOf()): String {
        val format =
            translationTable[key] ?: throw IllegalArgumentException("Key is not found in the translation table.")
        return KEY_REGEX.replace(format, transform = {
            arguments[it.groups[1]!!.value] ?: "?"
        })
    }
}