package dev.augu.nino.butterfly.interaction

/**
 * An interface defining a graph of [GraphableInteractionStep].
 *
 * @since 0.3
 */
interface InteractionGraph {
    /**
     * The current [GraphableInteractionStep].
     */
    fun currentStep(): GraphableInteractionStep

    /**
     * The next possible steps, might lead us to this same graph instance
     */
    fun possibleNextSteps(): List<InteractionGraph>
}