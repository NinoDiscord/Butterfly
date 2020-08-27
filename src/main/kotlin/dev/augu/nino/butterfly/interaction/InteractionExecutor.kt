package dev.augu.nino.butterfly.interaction

import club.minnced.jda.reactor.ReactiveEventManager
import net.dv8tion.jda.api.events.GenericEvent

/**
 * An interface defining an executor of interactions.
 * Interactions should be executed like a decision tree but in a graph, meaning that there can be circles.
 *
 * More Information can be found in the [DefaultInteractionExecutor]'s documentation.
 */
interface InteractionExecutor {

    /**
     * Executes the interaction flow, starting from the given root, using events from the manager.
     *
     * @param rootInteractionStep the root of the interaction graph
     * @param eventManager the event manager to receive events from
     * @param rootEvent an optional event that made us reach the root interaction step
     */
    suspend fun executeInteractionFlow(
        rootInteractionStep: InteractionStep,
        eventManager: ReactiveEventManager,
        rootEvent: GenericEvent?
    )
}