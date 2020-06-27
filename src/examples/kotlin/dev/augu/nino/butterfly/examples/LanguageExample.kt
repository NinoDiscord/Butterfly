@file:JvmName("LanguageExample")

package dev.augu.nino.butterfly.examples

import dev.augu.nino.butterfly.ButterflyClient
import dev.augu.nino.butterfly.GuildSettings
import dev.augu.nino.butterfly.GuildSettingsLoader
import dev.augu.nino.butterfly.command.Command
import dev.augu.nino.butterfly.command.CommandContext
import dev.augu.nino.butterfly.i18n.I18nLanguage
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Guild


/**
 * Custom settings loader
 */
class CustomLanguageSettingsLoader(val defaultLanguage: I18nLanguage) : GuildSettingsLoader<GuildSettings> {
    val map = mutableMapOf<String, GuildSettings>()

    override suspend fun load(guild: Guild): GuildSettings =
        map.getOrPut(guild.id, { GuildSettings(null, defaultLanguage) })

}

/**
 * Example
 */
class ExampleCommand : Command("example", "generic", "דוגמא", "пример", guildOnly = true) {
    override suspend fun execute(ctx: CommandContext) {
        ctx.replyTranslate("example")
    }
}

/**
 * Greets someone
 */
class GreetCommand : Command("greet", "generic", "ברך", "приветствуйте", guildOnly = true) {
    override suspend fun execute(ctx: CommandContext) {
        if (ctx.args.isEmpty()) {
            ctx.replyTranslate("greetError")
            return
        }
        ctx.replyTranslate("greet", mapOf("name" to ctx.args[0]))
    }
}

/**
 * Changes the language
 */
class ChangeCommand : Command("change", "generic", "שנה", "изменение", guildOnly = true) {
    override suspend fun execute(ctx: CommandContext) {
        if (ctx.args.isEmpty()) {
            ctx.replyTranslate("changeNotProvided")
            return
        }
        val language = ctx.client.languages[ctx.args[0]]
        if (language == null) {
            ctx.replyTranslate("changeWrongLanguage")
        } else {
            ctx.settings<GuildSettings>()!!.language = language
            ctx.replyTranslate("changeSuccess")
        }
    }
}

object LanguageBot {
    fun launch() {
        val english = I18nLanguage(
            mapOf(
                "example" to "This is an example",
                "greet" to "Hello \${name}!",
                "greetError" to "You must provide a name!",
                "changeSuccess" to "Successfully changed the language!",
                "changeNotProvided" to "You must provide a language to change to!",
                "changeWrongLanguage" to "The language you have provided is not available!"
            )
        )
        val hebrew = I18nLanguage(
            mapOf(
                "example" to "זאת דוגמא",
                "greet" to "שלום \${name}!",
                "greetError" to "אתה חייב לציין שם!",
                "changeSuccess" to "השפה שונתה בהצלחה!",
                "changeNotProvided" to "אתה חייב לציין את שם השפה לשנות אליה!",
                "changeWrongLanguage" to "השפה שציינת אינה זמינה!"
            )
        )
        val russian = I18nLanguage(
            mapOf(
                "example" to "это пример",
                "greet" to "привет \${name}!",
                "greetError" to "Вы должны предоставить имя!",
                "changeSuccess" to "Успешно поменял язык!",
                "changeNotProvided" to "Вы должны предоставить язык для изменения!",
                "changeWrongLanguage" to "Язык, который вы указали, недоступен!"
            )
        )
        val jda = JDABuilder
            .createDefault(System.getenv("TOKEN"))
            .build()
        val client = ButterflyClient.builder(jda, "239790360728043520").let {
            it.useDefaultHelpCommand = false
            it.guildSettingsLoader = CustomLanguageSettingsLoader(english)
            it
        }.build()
        client.addPrefix("test!")
        client.addLanguage("english", english)
        client.addLanguage("עברית", hebrew)
        client.addLanguage("русский", russian)
        client.addCommand(ExampleCommand(), GreetCommand(), ChangeCommand())
    }
}

fun main() {
    LanguageBot.launch()
}