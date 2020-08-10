package dev.augu.nino.butterfly

import club.minnced.jda.reactor.on
import dev.augu.nino.butterfly.command.*
import dev.augu.nino.butterfly.i18n.I18nLanguage
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono


/**
 * The Butterfly client
 *
 * This class wraps the normal JDA client and allows access to all of Butterfly's subprojects at ease.
 *
 * ## Example:
 * @sample dev.augu.nino.butterfly.examples.ExampleBot
 * @property jda the JDA instance
 * @property ownerIds the owners' id, necessary for the command system to function
 * @param invokeOnMessageEdit whether to invoke on message edit or not
 * @param useDefaultHelpCommand whether to use the default help command or not
 * @property defaultLanguage the default language to use
 * @property guildSettingsLoader a [GuildSettings] loader, by default it loads an empty settings.
 * This function is called by the [CommandHandler] in order to add the guild prefix to the list.
 * @constructor creates a new ButterflyClient
 */
class ButterflyClient(
    val jda: JDA,
    val ownerIds: Array<String>,
    invokeOnMessageEdit: Boolean = false,
    useDefaultHelpCommand: Boolean = true,
    val defaultLanguage: I18nLanguage? = null,
    val guildSettingsLoader: GuildSettingsLoader<*> = object :
        GuildSettingsLoader<GuildSettings> {
        override suspend fun load(guild: Guild): GuildSettings = GuildSettings(null, null)
    }
) : JDA by jda {

    /**
     * A map of commands
     */
    val commands: MutableMap<String, Command> = HashMap()

    /**
     * A map of commands by their aliases
     */
    val aliases: MutableMap<String, Command> = HashMap()

    /**
     * A map of languages by their names
     */
    val languages: MutableMap<String, I18nLanguage> = HashMap()

    /**
     * A list of prefixes
     */
    val prefixes: MutableList<String> = ArrayList()

    /**
     * A list of prefixGetters
     */
    val prefixLoaders: MutableList<suspend (Message) -> String?> = ArrayList()

    /**
     * A list of [CommandErrorHandler]s
     */
    val commandErrorHandlers: MutableList<CommandErrorHandler> = ArrayList()

    /**
     * Logger
     */
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val handler: CommandHandler =
        CommandHandler(this)

    /**
     * Invokes the [CommandHandler] and catches errors
     */
    private fun invokeAndCatch(msg: Message): Mono<Unit> = mono {
        try {
            handler.invoke(msg)
        } catch (c: CommandException) { // CommandExceptions can be handled by the user.
            for (errorHandler in commandErrorHandlers) {
                errorHandler.invoke(c.error)
            }
            if (commandErrorHandlers.isEmpty()) {
                logger.error("CommandClient: Uncaught error during execution of command: ${c.localizedMessage}")
            }
        } catch (e: Exception) { // Makes sure not to crash on any command error
            logger.error("CommandClient: Uncaught error during execution of command: ${e.localizedMessage}")
        }
    }

    init {
        jda.on<MessageReceivedEvent>().subscribe {
            invokeAndCatch(it.message).subscribe()
        }
        if (invokeOnMessageEdit) {
            jda.on<MessageUpdateEvent>().subscribe {
                invokeAndCatch(it.message).subscribe()
            }
        }

        if (useDefaultHelpCommand) {
            addCommand(DefaultHelpCommand())
        }
    }

    /**
     * Adds a command
     *
     * @param cmd the command to add
     * @param cmds more commands to add
     */
    fun addCommand(cmd: Command, vararg cmds: Command) {
        commands[cmd.name] = cmd
        for (alias in cmd.aliases) {
            aliases[alias] = cmd
        }
        for (command in cmds) {
            addCommand(command)
        }
    }

    /**
     * Adds a language
     *
     * @param name the name of the language
     * @param language the language
     */
    fun addLanguage(name: String, language: I18nLanguage) {
        languages[name] = language
    }

    /**
     * Adds a prefix
     *
     * @param prefix the prefix to add
     * @param prefixes additional prefixes to add
     */
    fun addPrefix(prefix: String, vararg prefixes: String) {
        this.prefixes.add(prefix)
        for (pfix in prefixes) {
            this.prefixes.add(pfix)
        }
    }

    /**
     * Adds a prefix getter
     *
     * Prefix getters are suspended functions that return a prefix.
     *
     * @param getter the prefix getter to add
     * @param getters additional prefix getters to add
     */
    fun addPrefixGetter(getter: suspend (Message) -> String, vararg getters: suspend (Message) -> String) {
        prefixLoaders.add(getter)
        for (gtr in getters) {
            prefixLoaders.add(gtr)
        }
    }

    /**
     * Adds an error handler
     *
     * Error handlers are special classes that can handle command errors
     *
     * @param handler the command error handler to add
     * @param handlers additional command error handlers to add
     */
    fun addErrorHandler(handler: CommandErrorHandler, vararg handlers: CommandErrorHandler) {
        commandErrorHandlers.add(handler)
        for (hndlr in handlers) {
            commandErrorHandlers.add(hndlr)
        }
    }

    companion object {
        /**
         * Creates a new [Builder]
         *
         * @param jda the [JDA] instance
         * @param ownerIds the owners' id
         * @return a new [Builder]
         */
        fun builder(jda: JDA, ownerIds: Array<String>): Builder {
            return Builder(jda, ownerIds)
        }

        /**
         * Builds a [ButterflyClient]
         *
         * This class adds Java interoperability.
         * @property jda the [JDA] instance
         * @property ownerIds the owners' id
         */
        class Builder internal constructor(var jda: JDA, var ownerIds: Array<String>) {
            /**
             * Whether to invoke on message edit or not
             */
            var invokeOnMessageEdit: Boolean = false

            /**
             * Whether to use the default help command or not.
             */
            var useDefaultHelpCommand: Boolean = true

            /**
             * The default language to use.
             */
            var defaultLanguage: I18nLanguage? = null

            /**
             * A [GuildSettings] loader, by default it loads an empty settings.
             */
            var guildSettingsLoader: GuildSettingsLoader<*> = object :
                GuildSettingsLoader<GuildSettings> {
                override suspend fun load(guild: Guild): GuildSettings = GuildSettings(null, null)
            }

            /**
             * Builds the [ButterflyClient]
             * @return the [ButterflyClient] instance
             */
            fun build(): ButterflyClient = ButterflyClient(
                jda,
                ownerIds,
                invokeOnMessageEdit,
                useDefaultHelpCommand,
                defaultLanguage,
                guildSettingsLoader
            )

        }
    }
}