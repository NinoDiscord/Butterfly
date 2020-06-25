package dev.augu.nino.butterfly.command

import net.dv8tion.jda.api.entities.Message

/**
 * An error thrown when a user invokes a guild-only command not through a guild channel.
 *
 * @property message the message that caused the error
 * @property command the command that had the error
 */
data class NotInGuildError(
    override val message: Message,
    override val command: Command
) : CommandError(message, command, "Guild-only command invoked in non-guild environment.")

/**
 * An error thrown when a user invokes a command he does not have the required permissions for.
 *
 * @property message the message that caused the error
 * @property command the command that had the error
 * @property requiredPermissions the permissions the user needs to run the command
 * @property actualPermissions the permissions the user had
 */
data class InsufficientUserPermissionsError(
    override val message: Message,
    override val command: Command,
    val requiredPermissions: Long,
    val actualPermissions: Long
) : CommandError(message, command, "User has insufficient permissions.")

/**
 * An error thrown when a user invokes a command the bot does not have the required permissions for.
 *
 * @property message the message that caused the error
 * @property command the command that had the error
 * @property requiredPermissions the permissions the bot needs to run the command
 * @property actualPermissions the permissions the bot had
 */
data class InsufficientBotPermissionsError(
    override val message: Message,
    override val command: Command,
    val requiredPermissions: Long,
    val actualPermissions: Long
) : CommandError(message, command, "Bot has insufficient permissions.")