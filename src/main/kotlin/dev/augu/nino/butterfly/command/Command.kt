package dev.augu.nino.butterfly.command

import net.dv8tion.jda.api.Permission

/**
 * The Command Class
 * Represents a command
 */
abstract class Command(
    var name: String,
    var category: String,
    vararg val aliases: String,
    val guildOnly: Boolean = true,
    val userPermissions: Long = 0,
    val botPermissions: Long = Permission.getRaw(Permission.MESSAGE_WRITE)
) {

    /**
     *  The command execution function
     */
    abstract suspend fun execute(ctx: CommandContext)


}