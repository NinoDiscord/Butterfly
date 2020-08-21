@file:JvmName("BasicShardedExample")

package dev.augu.nino.butterfly.examples

import club.minnced.jda.reactor.ReactiveEventManager
import dev.augu.nino.butterfly.ButterflyClientManager
import dev.augu.nino.butterfly.command.Command
import dev.augu.nino.butterfly.command.CommandContext
import dev.augu.nino.butterfly.util.edit
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import java.time.temporal.ChronoUnit

private class ShardedEchoCommand : Command("echo", "generic") {
    override suspend fun execute(ctx: CommandContext) {
        ctx.reply(ctx.args.joinToString(" "))
    }
}

private class ShardedPingCommand : Command("ping", "generic", "pong", guildOnly = true) {
    override suspend fun execute(ctx: CommandContext) {
        val msg = ctx.reply("Calculating...")
        val ping = ctx.message.timeCreated.until(msg.timeCreated, ChronoUnit.MILLIS)
        msg.edit("Ping: ${ping}ms | Websocket: ${ctx.client.jda.gatewayPing}ms")
    }
}

private object ShardedExampleBot {
    fun launch() {
        val defaultShardManager = DefaultShardManagerBuilder
            .createDefault(System.getenv("TOKEN"))
            .setEventManagerProvider {
                ReactiveEventManager()
            }
            .build()
        val client = ButterflyClientManager
            .builder(defaultShardManager, arrayOf("239790360728043520"))
            .addPrefixes("test!")
            .addCommands(ShardedEchoCommand(), ShardedPingCommand())
            .build()
    }
}

private fun main() {
    ShardedExampleBot.launch()
}