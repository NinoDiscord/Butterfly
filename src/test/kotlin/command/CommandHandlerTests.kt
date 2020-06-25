package command

import dev.augu.nino.butterfly.ButterflyClient
import dev.augu.nino.butterfly.command.Command
import dev.augu.nino.butterfly.command.CommandContext
import dev.augu.nino.butterfly.command.CommandException
import dev.augu.nino.butterfly.command.CommandHandler
import dev.augu.nino.butterfly.util.reply
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class CommandHandlerTests : DescribeSpec({
    val coroutinescope = CoroutineScope(newSingleThreadContext("Command Thread"))
    val jda = mockk<JDA>(relaxed = true)
    val client = ButterflyClient(jda, scope = coroutinescope)
    val handler = CommandHandler(client)
    val message = mockk<Message>(relaxed = true)
    val command = spyk<Command>(object : Command(
        "test", "generic", "teest",
        guildOnly = true,
        userPermissions = Permission.getRaw(Permission.MESSAGE_WRITE),
        botPermissions = Permission.getRaw(Permission.MESSAGE_WRITE)
    ) {

        override suspend fun execute(ctx: CommandContext) {
            ctx.reply("test")
        }
    })

    client.addPrefix("x!")
    client.addCommand(command)

    beforeTest {
        clearAllMocks()
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

        it("should not run a command if the user is a bot") {
            val guild = mockk<Guild>(relaxed = true)
            val guildChannel = mockk<TextChannel>(relaxed = true)
            val member = mockk<Member>(relaxed = true)

            every { message.author.isBot } returns true
            every { message.isFromGuild } returns true
            every { message.guild } returns guild
            every { message.member } returns member
            every { message.textChannel } returns guildChannel
            every { message.contentRaw } returns "x!test"
            every { member.permissions } returns Permission.getPermissions(0)
            every { member.hasPermission(any(), any<Collection<Permission>>()) } returns true

            shouldNotThrow<Exception> {
                runBlocking {
                    handler.invoke(message)
                }
            }

            coVerify(exactly = 0) {
                command.execute(any())
            }

            confirmVerified(command)
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
            every { member.hasPermission(any(), any<Collection<Permission>>()) } returns false

            message.member!!.hasPermission(guildChannel, Permission.getPermissions(Permission.MESSAGE_WRITE.rawValue))
                .shouldBe(false)

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
            every { message.textChannel } returns guildChannel
            every { member.permissions } returns Permission.getPermissions(Permission.MESSAGE_WRITE.rawValue)
            every { member.hasPermission(any(), any<Collection<Permission>>()) } returns true
            every { me.hasPermission(any(), any<Collection<Permission>>()) } returns false
            every { message.member } returns member
            every { me.permissions } returns Permission.getPermissions(0)
            every { guild.selfMember } returns me
            every { message.guild } returns guild
            every { message.contentRaw } returns "x!test"

            message.member!!.hasPermission(message.textChannel, Permission.getPermissions(1)).shouldBe(true)
            message.guild.selfMember.hasPermission(guildChannel, Permission.getPermissions(1)).shouldBe(false)
            shouldThrowMessage("Bot has insufficient permissions.") {
                runBlocking {
                    handler.invoke(message)
                }
            }
        }

        it("should run the command if all of the requirements are met") {
            val guild = mockk<Guild>(relaxed = true)
            val guildChannel = mockk<TextChannel>(relaxed = true)
            val member = mockk<Member>(relaxed = true)
            val me = mockk<Member>(relaxed = true)
            val sampleMsg = mockk<Message>(relaxed = true)

            every { message.isFromGuild } returns true
            every { message.channel } returns guildChannel
            every { message.textChannel } returns guildChannel
            every { member.permissions } returns Permission.getPermissions(Permission.MESSAGE_WRITE.rawValue)
            every { member.hasPermission(any(), any<Collection<Permission>>()) } returns true
            every { me.hasPermission(any(), any<Collection<Permission>>()) } returns true
            every { me.hasPermission(any<GuildChannel>(), *anyVararg<Permission>()) } returns true
            every { message.member } returns member
            every { me.permissions } returns Permission.getPermissions(0)
            every { guild.selfMember } returns me
            every { message.guild } returns guild
            every { message.contentRaw } returns "x!test"

            mockkStatic("dev.augu.nino.butterfly.util.MessageExtensions") // in order to mock the reply extension function.
            coEvery { message.reply(any<CharSequence>()) } returns sampleMsg

            shouldNotThrow<CommandException> {
                runBlocking {
                    handler.invoke(message)
                }
            }

            coVerify(exactly = 1) { command.execute(any()) }

            coVerify(exactly = 1) { message.reply("test") }
        }

        it("should run the command and send arguments") {
            val guild = mockk<Guild>(relaxed = true)
            val guildChannel = mockk<TextChannel>(relaxed = true)
            val member = mockk<Member>(relaxed = true)
            val me = mockk<Member>(relaxed = true)
            val sampleMsg = mockk<Message>(relaxed = true)

            every { message.isFromGuild } returns true
            every { message.channel } returns guildChannel
            every { message.textChannel } returns guildChannel
            every { member.permissions } returns Permission.getPermissions(Permission.MESSAGE_WRITE.rawValue)
            every { member.hasPermission(any(), any<Collection<Permission>>()) } returns true
            every { me.hasPermission(any(), any<Collection<Permission>>()) } returns true
            every { me.hasPermission(any<GuildChannel>(), *anyVararg<Permission>()) } returns true
            every { message.member } returns member
            every { me.permissions } returns Permission.getPermissions(0)
            every { guild.selfMember } returns me
            every { message.guild } returns guild
            every { message.contentRaw } returns "x!test a b c d"

            mockkStatic("dev.augu.nino.butterfly.util.MessageExtensions") // in order to mock the reply extension function.
            coEvery { message.reply(any<CharSequence>()) } returns sampleMsg

            shouldNotThrow<CommandException> {
                runBlocking {
                    handler.invoke(message)
                }
            }

            coVerify(exactly = 1) { command.execute(match { it.args.contentEquals(arrayOf("a", "b", "c", "d")) }) }

            coVerify(exactly = 1) { message.reply("test") }
        }
    }


})