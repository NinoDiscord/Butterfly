package dev.augu.nino.butterfly.interaction

/**
 * Defines the final interaction step, if the [DefaultInteractionExecutor] reaches this step, it stops the interaction flow.
 *
 * @since 0.3
 */
object EndInteractionStep : GraphableInteractionStep("end")