package dev.augu.nino.butterfly.command

import net.dv8tion.jda.api.Permission

/**
 * A group of commands, allows command nesting.
 *
 * @since 0.2.0
 * @property commands A list of commands to have in the group.
 * @property defaultCommand The default command to use if a command with that name is not found.
 *
 * @constructor Creates a new command group
 * @param name the command name
 * @param category the category
 * @param aliases the aliases of the command
 * @param description the command description, shown in the help command
 * @param guildOnly whether to run the command only on guilds or not
 * @param ownerOnly whether this command can be used only by the bot owner
 * @param userPermissions the required permissions for the user
 * @param botPermissions the required permissions for the bot
 * @param visible whether the command is visible in the help command
 */
class CommandGroup(
    name: String,
    val commands: List<Command>,
    category: String,
    vararg aliases: String,
    description: String = "",
    val defaultCommand: Command? = null,
    guildOnly: Boolean = true,
    ownerOnly: Boolean = false,
    userPermissions: Long = 0,
    botPermissions: Long = Permission.getRaw(Permission.MESSAGE_WRITE),
    visible: Boolean = true
) : Command(
    name,
    category,
    *aliases,
    description,
    guildOnly = guildOnly,
    ownerOnly = ownerOnly,
    userPermissions = userPermissions,
    botPermissions = botPermissions,
    visible = visible
) {
    private val commandMap: MutableMap<String, Command> = mutableMapOf();

    init {
        for (cmd in commands) {
            commandMap[cmd.name] = cmd
            for (alias in cmd.aliases) {
                commandMap[alias] = cmd
            }
        }
    }

    /**
     * Applies the context and executes a subcommand.
     *
     * It takes the first argument and tries to match it with a command name. If it fails to do so, it invokes the default command.
     *
     * @param ctx the context
     */
    override suspend fun execute(ctx: CommandContext) {
        if (ctx.args.isEmpty() || commandMap[ctx.args[0]] == null) {
            return defaultCommand?.execute(
                CommandContext(
                    ctx.message,
                    defaultCommand,
                    ctx.args,
                    ctx.prefix,
                    ctx.client
                )
            )
                ?: throw CommandException(SubCommandNotFoundError(ctx.message, ctx.command, ctx.args.getOrNull(0)))
        }
        val command = commandMap[ctx.args[0]]!!
        CommandHandler.verify(ctx.message, command, ctx.client) // Lets the command handler verify the integrity.

        command.execute(
            CommandContext(
                ctx.message,
                command,
                ctx.args.drop(1).toTypedArray(),
                ctx.prefix,
                ctx.client
            )
        )
    }
}