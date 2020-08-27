package dev.augu.nino.butterfly

import net.dv8tion.jda.api.entities.Guild

/**
 * Loads [GuildSettings] from a [Guild] instance
 *
 * This class is generic, letting users define their own settings type.
 *
 * ## Example
 * @sample dev.augu.nino.butterfly.examples.CustomSettingsLoader
 * @param T the settings type
 */
interface GuildSettingsLoader<out T : GuildSettings> {
    /**
     * Loads the [GuildSettings] for the given guild
     *
     * @param guild the guild to load the settings of
     * @return the settings
     */
    suspend fun load(guild: Guild): T?
}