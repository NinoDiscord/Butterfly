package command

import dev.augu.nino.butterfly.command.Command
import dev.augu.nino.butterfly.command.CommandContext
import dev.augu.nino.butterfly.util.edit
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.test.runBlockingTest
import net.dv8tion.jda.api.entities.Message
import java.time.Duration
import java.time.OffsetDateTime
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

        it("Integration Test") {
            val ctx = mockk<CommandContext>(relaxed = true)
            val msg = mockk<Message>(relaxed = true)
            val time = OffsetDateTime.now()

            every { ctx.client.gatewayPing } returns 5
            every { ctx.message.timeCreated } returns time
            every { msg.timeCreated } returns time + Duration.of(5, ChronoUnit.SECONDS)
            coEvery { ctx.reply(any<CharSequence>()) } coAnswers {
                msg
            }
            mockkStatic("dev.augu.nino.butterfly.util.MessageExtensions")
            coEvery { msg.edit(any<CharSequence>()) } returns msg

            shouldNotThrow<Exception> {
                runBlockingTest {
                    cmd.execute(ctx)
                }
            }

            coVerify(exactly = 1) { ctx.reply("Calculating...") }
            coVerify(exactly = 1) { msg.edit("Ping: 5000ms | Websocket: 5ms") }

        }
    }
})