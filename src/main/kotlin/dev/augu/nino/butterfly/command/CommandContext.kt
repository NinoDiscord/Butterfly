package dev.augu.nino.butterfly.command

import club.minnced.jda.reactor.asMono
import dev.augu.nino.butterfly.ButterflyClient
import dev.augu.nino.butterfly.GuildSettings
import dev.augu.nino.butterfly.i18n.I18nLanguage
import dev.augu.nino.butterfly.util.*
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.GenericMessageEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

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
 * @property event The event that called this message
 */
class CommandContext(
    val message: Message,
    val command: Command,
    val args: Array<String>,
    val prefix: String,
    val client: ButterflyClient,
    val event: GenericMessageEvent
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
    val guild: Guild? = if (message.isFromGuild) message.guild else null

    /**
     * The channel the message was sent in
     */
    val channel: MessageChannel = message.channel

    /**
     * This bot as a [SelfUser]
     */
    val me: SelfUser = client.jda.selfUser

    /**
     * This bot as a [Member]
     */
    val meMember: Member? = message.guild.selfMember

    /**
     * Fetches the guild settings
     *
     * @param T the settings type
     * @return the guild settings if in the guild
     */
    suspend inline fun <reified T : GuildSettings> settings(): T? {
        if (guild == null) {
            return null
        }
        val s = client.guildSettingsLoader.load(guild)
        if (s !is T) {
            return null
        }
        return s
    }

    /**
     * Returns the [I18nLanguage] for this context.
     *
     * If in the guild and a language is set, it defaults to that, otherwise it uses the client's default language.
     *
     * @return the [I18nLanguage] if set
     */
    suspend fun language(): I18nLanguage? {
        return settings<GuildSettings>()?.language ?: client.defaultLanguage
    }

    /**
     * Sends a message in the same channel as the context's
     *
     * @param msg The message to send
     * @return a [Mono] returning the message sent if done successfully
     */
    fun replyMono(msg: CharSequence): Mono<Message> {
        return message.replyMono(msg)
    }

    /**
     * Sends a message in the same channel as the context's
     *
     * @param msg The message to send
     * @return a [Mono] returning the message sent if done successfully
     */
    fun replyMono(msg: Message): Mono<Message> {
        return message.replyMono(msg)
    }

    /**
     * Sends a message in the same channel as the context's
     *
     * @param msg The message to send
     * @return a [Mono] returning the message sent if done successfully
     */
    fun replyMono(msg: MessageEmbed): Mono<Message> {
        return message.replyMono(msg)
    }

    /**
     * Sends a translated message in the same channel as the context's
     *
     * @param key the key of the translation
     * @param args the arguments for the translation
     * @param language the language to use
     * By default, it falls back to the guild language and if null, falls back to the client default language, if null it errors.
     * @return a [Mono] returning the message sent if done successfully
     */
    fun replyTranslateMono(
        key: String,
        args: Map<String, String> = mapOf(),
        language: I18nLanguage? = null
    ): Mono<Message> {
        return mono { replyTranslate(key, args, language) }
    }

    /**
     * Sends a message in the same channel as the context's
     *
     * @param msg The message to send
     * @return a [Message] instance of the message sent
     */
    suspend fun reply(msg: CharSequence): Message {
        return message.reply(msg)
    }

    /**
     * Sends a message in the same channel as the context's
     *
     * @param msg The message to send
     * @return a [Message] instance of the message sent
     */
    suspend fun reply(msg: Message): Message {
        return message.reply(msg)
    }

    /**
     * Sends a message in the same channel as the context's
     *
     * @param msg The message to send
     * @return a [Message] instance of the message sent
     */
    suspend fun reply(msg: MessageEmbed): Message {
        if (guild != null && !meMember!!.hasPermission(channel as GuildChannel, Permission.MESSAGE_EMBED_LINKS)) {
            throw CommandException(MissingEmbedPermissionsError(message, command))
        }

        return message.reply(msg)
    }

    /**
     * Sends a translated message in the same channel as the context's
     *
     * @param key the key of the translation
     * @param args the arguments for the translation
     * @param language the language to use
     * By default, it falls back to the guild language and if null, falls back to the client default language, if null it errors.
     * @return a [Message] instance of the message sent
     */
    suspend fun replyTranslate(
        key: String,
        args: Map<String, String> = mapOf(),
        language: I18nLanguage? = null
    ): Message {
        val lang = language ?: this.language() ?: throw IllegalStateException("No language found.")
        return reply(lang.translate(key, args))
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

    /**
     * Waits for one message
     *
     * @param predicate the predicate that chooses which message to receive.
     * @return a mono of a message
     */
    fun waitForMessageMono(predicate: (Message) -> Boolean = { it.channel == channel && it.author == author }): Mono<Message> {
        return channel.waitForMessageMono(predicate)
    }

    /**
     * Waits for one message
     *
     * @param predicate the predicate that chooses which message to receive.
     * @return a message
     */
    suspend fun waitForMessage(predicate: (Message) -> Boolean = { it.channel == channel && it.author == author }): Message {
        return channel.waitForMessage(predicate)
    }


    /**
     * Waits for one message
     *
     * @param predicate the predicate that chooses which message to receive.
     * @param timeOut specifies the timeout duration
     * @return a message
     */
    suspend fun waitForMessage(
        timeOut: Duration,
        predicate: (Message) -> Boolean = { it.channel == channel && it.author == author }
    ): Message {
        return channel.waitForMessage(timeOut, predicate)
    }


    /**
     * Waits for messages
     *
     * @param predicate the predicate that chooses which messages to receive.
     * @return a flux of messages
     */
    fun waitForMessages(predicate: (Message) -> Boolean = { it.channel == channel && it.author == author }): Flux<Message> {
        return channel.waitForMessages(predicate)
    }
}