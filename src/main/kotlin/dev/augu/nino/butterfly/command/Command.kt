package dev.augu.nino.butterfly.command

import net.dv8tion.jda.api.Permission

/**
 * The Command Class
 *
 * This class represents a command.
 *
 * class Ez : Command(
 *   "test", "generic", "teest",
 *   guildOnly = true,
 *   userPermissions = Permission.getRaw(Permission.MESSAGE_WRITE),
 *   botPermissions = Permission.getRaw(Permission.MESSAGE_WRITE)
 *   ) {
 *
 *   override suspend fun execute(ctx: CommandContext) {
 *     ctx.reply("test")
 *   }
 * }
 *
 * @property name the command name
 * @property category the category
 * @property aliases the aliases of the command
 * @property guildOnly whether to run the command only on guilds or not
 * @property userPermissions the required permissions for the user
 * @property botPermissions the required permissions for the bot
 */
abstract class Command(
    val name: String,
    val category: String,
    vararg val aliases: String,
    val guildOnly: Boolean = true,
    val userPermissions: Long = 0,
    val botPermissions: Long = Permission.getRaw(Permission.MESSAGE_WRITE)
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