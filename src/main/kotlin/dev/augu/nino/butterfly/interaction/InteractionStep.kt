package dev.augu.nino.butterfly.interaction

import net.dv8tion.jda.api.events.GenericEvent

/**
 * Defines a step in an interaction tree.
 *
 * @since 0.3
 */
open class InteractionStep {
    val actions: MutableList<Action> = arrayListOf()

    fun addStep(filter: (GenericEvent) -> Boolean, step: GraphableInteractionStep, priority: Int = 0): Action {
        val action = object : Action {
            override fun shouldExecute(event: GenericEvent): Boolean = shouldExecute(event)
            override fun priority(): Int = priority
            override fun proceedToStep(): GraphableInteractionStep = step
        }
        actions.add(action)
        return action
    }

    fun nextStep(event: GenericEvent): InteractionStep? {
        val viableActions = actions.filter { it.shouldExecute(event) }.sortedByDescending { it.priority() }
        if (viableActions.isNotEmpty()) {
            return viableActions.first().proceedToStep()
        }
        return null
    }
}