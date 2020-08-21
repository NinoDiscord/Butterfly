package i18n

import dev.augu.nino.butterfly.command.Command
import dev.augu.nino.butterfly.command.CommandContext
import dev.augu.nino.butterfly.command.DefaultHelpCommand
import dev.augu.nino.butterfly.i18n.I18nLanguage
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.*
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.MessageEmbed

class CommandIntegrationTests : DescribeSpec({
    describe("Integration Test - Help Command - I18n") {
        val command = spyk(DefaultHelpCommand())
        val hebrew = spyk(
            I18nLanguage(
                "hebrew",
                mapOf(
                    "helpCommandTitle" to "עזרה - \${botName}",
                    "helpCommandDescription" to "בכדי לקבל עזרה בפקודה מסוימת, תעשה \${prefix}help <פקודה>",
                    "helpCommandNotFound" to "פקודה לא נמצאה.",
                    "helpCommandDocTitle" to "פקודה - \${commandName}",
                    "helpCommandDocDescription" to "\${commandDesc}"
                )
            )
        )

        beforeTest {
            clearAllMocks()
        }

        it("No arguments should list the visible commands") {
            val visibleTestCommand = object : Command("visible", "simple") {
                override suspend fun execute(ctx: CommandContext) {}
            }
            val invisibleTestCommand = object : Command("invisible", "simple", visible = false) {
                override suspend fun execute(ctx: CommandContext) {}
            }
            val commands = mutableMapOf("invisible" to invisibleTestCommand, "visible" to visibleTestCommand)
            val ctx = mockk<CommandContext>(relaxed = true)
            every { ctx.client.commands } returns commands
            every { ctx.args } returns arrayOf()
            every { ctx.prefix } returns "!"
            every { ctx.client.jda.selfUser.name } returns "Test"
            coEvery { ctx.language() } returns hebrew

            shouldNotThrow<Exception> {
                runBlocking {
                    command.execute(ctx)
                }
            }

            coVerify(exactly = 1) {
                ctx.reply(match<MessageEmbed> {
                    it.title == "עזרה - Test" &&
                            it.description == "בכדי לקבל עזרה בפקודה מסוימת, תעשה !help <פקודה>"
                    it.fields[0].name == "simple" &&
                            it.fields[0].value == "`visible`"
                })
            }

        }
        it("Command argument should return the command help.") {
            val visibleTestCommand = object : Command("visible", "simple", description = "A visible command.") {
                override suspend fun execute(ctx: CommandContext) {}
            }
            val invisibleTestCommand = object : Command("invisible", "simple", visible = false) {
                override suspend fun execute(ctx: CommandContext) {}
            }
            val commands = mutableMapOf("invisible" to invisibleTestCommand, "visible" to visibleTestCommand)
            val ctx = mockk<CommandContext>(relaxed = true)
            every { ctx.client.commands } returns commands
            every { ctx.args } returns arrayOf("visible")
            every { ctx.client.jda.selfUser.name } returns "Test"
            coEvery { ctx.language() } returns hebrew

            shouldNotThrow<Exception> {
                runBlocking {
                    command.execute(ctx)
                }
            }

            coVerify(exactly = 1) {
                ctx.reply(match<MessageEmbed> {
                    it.title == "פקודה - visible" &&
                            it.description == "A visible command."
                })
            }

        }

        it("Invalid command argument should return command not found.") {
            val visibleTestCommand = object : Command("visible", "simple", description = "A visible command.") {
                override suspend fun execute(ctx: CommandContext) {}
            }
            val invisibleTestCommand = object : Command("invisible", "simple", visible = false) {
                override suspend fun execute(ctx: CommandContext) {}
            }
            val commands = mutableMapOf("invisible" to invisibleTestCommand, "visible" to visibleTestCommand)
            val ctx = mockk<CommandContext>(relaxed = true)
            every { ctx.client.commands } returns commands
            every { ctx.client.aliases } returns mutableMapOf()
            every { ctx.args } returns arrayOf("errorous")
            every { ctx.client.jda.selfUser.name } returns "Test"
            coEvery { ctx.language() } returns hebrew

            shouldNotThrow<Exception> {
                runBlocking {
                    command.execute(ctx)
                }
            }

            coVerify(exactly = 1) {
                ctx.reply("פקודה לא נמצאה.")
            }

        }
    }
})