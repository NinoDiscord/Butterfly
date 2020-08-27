package dev.augu.nino.butterfly

import club.minnced.jda.reactor.on
import dev.augu.nino.butterfly.command.*
import dev.augu.nino.butterfly.i18n.I18nLanguage
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.GenericMessageEvent
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
 * @property guildSettingsLoader a [GuildSettings] loader
 */
class ButterflyClient(
    val jda: JDA,
    override val ownerIds: Array<String>,
    invokeOnMessageEdit: Boolean = false,
    useDefaultHelpCommand: Boolean = true,
    override val defaultLanguage: I18nLanguage? = null,
    override val guildSettingsLoader: GuildSettingsLoader<*> = object :
        GuildSettingsLoader<GuildSettings> {
        override suspend fun load(guild: Guild): GuildSettings = GuildSettings(null, null)
    }
) : IButterflyClient {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val handler: CommandHandler = CommandHandler(this)

    /**
     * Invokes the [CommandHandler] and catches errors
     */
    private fun invokeAndCatch(msg: Message, event: GenericMessageEvent): Mono<Unit> = mono {
        try {
            handler.invoke(msg, event)
        } catch (c: CommandException) { // CommandExceptions can be handled by the user.
            for (errorHandler in commandErrorHandlers) {
                errorHandler.invoke(c.error)
            }
            if (commandErrorHandlers.isEmpty()) {
                logger.error("CommandClient: Uncaught error during execution of command: ${c.localizedMessage}")
            }
        }
    }

    override val commands: MutableMap<String, Command> = HashMap()


    override val aliases: MutableMap<String, Command> = HashMap()


    override val languages: MutableMap<String, I18nLanguage> = HashMap()


    override val prefixes: MutableList<String> = ArrayList()


    override val prefixLoaders: MutableList<suspend (Message) -> String?> = ArrayList()


    override val commandErrorHandlers: MutableList<CommandErrorHandler> = ArrayList()

    init {
        jda.on<MessageReceivedEvent>().subscribe {
            invokeAndCatch(it.message, it).subscribe()
        }
        if (invokeOnMessageEdit) {
            jda.on<MessageUpdateEvent>().subscribe {
                invokeAndCatch(it.message, it).subscribe()
            }
        }

        if (useDefaultHelpCommand) {
            addCommand(DefaultHelpCommand())
        }
    }

    override fun addCommand(command: Command, vararg commands: Command) {
        this.commands[command.name] = command

        for (alias in command.aliases) {
            aliases[alias] = command
        }

        commands.forEach { addCommand(it) }
    }

    override fun addLanguage(language: I18nLanguage, vararg languages: I18nLanguage) {
        this.languages[language.name] = language

        languages.forEach { addLanguage(it) }
    }

    override fun addPrefix(prefix: String, vararg prefixes: String) {
        this.prefixes.add(prefix)
        for (prefx in prefixes) {
            this.prefixes.add(prefx)
        }
    }

    override fun addPrefixLoader(loader: suspend (Message) -> String?, vararg loaders: suspend (Message) -> String?) {
        prefixLoaders.add(loader)
        for (gtr in loaders) {
            prefixLoaders.add(gtr)
        }
    }

    override fun addErrorHandler(handler: CommandErrorHandler, vararg handlers: CommandErrorHandler) {
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
         * @param invokeOnMessageEdit Whether to invoke on message edit or not
         * @param useDefaultHelpCommand Whether to use the default help command or not.
         * @param defaultLanguage The default language to use.
         * @param guildSettingsLoader A [GuildSettings] loader, by default it loads an empty settings.
         * @return a new [Builder]
         */
        fun builder(
            jda: JDA,
            ownerIds: Array<String>,
            invokeOnMessageEdit: Boolean = false,
            useDefaultHelpCommand: Boolean = true,
            defaultLanguage: I18nLanguage? = null,
            guildSettingsLoader: GuildSettingsLoader<*> = object :
                GuildSettingsLoader<GuildSettings> {
                override suspend fun load(guild: Guild): GuildSettings = GuildSettings(null, null)
            }
        ): Builder {
            return Builder(
                jda,
                ownerIds,
                invokeOnMessageEdit,
                useDefaultHelpCommand,
                defaultLanguage,
                guildSettingsLoader
            )
        }

        /**
         * Builds a [ButterflyClient]
         *
         * This class adds Java interoperability.
         * @property jda the [JDA] instance
         * @property ownerIds the owners' id
         * @property invokeOnMessageEdit Whether to invoke on message edit or not
         * @property useDefaultHelpCommand Whether to use the default help command or not.
         * @property defaultLanguage The default language to use.
         * @property guildSettingsLoader A [GuildSettings] loader, by default it loads an empty settings.
         */
        class Builder internal constructor(
            var jda: JDA,
            var ownerIds: Array<String>,
            var invokeOnMessageEdit: Boolean = false,
            var useDefaultHelpCommand: Boolean = true,
            var defaultLanguage: I18nLanguage? = null,
            var guildSettingsLoader: GuildSettingsLoader<*> = object :
                GuildSettingsLoader<GuildSettings> {
                override suspend fun load(guild: Guild): GuildSettings = GuildSettings(null, null)
            }
        ) {


            private val commandList: MutableList<Command> = arrayListOf()

            /**
             * Add commands
             *
             * The commands will be added after client construction.
             *
             * @since 0.3
             * @param commands An array of commands to add
             * @return this builder instance
             */
            fun addCommands(vararg commands: Command): Builder {
                commandList.addAll(commands)
                return this
            }

            private val languageList: MutableList<I18nLanguage> = arrayListOf()

            /**
             * Add languages
             *
             * The languages will be added after client construction.
             *
             * @since 0.3
             * @param languages An array of languages to add
             * @return this builder instance
             */
            fun addLanguages(vararg languages: I18nLanguage): Builder {
                languageList.addAll(languages)
                return this
            }

            private val prefixList: MutableList<String> = arrayListOf()

            /**
             * Add prefixes
             *
             * The prefixes will be added after client construction.
             *
             * @since 0.3
             * @param prefixes An array of prefixes to add
             * @return this builder instance
             */
            fun addPrefixes(vararg prefixes: String): Builder {
                prefixList.addAll(prefixes)
                return this
            }

            private val prefixLoaderList: MutableList<suspend (Message) -> String?> = arrayListOf()

            /**
             * Add prefix loaders
             *
             * The prefix loaders will be added after client construction.
             *
             * @since 0.3
             * @param prefixLoaders An array of prefix loaders to add
             * @return this builder instance
             */
            fun addPrefixLoaders(vararg prefixLoaders: suspend (Message) -> String?): Builder {
                prefixLoaderList.addAll(prefixLoaders)
                return this
            }

            private val commandErrorHandlerList: MutableList<CommandErrorHandler> = arrayListOf()

            /**
             * Add command error handlers
             *
             * The command error handlers will be added after client construction.
             *
             * @since 0.3
             * @param commandErrorHandlers An array of command error handlers to add
             * @return this builder instance
             */
            fun addCommandErrorHandlers(vararg commandErrorHandlers: CommandErrorHandler): Builder {
                commandErrorHandlerList.addAll(commandErrorHandlers)
                return this
            }

            /**
             * Builds the [ButterflyClient]
             * @return the [ButterflyClient] instance
             */
            fun build(): ButterflyClient {
                val client = ButterflyClient(
                    jda,
                    ownerIds,
                    invokeOnMessageEdit,
                    useDefaultHelpCommand,
                    defaultLanguage,
                    guildSettingsLoader
                )
                if (commandList.isNotEmpty()) {
                    client.addCommand(commandList.first(), *commandList.drop(1).toTypedArray())
                }
                if (languageList.isNotEmpty()) {
                    client.addLanguage(languageList.first(), *languageList.drop(1).toTypedArray())
                }
                if (prefixList.isNotEmpty()) {
                    client.addPrefix(prefixList.first(), *prefixList.drop(1).toTypedArray())
                }
                if (prefixLoaderList.isNotEmpty()) {
                    client.addPrefixLoader(prefixLoaderList.first(), *prefixLoaderList.drop(1).toTypedArray())
                }
                if (commandErrorHandlerList.isNotEmpty()) {
                    client.addErrorHandler(
                        commandErrorHandlerList.first(),
                        *commandErrorHandlerList.drop(1).toTypedArray()
                    )
                }
                return client
            }

        }
    }
}