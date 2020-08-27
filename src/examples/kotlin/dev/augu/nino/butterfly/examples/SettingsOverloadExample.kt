@file:JvmName("SettingsOverloadExample")

package dev.augu.nino.butterfly.examples

import club.minnced.jda.reactor.ReactiveEventManager
import dev.augu.nino.butterfly.ButterflyClient
import dev.augu.nino.butterfly.GuildSettings
import dev.augu.nino.butterfly.GuildSettingsLoader
import dev.augu.nino.butterfly.command.Command
import dev.augu.nino.butterfly.command.CommandContext
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Guild

/**
 * Custom settings
 *
 * @property prefix the guild prefix
 * @property counter counts the number of command calls for each guild.
 */
private class CustomSettings(override var prefix: String?, var counter: Int) : GuildSettings(prefix, null) {
    suspend fun save() {
        // Save to a database
    }
}

/**
 * Custom settings loader
 */
private object CustomSettingsLoader : GuildSettingsLoader<CustomSettings> {
    val map = mutableMapOf<String, CustomSettings>()

    override suspend fun load(guild: Guild): CustomSettings =
        map.getOrPut(guild.id, { CustomSettings(prefix = null, counter = 0) })

}

private class AddCommand : Command("add", "simple", "++", guildOnly = true) {
    override suspend fun execute(ctx: CommandContext) {
        ctx.settings<CustomSettings>()!!.counter++
    }
}

private class PrintCommand : Command("print", "simple", "printcount", "value", guildOnly = true) {
    override suspend fun execute(ctx: CommandContext) {
        ctx.reply("The value is: ${ctx.settings<CustomSettings>()!!.counter}")
    }
}

private class ClearCommand : Command("clear", "simple", guildOnly = true) {
    override suspend fun execute(ctx: CommandContext) {
        ctx.settings<CustomSettings>()!!.counter = 0
    }
}

private object SettingsOverload {
    fun launch() {
        val jda = JDABuilder
            .createDefault(System.getenv("TOKEN"))
            .setEventManager(ReactiveEventManager())
            .build()
        val client =
            ButterflyClient.builder(jda, arrayOf("239790360728043520"), guildSettingsLoader = CustomSettingsLoader)
                .addCommands(AddCommand(), PrintCommand(), ClearCommand())
                .addPrefixes("test!")
                .build()
    }
}

private fun main() {
    SettingsOverload.launch()
}