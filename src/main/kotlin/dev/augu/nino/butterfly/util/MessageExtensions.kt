@file:JvmName("MessageExtensions")

package dev.augu.nino.butterfly.util

import club.minnced.jda.reactor.asMono
import kotlinx.coroutines.reactive.awaitSingle
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import reactor.core.publisher.Mono


/**
 * Sends a message in the same channel as the context's
 *
 * @param msg The message to send
 * @return a [Mono] returning the message sent if done successfully
 */
fun Message.replyMono(msg: CharSequence): Mono<Message> {
    if (!isFromGuild ||
        (guild.selfMember.hasPermission(textChannel, Permission.MESSAGE_WRITE))
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
fun Message.replyMono(msg: Message): Mono<Message> {
    if (!isFromGuild ||
        (guild.selfMember.hasPermission(textChannel, Permission.MESSAGE_WRITE))
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
fun Message.replyMono(msg: MessageEmbed): Mono<Message> {
    if (!isFromGuild ||
        (guild.selfMember.hasPermission(
            textChannel,
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
suspend fun Message.reply(msg: CharSequence): Message {
    return replyMono(msg).awaitSingle()
}

/**
 * Sends a message in the same channel as the context's
 *
 * @param msg The message to send
 * @return a [Message] instance of the message sent
 */
suspend fun Message.reply(msg: Message): Message {
    return replyMono(msg).awaitSingle()
}

/**
 * Sends a message in the same channel as the context's
 *
 * @param msg The message to send
 * @return a [Message] instance of the message sent
 */
suspend fun Message.reply(msg: MessageEmbed): Message {
    return replyMono(msg).awaitSingle()
}

/**
 * Edits the message
 *
 * @param msg The new content
 * @return a mono of the [Message] returned
 */
fun Message.editMono(msg: CharSequence): Mono<Message> {
    return editMessage(msg).asMono()
}

/**
 * Edits the message
 *
 * @param msg The new content
 * @return a mono of the [Message] returned
 */
fun Message.editMono(msg: Message): Mono<Message> {
    return editMessage(msg).asMono()
}

/**
 * Edits the message
 *
 * @param msg The new content
 * @return a mono of the [Message] returned
 */
fun Message.editMono(msg: MessageEmbed): Mono<Message> {
    return editMessage(msg).asMono()
}

/**
 * Edits the message
 *
 * @param msg The new content
 * @return the [Message] returned
 */
suspend fun Message.edit(msg: CharSequence): Message {
    return editMono(msg).awaitSingle()
}


/**
 * Edits the message
 *
 * @param msg The new content
 * @return the [Message] returned
 */
suspend fun Message.edit(msg: Message): Message {
    return editMono(msg).awaitSingle()
}


/**
 * Edits the message
 *
 * @param msg The new content
 * @return the [Message] returned
 */
suspend fun Message.edit(msg: MessageEmbed): Message {
    return editMono(msg).awaitSingle()
}