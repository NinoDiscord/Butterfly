package dev.augu.nino.butterfly.interaction

import net.dv8tion.jda.api.events.GenericEvent

/**
 * An [InteractionStep] but has a function it needs to execute every time we reach this step.
 *
 * @since 0.3
 * @property callback the callback to execute every time we reach this step, the receives the event that caused it to reach this step.
 */
open class ExecutableInteractionStep(private val callback: suspend (GenericEvent) -> Unit) : InteractionStep() {
    /**
     * Executes the step's callback
     *
     * @param event the event to pass to the callback
     */
    suspend fun execute(event: GenericEvent) = callback(event)
}