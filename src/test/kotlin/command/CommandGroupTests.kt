package command

import dev.augu.nino.butterfly.ButterflyClient
import dev.augu.nino.butterfly.command.Command
import dev.augu.nino.butterfly.command.CommandContext
import dev.augu.nino.butterfly.command.CommandException
import dev.augu.nino.butterfly.command.CommandGroup
import dev.augu.nino.butterfly.util.reply
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.*
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*

class CommandGroupTests : DescribeSpec({
    describe("CommandGroup Tests") {
        it("should throw an error if there are no suitable commands") {
            val cmd = spyk(object : Command("example", "generic") {
                override suspend fun execute(ctx: CommandContext) {
                    ctx.reply("example")
                }
            })
            val group = CommandGroup("test", arrayOf(cmd).asList(), "generic")
            val ctx = mockk<CommandContext>()
            val message = mockk<Message>()

            every { ctx.args } returns arrayOf("notexample")
            every { ctx.message } returns message
            every { ctx.command } returns group
            coEvery { ctx.reply(any<CharSequence>()) } returns message

            shouldThrowMessage("Subcommand not found.") {
                runBlocking {
                    group.execute(ctx)
                }
            }

            coVerify(exactly = 0) {
                cmd.execute(any())
            }
        }

        it("should run a command in the list") {
            val cmd = spyk(object : Command("example", "generic") {
                override suspend fun execute(ctx: CommandContext) {
                    ctx.reply("example")
                }
            })
            val group = CommandGroup("test", arrayOf(cmd).asList(), "generic")
            val ctx = mockk<CommandContext>()
            val message = mockk<Message>()
            val client = mockk<ButterflyClient>()
            val author = mockk<User>()
            val meUser = mockk<SelfUser>()
            val member = mockk<Member>()
            val meMember = mockk<Member>()
            val channel = mockk<TextChannel>()
            val guild = mockk<Guild>()

            every { ctx.prefix } returns "!"
            every { ctx.args } returns arrayOf("example")
            every { message.isFromGuild } returns true
            every { ctx.message } returns message
            every { message.member } returns member
            every { message.author } returns author
            every { message.textChannel } returns channel
            every { message.channel } returns channel
            every { message.guild } returns guild
            every { guild.selfMember } returns meMember
            every { member.hasPermission(any<GuildChannel>(), any<Collection<Permission>>()) } returns true
            every { meMember.hasPermission(any<GuildChannel>(), any<Collection<Permission>>()) } returns true
            every { ctx.command } returns group
            every { client.selfUser } returns meUser
            every { ctx.client } returns client
            mockkStatic("dev.augu.nino.butterfly.util.MessageExtensions")
            coEvery { message.reply(any<CharSequence>()) } returns message

            shouldNotThrow<CommandException> {
                runBlocking {
                    group.execute(ctx)
                }
            }

            coVerify(exactly = 1) {
                cmd.execute(any())
            }
        }

        it("should run the default command if no command is found") {
            val cmd = spyk(object : Command("example", "generic") {
                override suspend fun execute(ctx: CommandContext) {
                    ctx.reply("example")
                }
            })
            val group = CommandGroup("test", arrayOf(cmd).asList(), "generic", defaultCommand = cmd)
            val ctx = mockk<CommandContext>()
            val message = mockk<Message>()
            val client = mockk<ButterflyClient>()
            val author = mockk<User>()
            val meUser = mockk<SelfUser>()
            val member = mockk<Member>()
            val meMember = mockk<Member>()
            val channel = mockk<TextChannel>()
            val guild = mockk<Guild>()

            every { ctx.prefix } returns "!"
            every { ctx.args } returns arrayOf("yee")
            every { message.isFromGuild } returns true
            every { ctx.message } returns message
            every { message.member } returns member
            every { message.author } returns author
            every { message.textChannel } returns channel
            every { message.channel } returns channel
            every { message.guild } returns guild
            every { guild.selfMember } returns meMember
            every { member.hasPermission(any<GuildChannel>(), any<Collection<Permission>>()) } returns true
            every { meMember.hasPermission(any<GuildChannel>(), any<Collection<Permission>>()) } returns true
            every { ctx.command } returns group
            every { client.selfUser } returns meUser
            every { ctx.client } returns client
            mockkStatic("dev.augu.nino.butterfly.util.MessageExtensions")
            coEvery { message.reply(any<CharSequence>()) } returns message

            shouldNotThrow<CommandException> {
                runBlocking {
                    group.execute(ctx)
                }
            }

            coVerify(exactly = 1) {
                cmd.execute(any())
            }
        }
    }
})