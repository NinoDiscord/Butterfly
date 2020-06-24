package dev.augu.nino.butterfly.command

import club.minnced.jda.reactor.asMono
import kotlinx.coroutines.reactive.awaitSingle
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import reactor.core.publisher.Mono

/**
 * The context passed on command invocation.
 */
class CommandContext(
    val message: Message,
    val command: Command,
    val args: Array<String>,
    val prefix: String
) {

    val author: User = message.author
    val member: Member? = message.member
    val guild: Guild? = if (message.channelType == ChannelType.TEXT) message.guild else null
    val channel: MessageChannel = message.channel
    val jda: JDA = message.jda
    val me: SelfUser = jda.selfUser
    val meMember: Member? = message.guild.selfMember

    /**
     * Replies to the message
     */
    fun replyMono(msg: CharSequence): Mono<Message> {
        if (guild == null ||
            (meMember != null && meMember.hasPermission(channel as GuildChannel, Permission.MESSAGE_WRITE))
        ) {
            return channel.sendMessage(msg).asMono()
        }
        return Mono.empty()
    }

    fun replyMono(msg: Message): Mono<Message> {
        if (guild == null ||
            (meMember != null && meMember.hasPermission(channel as GuildChannel, Permission.MESSAGE_WRITE))
        ) {
            return channel.sendMessage(msg).asMono()
        }
        return Mono.empty()
    }

    fun replyMono(msg: MessageEmbed): Mono<Message> {
        if (guild == null ||
            (meMember != null && meMember.hasPermission(
                channel as GuildChannel,
                Permission.MESSAGE_WRITE,
                Permission.MESSAGE_EMBED_LINKS
            ))
        ) {
            return channel.sendMessage(msg).asMono()
        }
        return Mono.empty()
    }

    suspend fun reply(msg: CharSequence): Message {
        val msg = replyMono(msg).awaitSingle()
        return msg
    }

    suspend fun reply(msg: Message): Message {
        return replyMono(msg).awaitSingle()
    }

    suspend fun reply(msg: MessageEmbed): Message {
        return replyMono(msg).awaitSingle()
    }

    fun deleteMessageMono(reason: String?): Mono<Void> {
        if (guild != null && (meMember != null && meMember.hasPermission(
                channel as GuildChannel,
                Permission.MESSAGE_MANAGE
            ))
        ) {
            return message.delete().reason(reason).asMono()
        }
        return Mono.empty()
    }

    suspend fun deleteMessage(reason: String?) {
        deleteMessageMono(reason).awaitSingle()
    }
}