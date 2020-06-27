package dev.augu.nino.butterfly

import dev.augu.nino.butterfly.i18n.I18nLanguage

/**
 * Defines the settings of the guild, can be overridden for custom settings.
 *
 * ## Example
 * @sample dev.augu.nino.butterfly.examples.CustomSettings
 * @property prefix the custom prefix of the guild, if null it will be ignored
 * @property language the custom language for the guild
 */
open class GuildSettings(open var prefix: String?, open var language: I18nLanguage?)