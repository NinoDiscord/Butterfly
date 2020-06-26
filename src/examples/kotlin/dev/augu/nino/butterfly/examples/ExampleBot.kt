@file:JvmName("MainExample")

package dev.augu.nino.butterfly.examples

import dev.augu.nino.butterfly.ButterflyClient
import net.dv8tion.jda.api.JDABuilder

object ExampleBot {
    fun launch() {
        val jda = JDABuilder
            .createDefault(System.getenv("TOKEN"))
            .build()
        val client = ButterflyClient.builder(jda, "239790360728043520").build()
        client.addPrefix("test!")
        client.addCommand(EchoCommand())
        client.addCommand(PingCommand())
    }
}

fun main() {
    ExampleBot.launch()
}