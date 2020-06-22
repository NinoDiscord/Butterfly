package dev.augu.nino.butterfly.command

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message

class CommandHandler(val client: ButterflyClient) {
    suspend fun invoke(message: Message) {
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
            !message.member!!.hasPermission(message.textChannel, Permission.getPermissions(command.userPermissions))
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
                    command, command.botPermissions, Permission.getRaw(message.guild.selfMember!!.permissions)
                )
            )
        }

        val ctx = CommandContext(message, command, content.split(" ").toTypedArray(), prefix)

        command.execute(ctx)
    }
}

data class CommandException(val error: CommandError) : RuntimeException(error.reason) {

}