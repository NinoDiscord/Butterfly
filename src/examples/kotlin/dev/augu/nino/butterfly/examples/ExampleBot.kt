@file:JvmName("MainExample")

package dev.augu.nino.butterfly.examples

import club.minnced.jda.reactor.ReactiveEventManager
import dev.augu.nino.butterfly.ButterflyClient
import net.dv8tion.jda.api.JDABuilder

object ExampleBot {
    fun launch() {
        val jda = JDABuilder
            .createDefault(System.getenv("TOKEN"))
            .setEventManager(ReactiveEventManager())
            .build()
        val client = ButterflyClient.builder(jda, arrayOf("239790360728043520")).build()
        client.addPrefix("test!")
        client.addCommand(EchoCommand(), PingCommand())
    }
}

fun main() {
    ExampleBot.launch()
}