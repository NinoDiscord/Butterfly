package interaction

import dev.augu.nino.butterfly.interaction.GraphableInteractionStep
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent

class InteractionGraphTests : DescribeSpec({
    describe("Interaction - Graph Test") {
        val step1 = GraphableInteractionStep("Initial")
        val step2 = GraphableInteractionStep("OnMessage")
        val step3 = GraphableInteractionStep("OnReaction")
        step1.addStep(step2, {
            it is MessageReceivedEvent
        }, 1)
        step1.addStep(step3, {
            it is MessageReactionAddEvent
        }, 1)
        step1.addEndStep({ true })
        step2.addStep(step1, { true })
        step3.addStep(step1, { true })
        val graph = step1.getGraph()

        it("should build graph") {
            graph.shouldNotBeNull()
        }

        it("step1 should have step2, step3 and end as its children") {
            val names = graph.possibleNextSteps().map { it.currentStep().name }.sorted()
            names[0].shouldBe("OnMessage")
            names[1].shouldBe("OnReaction")
            names[2].shouldBe("end")
            names.shouldHaveSize(3)
        }

        it("step2 should have step1 as its children") {
            val steps = step2.getGraph().possibleNextSteps()
            steps[0].currentStep().name.shouldBe("Initial")
            steps.shouldHaveSize(1)
        }

        it("step3 should have step1 as its children") {
            val steps = step3.getGraph().possibleNextSteps()
            steps[0].currentStep().name.shouldBe("Initial")
            steps.shouldHaveSize(1)
        }

        it("taking a step from step1 using a message event should forward to step2") {
            val messageEvent = mockk<MessageReceivedEvent>()
            val nextStep = step1.nextStep(messageEvent) as GraphableInteractionStep
            nextStep.name.shouldBe("OnMessage")
        }

        it("taking a step from step1 using a reaction event should forward to step3") {
            val reactionEvent = mockk<MessageReactionAddEvent>()
            val nextStep = step1.nextStep(reactionEvent) as GraphableInteractionStep
            nextStep.name.shouldBe("OnReaction")
        }

        it("taking a step from step1 using another event should forward to end") {
            val event = mockk<GenericEvent>()
            val nextStep = step1.nextStep(event) as GraphableInteractionStep
            nextStep.name.shouldBe("end")
        }

        it("taking a step from step2 using an event should forward to step1") {
            val event = mockk<GenericEvent>()
            val nextStep = step2.nextStep(event) as GraphableInteractionStep
            nextStep.name.shouldBe("Initial")
        }

        it("taking a step from step3 using an event should forward to step1") {
            val event = mockk<GenericEvent>()
            val nextStep = step3.nextStep(event) as GraphableInteractionStep
            nextStep.name.shouldBe("Initial")
        }
    }
})