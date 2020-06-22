package dev.augu.nino.butterfly.command

import net.dv8tion.jda.api.entities.Message

abstract class CommandError(open val message: Message, open val command: Command, open val reason: String?) {
    override fun toString(): String {
        return "CommandError { Guild ${if (message.isFromGuild) message.guild.id else "Not in a guild"}, " +
                "Author ${message.author.id}, " +
                "Reason: ${reason ?: "No reason specified"} " +
                "}"
    }
}

abstract class CommandErrorHandler(client: ButterflyClient) {
    abstract suspend fun invoke(error: CommandError)
}