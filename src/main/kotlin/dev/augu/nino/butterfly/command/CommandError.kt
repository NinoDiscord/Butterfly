package dev.augu.nino.butterfly.command

import net.dv8tion.jda.api.entities.Message

/**
 * A command error
 *
 * This class is returned on any command error.
 *
 * Users can inherit this command in-order to catch command errors using the [CommandErrorHandler]
 *
 * @property message the message that caused the error
 * @property command the command that had the error
 * @property reason a string representing the issue that caused the error
 */
abstract class CommandError(open val message: Message, open val command: Command, open val reason: String?) {
    override fun toString(): String {
        return "CommandError { Guild ${if (message.isFromGuild) message.guild.id else "Not in a guild"}, " +
                "Author ${message.author.id}, " +
                "Reason: ${reason ?: "No reason specified"} " +
                "}"
    }
}

/**
 * A handler that handles errors thrown in commands.
 *
 * This class should be overridden by the users and passed to the client.
 */
abstract class CommandErrorHandler {
    /**
     * Invokes the handler.
     *
     * This method is called when an error occurred.
     * @param error the error passed
     */
    abstract suspend fun invoke(error: CommandError)
}