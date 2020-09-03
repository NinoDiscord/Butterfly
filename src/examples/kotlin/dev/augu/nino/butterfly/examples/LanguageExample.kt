@file:JvmName("LanguageExample")

package dev.augu.nino.butterfly.examples

import club.minnced.jda.reactor.ReactiveEventManager
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
private class CustomLanguageSettingsLoader(val defaultLanguage: I18nLanguage) : GuildSettingsLoader<GuildSettings> {
    val map = mutableMapOf<String, GuildSettings>()

    override suspend fun load(guild: Guild): GuildSettings =
        map.getOrPut(guild.id, { GuildSettings(null, defaultLanguage) })

}

/**
 * Example
 */
private class ExampleCommand : Command("example", "generic", "דוגמא", "пример", guildOnly = true) {
    override suspend fun execute(ctx: CommandContext) {
        ctx.replyTranslate("example")
    }
}

/**
 * Greets someone
 */
private class GreetCommand : Command("greet", "generic", "ברך", "приветствуйте", guildOnly = true) {
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
private class ChangeCommand : Command("change", "generic", "שנה", "изменение", guildOnly = true) {
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

private object LanguageBot {
    fun launch() {
        val english = I18nLanguage(
            "english",
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
            "עברית",
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
            "русский",
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
            .setEventManager(ReactiveEventManager())
            .build()
        val client = ButterflyClient.builder(
            jda,
            arrayOf("239790360728043520"),
            useDefaultHelpCommand = false,
            guildSettingsLoader = CustomLanguageSettingsLoader(english)
        )
            .addCommands(ExampleCommand(), GreetCommand(), ChangeCommand())
            .addLanguages(english, hebrew, russian)
            .addPrefixes("test!")
            .build()
    }
}

private fun main() {
    LanguageBot.launch()
}