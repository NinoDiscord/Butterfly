@file:JvmName("SettingsOverloadExample")

package dev.augu.nino.butterfly.examples

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
class CustomSettings(override val prefix: String?, var counter: Int) : GuildSettings(prefix) {
    suspend fun save() {
        // Save to a database
    }
}

/**
 * Custom settings loader
 */
object CustomSettingsLoader : GuildSettingsLoader<CustomSettings> {
    val map = mutableMapOf<String, CustomSettings>()

    override suspend fun load(guild: Guild): CustomSettings =
        map.getOrPut(guild.id, { CustomSettings(prefix = null, counter = 0) })

}

class AddCommand : Command("add", "simple", "++", guildOnly = true) {
    override suspend fun execute(ctx: CommandContext) {
        ctx.settings<CustomSettings>()!!.counter++
    }
}

class PrintCommand : Command("print", "simple", "printcount", "value", guildOnly = true) {
    override suspend fun execute(ctx: CommandContext) {
        ctx.reply("The value is: ${ctx.settings<CustomSettings>()!!.counter}")
    }
}

class ClearCommand : Command("clear", "simple", guildOnly = true) {
    override suspend fun execute(ctx: CommandContext) {
        ctx.settings<CustomSettings>()!!.counter = 0
    }
}

object SettingsOverload {
    fun launch() {
        val jda = JDABuilder
            .createDefault(System.getenv("TOKEN"))
            .build()
        val client = ButterflyClient(jda, guildSettingsLoader = CustomSettingsLoader)
        client.addPrefix("test!")
        client.addCommand(AddCommand())
        client.addCommand(PrintCommand())
        client.addCommand(ClearCommand())
    }
}

fun main() {
    SettingsOverload.launch()
}