package dev.augu.nino.butterfly.examples

import club.minnced.jda.reactor.ReactiveEventManager
import dev.augu.nino.butterfly.ButterflyClient
import dev.augu.nino.butterfly.GuildSettings
import dev.augu.nino.butterfly.GuildSettingsLoader
import dev.augu.nino.butterfly.command.Command
import dev.augu.nino.butterfly.command.CommandContext
import dev.augu.nino.butterfly.interaction.DefaultInteractionExecutor
import dev.augu.nino.butterfly.interaction.ExecutableInteractionStep
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import java.util.concurrent.ConcurrentHashMap

val settings: ConcurrentHashMap<String, GuildSettings> = ConcurrentHashMap()
const val ONE_EMOJI = "1️⃣"
const val TWO_EMOJI = "2️⃣"
const val THREE_EMOJI = "3️⃣"

internal class SettingsCommand :
    Command("settings", "generic", guildOnly = true, userPermissions = Permission.MANAGE_SERVER.rawValue) {


    override suspend fun execute(ctx: CommandContext) {
        ctx.reply(
            "Hello ${ctx.author.asTag}!\n" +
                    "Welcome to the settings command!\n"
        )
        var message: Message? = null

        // ########################## Define the steps ##########################

        val startingStep = ExecutableInteractionStep {
            message = ctx.reply(
                "What would you like to do?\n\n" +
                        "To set a prefix, type \"prefix\" or react with the :one: emoji.\n" +
                        "To see the current settings, type \"settings\" or react with the :two: emoji.\n" +
                        "To exit this menu, type \"exit\" or react with the :three: emoji."
            )
            message!!.addReaction(ONE_EMOJI).queue()
            message!!.addReaction(TWO_EMOJI).queue()
            message!!.addReaction(THREE_EMOJI).queue()
            null
        }
        val initPrefixStep = ExecutableInteractionStep {
            ctx.reply("Type the new prefix to set. Can be up to 10 characters.")
            null
        }
        val setsPrefixStep = ExecutableInteractionStep {
            val prefixMessage = (it as GuildMessageReceivedEvent).message

            // Here you can set the prefix according to your set up.
            val newPrefix = prefixMessage.contentRaw
            settings[ctx.guild!!.id] = GuildSettings(newPrefix, null)
            ctx.reply("Successfully set new prefix to ${newPrefix}!")
            startingStep // go back to the settings info
        }

        val viewSettingsStep = ExecutableInteractionStep {
            ctx.reply("Current Prefix: ${ctx.settings<GuildSettings>()?.prefix ?: "test!"}")
            startingStep
        }

        // ########################## Define the interactions ##########################

        startingStep.addStep(initPrefixStep, { event ->
            if (event is GuildMessageReceivedEvent) {
                return@addStep event.channel.id == ctx.channel.id && event.message.contentRaw == "prefix" && event.message.author.id == ctx.author.id
            }
            if (event is GuildMessageReactionAddEvent) {
                return@addStep event.messageId == message?.id && event.reactionEmote.isEmoji && event.reactionEmote.emoji == ONE_EMOJI && event.user.id == ctx.author.id
            }
            false
        })

        startingStep.addStep(viewSettingsStep, { event ->
            if (event is GuildMessageReceivedEvent) {
                return@addStep event.channel.id == ctx.channel.id && event.message.contentRaw == "settings" && event.message.author.id == ctx.author.id
            }
            if (event is GuildMessageReactionAddEvent) {
                return@addStep event.messageId == message?.id && event.reactionEmote.isEmoji && event.reactionEmote.emoji == TWO_EMOJI && event.user.id == ctx.author.id
            }
            false
        })

        startingStep.addEndStep({ event ->
            if (event is GuildMessageReceivedEvent) {
                return@addEndStep event.channel.id == ctx.channel.id && event.message.contentRaw == "exit" && event.message.author.id == ctx.author.id
            }
            if (event is GuildMessageReactionAddEvent) {
                return@addEndStep event.messageId == message?.id && event.reactionEmote.isEmoji && event.reactionEmote.emoji == THREE_EMOJI && event.user.id == ctx.author.id
            }
            false
        })

        initPrefixStep.addStep(setsPrefixStep, { event ->
            if (event is GuildMessageReceivedEvent) {
                return@addStep event.channel.id == ctx.channel.id && event.author.id == ctx.author.id && event.message.contentRaw.length <= 10
            }
            false
        })

        DefaultInteractionExecutor.executeInteractionFlow(
            startingStep,
            ctx.client.jda.eventManager as ReactiveEventManager,
            ctx.event
        )
    }
}

private object InteractionExampleBot {
    fun launch() {
        val jda = JDABuilder
            .createDefault(System.getenv("TOKEN"))
            .setEventManager(ReactiveEventManager())
            .build()
        val client = ButterflyClient
            .builder(
                jda,
                arrayOf("239790360728043520"),
                guildSettingsLoader = object : GuildSettingsLoader<GuildSettings> {
                    override suspend fun load(guild: Guild): GuildSettings? = settings[guild.id]

                })
            .addPrefixes("test!")
            .addCommands(SettingsCommand())
            .build()
    }
}

private fun main() {
    InteractionExampleBot.launch()
}