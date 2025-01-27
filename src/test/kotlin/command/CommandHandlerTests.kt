package command

import club.minnced.jda.reactor.ReactiveEventManager
import dev.augu.nino.butterfly.ButterflyClient
import dev.augu.nino.butterfly.GuildSettings
import dev.augu.nino.butterfly.GuildSettingsLoader
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.GenericMessageEvent

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class CommandHandlerTests : DescribeSpec({
    val jda = mockk<JDA>(relaxed = true)
    every { jda.eventManager } returns ReactiveEventManager()
    val client = spyk(ButterflyClient(jda, arrayOf("239790360728043520"), useDefaultHelpCommand = false))
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
            val event = mockk<GenericMessageEvent>()


            every { message.channel } returns privChannel
            every { message.isFromGuild } returns false
            every { message.contentRaw } returns "x!test"

            shouldThrowMessage("Guild-only command invoked in non-guild environment.") {
                runBlocking {
                    handler.invoke(message, event)
                }
            }
        }

        it("should not run a command if the user is a bot") {
            val guild = mockk<Guild>(relaxed = true)
            val guildChannel = mockk<TextChannel>(relaxed = true)
            val member = mockk<Member>(relaxed = true)
            val event = mockk<GenericMessageEvent>()

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
                    handler.invoke(message, event)
                }
            }

            coVerify(exactly = 0) {
                command.execute(any())
            }

            confirmVerified(command)
        }

        it("should not run an owner-only command if the user is not the owner") {
            val guild = mockk<Guild>(relaxed = true)
            val guildChannel = mockk<TextChannel>(relaxed = true)
            val member = mockk<Member>(relaxed = true)
            val event = mockk<GenericMessageEvent>()

            every { message.author.id } returns "0"
            every { message.author.isBot } returns false
            every { command.ownerOnly } returns true
            every { message.isFromGuild } returns true
            every { message.guild } returns guild
            every { message.member } returns member
            every { message.textChannel } returns guildChannel
            every { message.contentRaw } returns "x!test"
            every { member.permissions } returns Permission.getPermissions(0)
            every { member.hasPermission(any(), any<Collection<Permission>>()) } returns true

            shouldThrowMessage("Owner-only command invoked not by the owner.") {
                runBlocking {
                    handler.invoke(message, event)
                }
            }

            coVerify(exactly = 0) {
                command.execute(any())
            }
        }

        it("should run an owner-only command if the user is the owner") {
            val guild = mockk<Guild>(relaxed = true)
            val guildChannel = mockk<TextChannel>(relaxed = true)
            val member = mockk<Member>(relaxed = true)
            val sampleMsg = mockk<Message>(relaxed = true)
            val event = mockk<GenericMessageEvent>()

            every { message.author.id } returns "239790360728043520"
            every { message.author.isBot } returns false
            every { command.ownerOnly } returns true
            every { message.isFromGuild } returns true
            every { message.guild } returns guild
            every { message.member } returns member
            every { message.textChannel } returns guildChannel
            every { message.contentRaw } returns "x!test"
            every { member.permissions } returns Permission.getPermissions(0)
            every { member.hasPermission(any(), any<Collection<Permission>>()) } returns true
            every { guild.selfMember.hasPermission(any(), any<Collection<Permission>>()) } returns true

            mockkStatic("dev.augu.nino.butterfly.util.MessageExtensions") // in order to mock the reply extension function.
            coEvery { message.reply(any<CharSequence>()) } returns sampleMsg

            shouldNotThrow<Exception> {
                runBlocking {
                    handler.invoke(message, event)
                }
            }

            coVerify(exactly = 1) {
                command.execute(any())
            }
        }

        it("should not run a command if the user doesn't have enough permissions") {
            val guild = mockk<Guild>(relaxed = true)
            val guildChannel = mockk<TextChannel>(relaxed = true)
            val member = mockk<Member>(relaxed = true)
            val event = mockk<GenericMessageEvent>()

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
                    handler.invoke(message, event)
                }
            }
        }

        it("should not run a command if the bot doesn't have enough permissions") {
            val guild = mockk<Guild>(relaxed = true)
            val guildChannel = mockk<TextChannel>(relaxed = true)
            val member = mockk<Member>(relaxed = true)
            val me = mockk<Member>(relaxed = true)
            val event = mockk<GenericMessageEvent>()

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
                    handler.invoke(message, event)
                }
            }
        }

        it("should run the command with custom guild prefix") {
            val guild = mockk<Guild>(relaxed = true)
            val guildChannel = mockk<TextChannel>(relaxed = true)
            val member = mockk<Member>(relaxed = true)
            val me = mockk<Member>(relaxed = true)
            val sampleMsg = mockk<Message>(relaxed = true)
            val customSettingsLoader = object : GuildSettingsLoader<GuildSettings> {
                override suspend fun load(guild: Guild): GuildSettings = GuildSettings("z!", null)
            }
            val event = mockk<GenericMessageEvent>()

            every { client.guildSettingsLoader } returns customSettingsLoader
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
            every { message.contentRaw } returns "z!test"

            mockkStatic("dev.augu.nino.butterfly.util.MessageExtensions") // in order to mock the reply extension function.
            coEvery { message.reply(any<CharSequence>()) } returns sampleMsg

            shouldNotThrow<CommandException> {
                runBlocking {
                    handler.invoke(message, event)
                }
            }

            coVerify(exactly = 1) { command.execute(any()) }

            coVerify(exactly = 1) { message.reply("test") }
        }

        it("should run the command if all of the requirements are met") {
            val guild = mockk<Guild>(relaxed = true)
            val guildChannel = mockk<TextChannel>(relaxed = true)
            val member = mockk<Member>(relaxed = true)
            val me = mockk<Member>(relaxed = true)
            val sampleMsg = mockk<Message>(relaxed = true)
            val event = mockk<GenericMessageEvent>()

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
                    handler.invoke(message, event)
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
            val event = mockk<GenericMessageEvent>()

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
                    handler.invoke(message, event)
                }
            }

            coVerify(exactly = 1) { command.execute(match { it.args.contentEquals(arrayOf("a", "b", "c", "d")) }) }

            coVerify(exactly = 1) { message.reply("test") }
        }
    }


})