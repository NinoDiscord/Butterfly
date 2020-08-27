package dev.augu.nino.butterfly.interaction

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.firstOrNull
import net.dv8tion.jda.api.events.GenericEvent

/**
 * Defines a step in an interaction tree.
 *
 * @since 0.3
 */
open class InteractionStep {

    /**
     * The available actions
     */
    val actions: MutableList<Action> = arrayListOf()

    /**
     * Adds a possible next step.
     *
     * @param step the step to proceed to if the filter returns true and it's of highest priority
     * @param filter the filter to filter-out specific events
     * @param priority the priority of this step, the higher the priority the higher the rank of this step is
     *
     * @return the action created
     */
    fun addStep(
        step: InteractionStep,
        filter: suspend (GenericEvent) -> Boolean,
        priority: Int = 0
    ): Action {
        val action = object : Action {
            override suspend fun shouldExecute(event: GenericEvent): Boolean = filter(event)
            override fun priority(): Int = priority
            override fun proceedToStep(): InteractionStep = step
        }
        actions.add(action)
        return action
    }

    /**
     * Similar to [addStep] but adds [EndInteractionStep].
     *
     * @param filter the filter to filter-out specific events
     * @param priority the priority of this step, the higher the priority the higher the rank of this step is
     *
     * @return the action created
     */
    fun addEndStep(filter: suspend (GenericEvent) -> Boolean, priority: Int = 0): Action =
        addStep(EndInteractionStep, filter, priority)

    /**
     * Proceeds to the next step asynchronously using a generic event and returns the event.
     *
     * In [DefaultInteractionExecutor] if the next step is null, it stays in this step; and if the next step is [EndInteractionStep] it finishes the flow.
     *
     * @param event the event to use to proceed to the next step.
     * @return the next step
     */
    suspend fun nextStep(event: GenericEvent): InteractionStep? {
        val action = actions
            .sortedByDescending { it.priority() }
            .asFlow()
//            .firstOrNull()
            .firstOrNull { it.shouldExecute(event) }
        return action?.proceedToStep()
    }
}