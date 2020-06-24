package dev.augu.nino.butterfly.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ButterflyClient(val jda: JDA, invokeOnMessageEdit: Boolean = false, val scope: CoroutineScope) : JDA by jda {
    val commands: HashMap<String, Command> = HashMap()
    val aliases: HashMap<String, Command> = HashMap()
    val prefixes: ArrayList<String> = ArrayList()
    val prefixGetters: ArrayList<suspend (Message) -> String?> = ArrayList()
    val commandErrorHandlers: ArrayList<CommandErrorHandler> = ArrayList()
    private val handler: CommandHandler = CommandHandler(this)

    private fun invokeAndCatch(msg: Message) {
        scope.launch {
            try {
                handler.invoke(msg)
            } catch (c: CommandException) {
                for (errorHandler in commandErrorHandlers) {
                    errorHandler.invoke(c.error)
                }
            } catch (e: Exception) {
                println("CommandClient: Uncaught error during execution of command: ${e.localizedMessage}")
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

    }

    fun addCommand(cmd: Command) {
        commands[cmd.name] = cmd
        for (alias in cmd.aliases) {
            aliases[alias] = cmd
        }
    }

    fun addPrefix(prefix: String) {
        prefixes.add(prefix)
    }

    fun addPrefixGetter(getter: suspend (Message) -> String) {
        prefixGetters.add(getter)
    }

    fun addErrorHandler(handler: CommandErrorHandler) {
        commandErrorHandlers.add(handler)
    }
}