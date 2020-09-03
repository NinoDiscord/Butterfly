package dev.augu.nino.butterfly

import club.minnced.jda.reactor.on
import dev.augu.nino.butterfly.command.Command
import dev.augu.nino.butterfly.command.CommandErrorHandler
import dev.augu.nino.butterfly.i18n.I18nLanguage
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.sharding.ShardManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * The Butterfly client manager
 *
 * This helps with managing shards using [ButterflyClient]s.
 * This class has the same API as the normal [ButterflyClient], making this class control all [ButterflyClient]s below him.
 *
 *
 * ## Example:
 * @sample dev.augu.nino.butterfly.examples.ShardedExampleBot
 * @property shardManager the [ShardManager] instance
 * @property ownerIds the owners' id, necessary for the command system to function
 * @param invokeOnMessageEdit whether to invoke on message edit or not
 * @param useDefaultHelpCommand whether to use the default help command or not
 * @property defaultLanguage the default language to use
 * @property guildSettingsLoader a [GuildSettings] loader
 */
class ButterflyClientManager(
    val shardManager: ShardManager,
    override val ownerIds: Array<String>,
    private val invokeOnMessageEdit: Boolean,
    private val useDefaultHelpCommand: Boolean,
    override val defaultLanguage: I18nLanguage?,
    override val guildSettingsLoader: GuildSettingsLoader<*>
) : IButterflyClient {
    private val butterflyClients: ConcurrentMap<Int, ButterflyClient> = ConcurrentHashMap()

    override val commands: MutableMap<String, Command> = mutableMapOf()

    override val aliases: MutableMap<String, Command> = mutableMapOf()

    override val languages: MutableMap<String, I18nLanguage> = mutableMapOf()

    override val prefixes: MutableList<String> = mutableListOf()

    override val prefixLoaders: MutableList<suspend (Message) -> String?> = mutableListOf()

    override val commandErrorHandlers: MutableList<CommandErrorHandler> = mutableListOf()

    init {
        shardManager.on<ReadyEvent>().subscribe { readyEvent: ReadyEvent ->
            val jda = readyEvent.jda
            val shardInfo = jda.shardInfo

            if (shardInfo.shardId !in butterflyClients) {
                butterflyClients[shardInfo.shardId] = ButterflyClient.builder(jda, ownerIds)
                    .let {
                        it.defaultLanguage = defaultLanguage
                        it.invokeOnMessageEdit = invokeOnMessageEdit
                        it.useDefaultHelpCommand = useDefaultHelpCommand
                        it.guildSettingsLoader = guildSettingsLoader
                        it
                    }
                    .addCommands(*commands.values.toTypedArray())
                    .addLanguages(*languages.values.toTypedArray())
                    .addPrefixes(*prefixes.toTypedArray())
                    .addPrefixLoaders(*prefixLoaders.toTypedArray())
                    .addCommandErrorHandlers(*commandErrorHandlers.toTypedArray())
                    .build()
            }
        }
    }

    override fun addCommand(command: Command, vararg commands: Command) {
        addCommandInternal(command)

        commands.forEach(this::addCommandInternal)

        for ((_, butterflyClient) in butterflyClients) {
            butterflyClient.addCommand(command, *commands)
        }
    }

    private fun addCommandInternal(cmd: Command) {
        commands[cmd.name] = cmd

        for (alias in cmd.aliases) {
            aliases[alias] = cmd
        }
    }

    override fun addLanguage(language: I18nLanguage, vararg languages: I18nLanguage) {
        addLanguageInternal(language)

        languages.forEach(this::addLanguageInternal)

        for ((_, butterflyClient) in butterflyClients) {
            butterflyClient.addLanguage(language, *languages)
        }
    }

    private fun addLanguageInternal(language: I18nLanguage) {
        languages[language.name] = language
    }

    override fun addPrefix(prefix: String, vararg prefixes: String) {
        this.prefixes.add(prefix)
        this.prefixes.addAll(prefixes)

        for ((_, butterflyClient) in butterflyClients) {
            butterflyClient.addPrefix(prefix, *prefixes)
        }
    }

    override fun addPrefixLoader(loader: suspend (Message) -> String?, vararg loaders: suspend (Message) -> String?) {
        this.prefixLoaders.add(loader)
        this.prefixLoaders.addAll(loaders)

        for ((_, butterflyClient) in butterflyClients) {
            butterflyClient.addPrefixLoader(loader, *loaders)
        }
    }

    override fun addErrorHandler(handler: CommandErrorHandler, vararg handlers: CommandErrorHandler) {
        for ((_, butterflyClient) in butterflyClients) {
            butterflyClient.addErrorHandler(handler, *handlers)
        }
    }

    companion object {
        /**
         * Creates a new [Builder]
         *
         * @param shardManager the [ShardManager] instance
         * @param ownerIds the owners' id
         * @return a new [Builder]
         */
        fun builder(shardManager: ShardManager, ownerIds: Array<String>): Builder {
            return Builder(shardManager, ownerIds)
        }

        /**
         * Builds a [ButterflyClientManager]
         *
         * This class adds Java interoperability.
         * @property shardManager the [ShardManager] instance
         * @property ownerIds the owners' id
         */
        class Builder internal constructor(var shardManager: ShardManager, var ownerIds: Array<String>) {
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
             * Builds the [ButterflyClientManager]
             * @return the [ButterflyClientManager] instance
             */
            fun build(): ButterflyClientManager {
                val client = ButterflyClientManager(
                    shardManager,
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