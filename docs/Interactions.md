# Package dev.augu.nino.butterfly.interaction

The interaction package, includes types related to execution, creation and manipulation of interactions.

Interactions are a new way of making bot-user interactions type-safe.
One way to think about them is as a command waiting for an event to proceed to a different command.

## Why?
* Interactions are not obvious and can lead to many bugs. With interactions you can easily debug and find where the bug is.
* Making it possible to test different areas of a command without even calling it.
* Making reaction menus and responses much more easier.

## How?
Let's take the example of a settings command.

[![Settings Command graph](https://i.augu.dev/13c0.png)]

You start at the root, and print out the options a user can take.
 
Then, the user chooses an action and is automatically forwarded to the next step, 
where he would do the same thing again and again until he reaches the EndInteractionStep.