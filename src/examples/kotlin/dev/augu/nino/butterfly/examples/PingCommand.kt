package dev.augu.nino.butterfly.examples

import dev.augu.nino.butterfly.command.Command
import dev.augu.nino.butterfly.command.CommandContext
import dev.augu.nino.butterfly.util.edit
import java.time.temporal.ChronoUnit

class PingCommand : Command("ping", "generic", "pong", guildOnly = true) {
    override suspend fun execute(ctx: CommandContext) {
        val msg = ctx.reply("Calculating...")
        val ping = ctx.message.timeCreated.until(msg.timeCreated, ChronoUnit.MILLIS)
        msg.edit("Ping: ${ping}ms | Websocket: ${ctx.client.gatewayPing}ms")
    }
}
