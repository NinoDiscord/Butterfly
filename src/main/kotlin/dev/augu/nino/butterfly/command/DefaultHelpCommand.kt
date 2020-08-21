package dev.augu.nino.butterfly.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

/**
 * The default help command, automatically enabled in the builder.
 *
 * ## Language Customization
 * This command supports i18n by design. In order to use this command with your language please add the following keys:
 * * helpCommandTitle: The title of the response embed when no arguments are given. Has argument botName which is the bot's Discord username.
 * * helpCommandDescription: The description of the response embed when no arguments are given. Has argument prefix which is the prefix the command has been invoked with.
 * * helpCommandNotFound: The response if the command is not found. No arguments.
 * * helpCommandDocTitle: The title of the response embed when a command argument is given. Has argument commandName which is the name of the command argument.
 * * helpCommandDocDescription: The description of the response embed when a command argument is given. Has argument commandDesc which is the description of the command argument.
 */
class DefaultHelpCommand : Command(
    "help", "generic", "docs", "hey", "hi",
    description = "The help command helps you learn how to use the bot."
) {

    private fun categoryValuesToDisplay(cmds: List<Command>): String {
        return cmds
            .map { it.name }
            .joinToString(" ") { "`$it`" }
    }

    override suspend fun execute(ctx: CommandContext) {
        val client = ctx.client
        val commands = client.commands

        if (ctx.args.isEmpty()) {
            val embed = EmbedBuilder().let { builder ->
                builder.setTitle(
                    ctx.language()?.translate("helpCommandTitle", mapOf("botName" to client.jda.selfUser.name))
                        ?: "Help - ${client.jda.selfUser.name}"
                )
                builder.setDescription(
                    ctx.language()?.translate("helpCommandDescription", mapOf("prefix" to ctx.prefix))
                        ?: "To get more information on a specific command, do ${ctx.prefix}help <Command>"
                )
                commands
                    .values
                    .filter { it.visible }
                    .groupBy { it.category }
                    .map {
                        MessageEmbed.Field(it.key, categoryValuesToDisplay(it.value), false)
                    }.forEach {
                        builder.addField(it)
                    }
                builder.build()
            }
            ctx.reply(embed)
        } else {
            val commandName = ctx.args[0]
            val command = commands[commandName] ?: client.aliases[commandName]
            if (command == null) {
                ctx.reply(ctx.language()?.translate("helpCommandNotFound") ?: "Command not found.")
            } else {
                val embed = EmbedBuilder().let { builder ->
                    builder.setTitle(
                        ctx.language()?.translate("helpCommandDocTitle", mapOf("commandName" to commandName))
                            ?: "Command $commandName"
                    )
                    builder.setDescription(
                        ctx.language()
                            ?.translate("helpCommandDocDescription", mapOf("commandDesc" to command.description))
                            ?: command.description
                    )
                    builder.build()
                }
                ctx.reply(embed)
            }

        }
    }

}