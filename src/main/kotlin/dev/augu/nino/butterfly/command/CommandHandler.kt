package dev.augu.nino.butterfly.command

import dev.augu.nino.butterfly.ButterflyClient
import dev.augu.nino.butterfly.GuildSettings
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message

/**
 * The command handler.
 *
 * This class is built in the [ButterflyClient].
 */
class CommandHandler(private val client: ButterflyClient) {

    companion object {
        /**
         * Verifies that the command can be ran in the message's environment
         *
         * @since 0.2.0
         * @param message the message sent
         * @param command the command to run
         * @param client the [ButterflyClient] instance
         */
        fun verify(message: Message, command: Command, client: ButterflyClient) {

            if (command.guildOnly && !message.isFromGuild) {
                throw CommandException(NotInGuildError(message, command))
            }

            if (command.ownerOnly && client.ownerIds.all { message.author.id != it }) {
                throw CommandException(NotOwnerError(message, command))
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
        }
    }

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
        val settings: GuildSettings?
        if (message.isFromGuild) {
            settings = client.guildSettingsLoader.load(message.guild)
        } else {
            settings = null
        }

        val prefixes =
            (client.prefixes + (client.prefixLoaders).mapNotNull { it(message) }).toMutableList() // get prefixes
        if (settings?.prefix != null) {
            prefixes += settings.prefix!!
        }
        val prefix = prefixes.find { content.startsWith(it) } ?: return // find if one of the prefixes matches
        content = content.removePrefix(prefix).trimStart()

        val commandname = content.split(" ")[0]
        val command = client.commands[commandname] ?: client.aliases[commandname] ?: return
        content = content.removePrefix(commandname).trimStart()

        verify(message, command, client)

        val ctx =
            CommandContext(message, command, content.split(" ").filterNot { it == "" }.toTypedArray(), prefix, client)

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