package dev.augu.nino.butterfly.command

import dev.augu.nino.butterfly.ButterflyClient
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message

/**
 * The command handler.
 *
 * This class is built in the [ButterflyClient].
 */
class CommandHandler(private val client: ButterflyClient) {

    /**
     * Invokes the handler
     *
     * This method is called on any message received.
     *
     * @param message the message received
     */
    suspend fun invoke(message: Message) {
        if (message.author.isBot) return

        var content = message.contentRaw

        val prefixes = client.prefixes + client.prefixGetters.mapNotNull { it(message) } // get prefixes
        val prefix = prefixes.find { content.startsWith(it) } ?: return // find if one of the prefixes matches
        content = content.removePrefix(prefix).trimStart()

        val commandname = content.split(" ")[0]
        val command = client.commands[commandname] ?: client.aliases[commandname] ?: return
        content = content.removePrefix(commandname).trimStart()

        if (command.guildOnly && !message.isFromGuild) {
            throw CommandException(NotInGuildError(message, command))
        }

        if (message.isFromGuild &&
            !message.member!!.hasPermission(
                message.textChannel,
                Permission.getPermissions(command.userPermissions)
            )
        ) {
            throw CommandException(
                InsufficientUserPermissionsError(
                    message,
                    command, command.userPermissions, Permission.getRaw(message.member!!.permissions)
                )
            )
        }

        if (message.isFromGuild &&
            !message.guild.selfMember.hasPermission(
                message.textChannel,
                Permission.getPermissions(command.botPermissions)
            )
        ) {
            throw CommandException(
                InsufficientBotPermissionsError(
                    message,
                    command, command.botPermissions, Permission.getRaw(message.guild.selfMember.permissions)
                )
            )
        }

        val ctx = CommandContext(message, command, content.split(" ").toTypedArray(), prefix, client)

        command.execute(ctx)
    }
}

/**
 * Exception thrown on command errors.
 *
 * When this exception is thrown inside a command, the client will forward the error to all [CommandErrorHandler] registered.
 * @param error the command error
 */
data class CommandException(val error: CommandError) : RuntimeException(error.reason) {

}