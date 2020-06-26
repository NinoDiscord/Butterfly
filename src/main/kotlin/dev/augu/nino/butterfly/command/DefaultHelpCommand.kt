package dev.augu.nino.butterfly.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

/**
 * The default help command, automatically enabled in the builder.
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
                builder.setTitle("Help - ${client.selfUser.name}")
                builder.setDescription("To get more information on a specific command, do ${ctx.prefix}help <Command>")
                commands
                    .values
                    .filter { visible }
                    .groupBy { category }
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
                ctx.reply("Command not found.")
            } else {
                val embed = EmbedBuilder().let { builder ->
                    builder.setTitle("Command $commandName")
                    builder.setDescription(command.description)
                    builder.build()
                }
                ctx.reply(embed)
            }

        }
    }

}