package dev.augu.nino.butterfly.interaction

import net.dv8tion.jda.api.events.GenericEvent

/**
 * This class describes an action that an [InteractionStep] should take
 * Upon reaching an [InteractionStep], the interaction looks up the next step it should take,
 * it filters through the possible [Action]s using [Action.shouldExecute] and sorts by the [Action.priority].

 * @since 0.3
 */
interface Action {
    /**
     * Returns whether it can proceed to the next step.
     * @param event the event
     * @return a boolean of whether it can proceed to the next step
     */
    suspend fun shouldExecute(event: GenericEvent): Boolean

    /**
     * Returns the priority of the action, priority higher than other priorities will result on it being chosen.
     * @return an int representing the priority
     */
    fun priority(): Int

    /**
     * Returns the step it should take if it deems this action the best action to take.
     * @return the [InteractionStep] the action leads to.
     */
    fun proceedToStep(): InteractionStep
}