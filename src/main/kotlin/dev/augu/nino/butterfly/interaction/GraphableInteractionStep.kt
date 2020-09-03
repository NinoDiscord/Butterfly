package dev.augu.nino.butterfly.interaction

/**
 * An [InteractionStep] that supports creation of graphs, allowing easier debugging and testability.
 *
 * @param name the name used for creating the graphs, must be unique in a graph.
 * @since 0.3
 */
open class GraphableInteractionStep(val name: String) : InteractionStep() {

    /**
     * Builds and returns the interaction step graph, it detects circles automatically.
     *
     * @return the interaction graph
     */
    fun getGraph(): InteractionGraph {
        return getGraphRecursive(hashMapOf())
    }

    private fun getGraphRecursive(visited: MutableMap<GraphableInteractionStep, InteractionGraph>): InteractionGraph {
        if (visited[this] != null) {
            return visited.getValue(this)
        }
        val graph = InteractionGraphImpl(this)
        visited[this] = graph
        for (step in actions.map { it.proceedToStep() }) {
            if (step is GraphableInteractionStep) {
                graph.nextSteps.add(step.getGraphRecursive(visited))
            }
        }
        return graph
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GraphableInteractionStep

        if (name != other.name) return false

        return true
    }
}