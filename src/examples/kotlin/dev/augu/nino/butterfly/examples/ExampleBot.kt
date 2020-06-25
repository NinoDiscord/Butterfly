@file:JvmName("Main")

package dev.augu.nino.butterfly.examples

import dev.augu.nino.butterfly.ButterflyClient
import net.dv8tion.jda.api.JDABuilder

object ExampleBot {
    fun launch() {
        val jda = JDABuilder
            .createDefault(System.getenv("TOKEN"))
            .build()
        val client = ButterflyClient(jda)
        client.addPrefix("test!")
        client.addCommand(EchoCommand())
        client.addCommand(PingCommand())
    }
}

fun main() {
    ExampleBot.launch()
}