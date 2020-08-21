package dev.augu.nino.butterfly

import dev.augu.nino.butterfly.command.Command
import dev.augu.nino.butterfly.command.CommandErrorHandler
import dev.augu.nino.butterfly.command.CommandHandler
import dev.augu.nino.butterfly.i18n.I18nLanguage
import net.dv8tion.jda.api.entities.Message

/**
 * Defines the interface of the ButterflyClient, allows for custom implementations and to have a common interface between shards and shard managers.
 *
 * @since 0.3
 */
interface IButterflyClient {
    /**
     * The owners' id, necessary for the command system to function
     */
    val ownerIds: Array<String>

    /**
     * The default language to use
     */
    val defaultLanguage: I18nLanguage?

    /**
     * A map of commands
     */
    val commands: MutableMap<String, Command>

    /**
     * A map of commands by their aliases
     */
    val aliases: MutableMap<String, Command>

    /**
     * A map of languages by their names
     */
    val languages: MutableMap<String, I18nLanguage>

    /**
     * A list of prefixes
     */
    val prefixes: MutableList<String>

    /**
     * A list of prefixGetters
     */
    val prefixLoaders: MutableList<suspend (Message) -> String?>

    /**
     * A list of [CommandErrorHandler]s
     */
    val commandErrorHandlers: MutableList<CommandErrorHandler>

    /**
     * A [GuildSettings] loader, by default it loads an empty settings.
     * This function is called by the [CommandHandler] in order to add the guild prefix to the list.
     */
    val guildSettingsLoader: GuildSettingsLoader<*>

    /**
     * Adds a command
     *
     * @param command the command to add
     * @param commands more commands to add
     */
    fun addCommand(command: Command, vararg commands: Command)

    /**
     * Adds a language
     *
     * @param language the language
     * @param languages more languages to add
     */
    fun addLanguage(language: I18nLanguage, vararg languages: I18nLanguage)

    /**
     * Adds a prefix
     *
     * @param prefix the prefix to add
     * @param prefixes additional prefixes to add
     */
    fun addPrefix(prefix: String, vararg prefixes: String)

    /**
     * Adds a prefix getter
     *
     * Prefix getters are suspended functions that return a prefix.
     *
     * @param loader the prefix getter to add
     * @param loaders additional prefix getters to add
     */
    fun addPrefixLoader(loader: suspend (Message) -> String?, vararg loaders: suspend (Message) -> String?)

    /**
     * Adds an error handler
     *
     * Error handlers are special classes that can handle command errors
     *
     * @param handler the command error handler to add
     * @param handlers additional command error handlers to add
     */
    fun addErrorHandler(handler: CommandErrorHandler, vararg handlers: CommandErrorHandler)
}