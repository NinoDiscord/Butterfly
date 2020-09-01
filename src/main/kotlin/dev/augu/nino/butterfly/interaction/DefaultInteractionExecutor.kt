package dev.augu.nino.butterfly.interaction

import club.minnced.jda.reactor.ReactiveEventManager
import club.minnced.jda.reactor.on
import kotlinx.coroutines.reactive.awaitFirst
import net.dv8tion.jda.api.events.GenericEvent

/**
 * The default implementation of [InteractionExecutor].
 *
 * It works by iteratively waiting for new events and using the events to proceed to different steps.
 * This supports [ExecutableInteractionStep]s and will execute them if present.
 *
 * @since 0.3
 */
object DefaultInteractionExecutor : InteractionExecutor {

    override suspend fun executeInteractionFlow(
        rootInteractionStep: InteractionStep,
        eventManager: ReactiveEventManager,
        rootEvent: GenericEvent?
    ) {
        var event: GenericEvent? = rootEvent
        var interactionStep = rootInteractionStep
        var sameInteraction = false

        while (interactionStep != EndInteractionStep) {
            if (!sameInteraction && interactionStep is ExecutableInteractionStep && event != null) {
                val maybeNext = interactionStep.execute(event)

                if (maybeNext != null) {
                    interactionStep = maybeNext
                    continue
                }
            }

            event = eventManager.on<GenericEvent>().awaitFirst()

            val nextStep = interactionStep.nextStep(event)

            if (nextStep != null) {
                interactionStep = nextStep
                sameInteraction = false
            } else {
                sameInteraction = true
            }
        }
    }

}