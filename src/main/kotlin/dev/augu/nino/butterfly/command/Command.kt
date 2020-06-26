package dev.augu.nino.butterfly.command

import net.dv8tion.jda.api.Permission

/**
 * The Command Class
 *
 * This class represents a command.
 *
 * ## Examples:
 * @sample dev.augu.nino.butterfly.examples.EchoCommand
 * @sample dev.augu.nino.butterfly.examples.PingCommand
 *
 * @property name the command name
 * @property category the category
 * @property aliases the aliases of the command
 * @property description the command description, shown in the help command
 * @property guildOnly whether to run the command only on guilds or not
 * @property ownerOnly whether this command can be used only by the bot owner
 * @property userPermissions the required permissions for the user
 * @property botPermissions the required permissions for the bot
 * @property visible whether the command is visible in the help command
 */
abstract class Command(
    val name: String,
    val category: String,
    vararg val aliases: String,
    val description: String = "",
    val guildOnly: Boolean = true,
    val ownerOnly: Boolean = false,
    val userPermissions: Long = 0,
    val botPermissions: Long = Permission.getRaw(Permission.MESSAGE_WRITE),
    val visible: Boolean = true
) {

    /**
     *  Executes the command
     *
     *  Given a context, this function will be called on command invocation.
     *
     *  @param ctx the command context
     */
    abstract suspend fun execute(ctx: CommandContext)


}