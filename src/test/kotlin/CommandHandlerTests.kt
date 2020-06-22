import dev.augu.nino.butterfly.command.ButterflyClient
import dev.augu.nino.butterfly.command.Command
import dev.augu.nino.butterfly.command.CommandContext
import dev.augu.nino.butterfly.command.CommandHandler
import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*

@ObsoleteCoroutinesApi
class CommandHandlerTests : DescribeSpec({
    val coroutinescope = CoroutineScope(newSingleThreadContext("Command Thread"))
    val jda = mockk<JDA>(relaxed = true)
    val client = ButterflyClient(jda, scope = coroutinescope)
    val handler = CommandHandler(client)
    val message = mockk<Message>(relaxed = true)

    client.addPrefix("x!")
    client.addCommand(object : Command(
        "test", "generic", "teest",
        guildOnly = true,
        userPermissions = Permission.getRaw(Permission.MESSAGE_WRITE),
        botPermissions = Permission.getRaw(Permission.MESSAGE_WRITE)
    ) {

        override suspend fun execute(ctx: CommandContext) {
            ctx.reply("test")
        }
    })

    beforeTest {
        clearMocks(message)
    }

    describe("Command Handler invoke tests") {
        it("should not run a guild-only command on a private channel") {
            val privChannel = mockk<PrivateChannel>(relaxed = true)


            every { message.channel } returns privChannel
            every { message.isFromGuild } returns false
            every { message.contentRaw } returns "x!test"

            shouldThrowMessage("Guild-only command invoked in non-guild environment.") {
                runBlocking {
                    handler.invoke(message)
                }
            }
        }

        it("should not run a command if the user doesn't have enough permissions") {
            val guild = mockk<Guild>(relaxed = true)
            val guildChannel = mockk<TextChannel>(relaxed = true)
            val member = mockk<Member>(relaxed = true)

            every { message.isFromGuild } returns true
            every { message.guild } returns guild
            every { message.member } returns member
            every { message.textChannel } returns guildChannel
            every { message.contentRaw } returns "x!test"
            every { member.permissions } returns Permission.getPermissions(0)
            every { member.hasPermission(guildChannel, *anyVararg()) } returns false

            message.member!!.hasPermission(guildChannel, Permission.MESSAGE_WRITE).shouldBe(false)

            shouldThrowMessage("User has insufficient permissions.") {
                runBlocking {
                    handler.invoke(message)
                }
            }
        }

        it("should not run a command if the bot doesn't have enough permissions") {
            val guild = mockk<Guild>(relaxed = true)
            val guildChannel = mockk<TextChannel>(relaxed = true)
            val member = mockk<Member>(relaxed = true)
            val me = mockk<Member>(relaxed = true)

            every { message.isFromGuild } returns true
            every { member.permissions } returns Permission.getPermissions(Permission.MESSAGE_WRITE.rawValue)
            every { member.hasPermission(guildChannel, *anyVararg()) } returns true
            every { me.hasPermission(guildChannel, *anyVararg()) } returns false
            every { message.member } returns member
            every { guild.selfMember } returns me
            every { me.permissions } returns Permission.getPermissions(0)
            every { message.guild } returns guild
            every { message.textChannel } returns guildChannel
            every { message.contentRaw } returns "x!test"

            message.member!!.hasPermission(guildChannel, Permission.MESSAGE_WRITE).shouldBe(true)
            message.guild.selfMember.hasPermission(guildChannel, Permission.MESSAGE_WRITE).shouldBe(false)
            shouldThrowMessage("Bot has insufficient permissions.") {
                runBlocking {
                    handler.invoke(message)
                }
            }
        }
    }


})