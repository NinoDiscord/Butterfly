package dev.augu.nino.butterfly.command

import club.minnced.jda.reactor.asMono
import dev.augu.nino.butterfly.ButterflyClient
import kotlinx.coroutines.reactive.awaitSingle
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import reactor.core.publisher.Mono

/**
 * The context passed on [Command] invocation.
 *
 * The context has some utility methods that will shorten your code by a large margin.
 *
 * @property message The message that invoked
 * @property command The command that got invoked
 * @property args The arguments passed
 * @property prefix The command prefix
 * @property client The [ButterflyClient] instance
 */
class CommandContext(
    val message: Message,
    val command: Command,
    val args: Array<String>,
    val prefix: String,
    val client: ButterflyClient
) {

    /**
     * The author of the message
     */
    val author: User = message.author

    /**
     * The author of the message as a [Member], will be null if the message was not sent in a guild
     */
    val member: Member? = message.member

    /**
     * The guild the message was sent in, will be null if the message was not sent in a guild
     */
    val guild: Guild? = if (message.channelType == ChannelType.TEXT) message.guild else null

    /**
     * The channel the message was sent in
     */
    val channel: MessageChannel = message.channel

    /**
     * This bot as a [SelfUser]
     */
    val me: SelfUser = client.selfUser

    /**
     * This bot as a [Member]
     */
    val meMember: Member? = message.guild.selfMember

    /**
     * Sends a message in the same channel as the context's
     *
     * @param msg The message to send
     * @return a [Mono] returning the message sent if done successfully
     */
    fun replyMono(msg: CharSequence): Mono<Message> {
        if (guild == null ||
            (meMember != null && meMember.hasPermission(channel as GuildChannel, Permission.MESSAGE_WRITE))
        ) {
            return channel.sendMessage(msg).asMono()
        }
        return Mono.empty()
    }

    /**
     * Sends a message in the same channel as the context's
     *
     * @param msg The message to send
     * @return a [Mono] returning the message sent if done successfully
     */
    fun replyMono(msg: Message): Mono<Message> {
        if (guild == null ||
            (meMember != null && meMember.hasPermission(channel as GuildChannel, Permission.MESSAGE_WRITE))
        ) {
            return channel.sendMessage(msg).asMono()
        }
        return Mono.empty()
    }

    /**
     * Sends a message in the same channel as the context's
     *
     * @param msg The message to send
     * @return a [Mono] returning the message sent if done successfully
     */
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

    /**
     * Sends a message in the same channel as the context's
     *
     * @param msg The message to send
     * @return a [Message] instance of the message sent
     */
    suspend fun reply(msg: CharSequence): Message {
        return replyMono(msg).awaitSingle()
    }

    /**
     * Sends a message in the same channel as the context's
     *
     * @param msg The message to send
     * @return a [Message] instance of the message sent
     */
    suspend fun reply(msg: Message): Message {
        return replyMono(msg).awaitSingle()
    }

    /**
     * Sends a message in the same channel as the context's
     *
     * @param msg The message to send
     * @return a [Message] instance of the message sent
     */
    suspend fun reply(msg: MessageEmbed): Message {
        return replyMono(msg).awaitSingle()
    }

    /**
     * Deletes the invocation message
     *
     * @param reason The reason for the audit-log
     * @return a [Mono] returning nothing
     */
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

    /**
     * Deletes the invocation message
     *
     * @param reason The reason for the audit-log
     */
    suspend fun deleteMessage(reason: String?) {
        deleteMessageMono(reason).awaitSingle()
    }
}