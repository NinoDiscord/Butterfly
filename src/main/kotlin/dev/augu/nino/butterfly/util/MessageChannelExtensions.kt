@file:JvmName("MessageChannelExtensions")

package dev.augu.nino.butterfly.util

import club.minnced.jda.reactor.on
import kotlinx.coroutines.reactive.awaitSingle
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration


/**
 * Waits for one message
 *
 * @param predicate the predicate that chooses which message to receive.
 * @return a mono of a message
 */
fun MessageChannel.waitForMessageMono(predicate: (Message) -> Boolean = { it.channel == this }): Mono<Message> {
    return this.waitForMessages(predicate).single()
}

/**
 * Waits for one message
 *
 * @param predicate the predicate that chooses which message to receive.
 * @return a message
 */
suspend fun MessageChannel.waitForMessage(predicate: (Message) -> Boolean = { it.channel == this }): Message {
    return this.waitForMessageMono(predicate).awaitSingle()
}


/**
 * Waits for one message
 *
 * @param predicate the predicate that chooses which message to receive.
 * @param timeOut specifies the timeout duration
 * @return a message
 */
suspend fun MessageChannel.waitForMessage(
    timeOut: Duration,
    predicate: (Message) -> Boolean = { it.channel == this }
): Message {
    return waitForMessageMono(predicate).timeout(timeOut).awaitSingle()
}


/**
 * Waits for messages
 *
 * @param predicate the predicate that chooses which messages to receive.
 * @return a flux of messages
 */
fun MessageChannel.waitForMessages(predicate: (Message) -> Boolean = { it.channel == this }): Flux<Message> {
    return jda.on<MessageReceivedEvent>()
        .map { it.message }
}