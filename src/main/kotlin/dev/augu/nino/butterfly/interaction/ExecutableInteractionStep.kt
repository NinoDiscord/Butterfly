package dev.augu.nino.butterfly.interaction

import net.dv8tion.jda.api.events.GenericEvent

/**
 * An [InteractionStep] but has a function it needs to execute every time we reach this step.
 *
 * @since 0.3
 * @property callback the callback to execute every time we reach this step,
 * the receives the event that caused it to reach this step,
 * if it returns an [InteractionStep], the executor should continue with the same event using the new step.
 */
open class ExecutableInteractionStep(private val callback: suspend (GenericEvent) -> InteractionStep?) :
    InteractionStep() {
    /**
     * Executes the step's callback
     *
     * @param event the event to pass to the callback
     * @return an interaction step if it should continue using a new interaction step, otherwise null
     */
    suspend fun execute(event: GenericEvent): InteractionStep? = callback(event)
}