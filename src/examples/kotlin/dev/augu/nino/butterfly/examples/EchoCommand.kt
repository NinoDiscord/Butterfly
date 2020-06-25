package dev.augu.nino.butterfly.examples

import dev.augu.nino.butterfly.command.Command
import dev.augu.nino.butterfly.command.CommandContext

class EchoCommand : Command("echo", "generic") {
    override suspend fun execute(ctx: CommandContext) {
        ctx.reply(ctx.args.joinToString(" "))
    }
}