package dev.augu.nino.butterfly

/**
 * Defines the settings of the guild, can be overridden for custom settings.
 *
 * ## Example
 * @sample dev.augu.nino.butterfly.examples.CustomSettings
 * @property prefix the custom prefix of the guild, if null it will be ignored
 */
open class GuildSettings(open val prefix: String?)