package command

import dev.augu.nino.butterfly.command.Command
import dev.augu.nino.butterfly.command.CommandContext
import dev.augu.nino.butterfly.util.edit
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.spyk
import java.time.temporal.ChronoUnit

class CommandIntegrationTests : DescribeSpec({
    describe("Integration Test - Ping Command") {
        // The Ping example from JDA-Utilities, redone in Butterfly!
        class PingCommand : Command("ping", "generic", "pong", guildOnly = true) {
            override suspend fun execute(ctx: CommandContext) {
                val msg = ctx.reply("Calculating...")
                val ping = ctx.message.timeCreated.until(msg.timeCreated, ChronoUnit.MILLIS)
                msg.edit("Ping: ${ping}ms | Websocket: ${ctx.client.gatewayPing}ms")
            }
        }

        val cmd = spyk<Command>(PingCommand())
        beforeTest {
            clearAllMocks()
        }

        it("Simple Tests") {
            cmd.name.shouldBe("ping")
            cmd.category.shouldBe("generic")
            cmd.aliases[0].shouldBe("pong")
            cmd.aliases.shouldHaveSize(1)
            cmd.guildOnly.shouldBeTrue()
        }
    }
})