package dev.augu.nino.butterfly.command

import net.dv8tion.jda.api.entities.Message

data class NotInGuildError(
    override val message: Message,
    override val command: Command
) : CommandError(message, command, "Guild-only command invoked in ")

data class InsufficientUserPermissionsError(
    override val message: Message,
    override val command: Command,
    val requiredPermissions: Long,
    val actualPermissions: Long
) : CommandError(message, command, "User has insufficient permissions.") {}

data class InsufficientBotPermissionsError(
    override val message: Message,
    override val command: Command,
    val requiredPermissions: Long,
    val actualPermissions: Long
) : CommandError(message, command, "Bot has insufficient permissions.") {}