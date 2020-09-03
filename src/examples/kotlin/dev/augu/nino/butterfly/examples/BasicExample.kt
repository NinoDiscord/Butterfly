@file:JvmName("BasicExample")

package dev.augu.nino.butterfly.examples

import club.minnced.jda.reactor.ReactiveEventManager
import dev.augu.nino.butterfly.ButterflyClient
import dev.augu.nino.butterfly.command.Command
import dev.augu.nino.butterfly.command.CommandContext
import dev.augu.nino.butterfly.util.edit
import net.dv8tion.jda.api.JDABuilder
import java.time.temporal.ChronoUnit

private class EchoCommand : Command("echo", "generic") {
    override suspend fun execute(ctx: CommandContext) {
        ctx.reply(ctx.args.joinToString(" "))
    }
}

private class PingCommand : Command("ping", "generic", "pong", guildOnly = true) {
    override suspend fun execute(ctx: CommandContext) {
        val msg = ctx.reply("Calculating...")
        val ping = ctx.message.timeCreated.until(msg.timeCreated, ChronoUnit.MILLIS)
        msg.edit("Ping: ${ping}ms | Websocket: ${ctx.client.jda.gatewayPing}ms")
    }
}

private object ExampleBot {
    fun launch() {
        val jda = JDABuilder
            .createDefault(System.getenv("TOKEN"))
            .setEventManager(ReactiveEventManager())
            .build()
        val client = ButterflyClient
            .builder(jda, arrayOf("239790360728043520"))
            .addPrefixes("test!")
            .addCommands(EchoCommand(), PingCommand())
            .build()
    }
}

private fun main() {
    ExampleBot.launch()
}