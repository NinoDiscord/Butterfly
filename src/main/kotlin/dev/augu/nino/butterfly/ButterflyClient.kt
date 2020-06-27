package dev.augu.nino.butterfly

import dev.augu.nino.butterfly.command.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * The Butterfly client
 *
 * This class wraps the normal JDA client and allows access to all of Butterfly's subprojects at ease.
 *
 * ## Example:
 * @sample dev.augu.nino.butterfly.examples.ExampleBot
 * @property jda the JDA instance
 * @property ownerId the owner's id, necessary for the command system to function
 * @property scope the scope on which to run the coroutines
 * @property guildSettingsLoader a [GuildSettings] loader, by default it loads an empty settings.
 * This function is called by the [CommandHandler] in order to add the guild prefix to the list.
 * @constructor creates a new ButterflyClient
 * @param invokeOnMessageEdit whether to invoke on message edit or not
 */
class ButterflyClient(
    val jda: JDA,
    val ownerId: String,
    invokeOnMessageEdit: Boolean = false,
    useDefaultHelpCommand: Boolean = true,
    val scope: CoroutineScope = GlobalScope,
    val guildSettingsLoader: GuildSettingsLoader<*> = object :
        GuildSettingsLoader<GuildSettings> {
        override suspend fun load(guild: Guild): GuildSettings = GuildSettings(null)
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
    private fun invokeAndCatch(msg: Message) {
        scope.launch {
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
    }

    init {
        eventManager.register(object : ListenerAdapter() {
            override fun onMessageReceived(event: MessageReceivedEvent) {
                super.onMessageReceived(event)
                invokeAndCatch(event.message)
            }

            override fun onMessageUpdate(event: MessageUpdateEvent) {
                super.onMessageUpdate(event)
                if (invokeOnMessageEdit) {
                    invokeAndCatch(event.message)
                }
            }
        })
        if (useDefaultHelpCommand) {
            addCommand(DefaultHelpCommand())
        }
    }

    /**
     * Adds a command
     *
     * @param cmd the command to add
     */
    fun addCommand(cmd: Command) {
        commands[cmd.name] = cmd
        for (alias in cmd.aliases) {
            aliases[alias] = cmd
        }
    }

    /**
     * Adds a prefix
     *
     * @param prefix the prefix to add
     */
    fun addPrefix(prefix: String) {
        prefixes.add(prefix)
    }

    /**
     * Adds a prefix getter
     *
     * Prefix getters are suspended functions that return a prefix.
     *
     * @param getter the prefix getter to add
     */
    fun addPrefixGetter(getter: suspend (Message) -> String) {
        prefixLoaders.add(getter)
    }

    /**
     * Adds an error handler
     *
     * Error handlers are special classes that can handle command errors
     *
     * @param handler the command handler to add
     */
    fun addErrorHandler(handler: CommandErrorHandler) {
        commandErrorHandlers.add(handler)
    }

    /**
     * Connects the butterfly client with the JDA instance.
     *
     * @return the ButterflyClient instance
     */
    fun JDA.client(): ButterflyClient = this@ButterflyClient

    companion object {
        fun builder(jda: JDA, ownerId: String): Builder {
            return Builder(jda, ownerId)
        }

        class Builder internal constructor(var jda: JDA, var ownerId: String) {
            var invokeOnMessageEdit: Boolean = false
            var useDefaultHelpCommand: Boolean = true
            var scope: CoroutineScope = GlobalScope
            var guildSettingsLoader: GuildSettingsLoader<*> = object :
                GuildSettingsLoader<GuildSettings> {
                override suspend fun load(guild: Guild): GuildSettings = GuildSettings(null)
            }

            fun build(): ButterflyClient = ButterflyClient(
                jda,
                ownerId,
                invokeOnMessageEdit,
                useDefaultHelpCommand,
                scope,
                guildSettingsLoader
            )

        }
    }
}