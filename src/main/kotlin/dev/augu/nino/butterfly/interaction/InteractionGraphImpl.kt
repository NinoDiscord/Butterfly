package dev.augu.nino.butterfly.interaction

internal class InteractionGraphImpl(private val step: GraphableInteractionStep) : InteractionGraph {
    internal val nextSteps: MutableList<InteractionGraph> = arrayListOf()

    override fun currentStep(): GraphableInteractionStep = step

    override fun possibleNextSteps(): List<InteractionGraph> = nextSteps
}