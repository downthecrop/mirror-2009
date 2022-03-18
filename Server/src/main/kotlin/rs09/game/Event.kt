package rs09.game

import api.events.*

object Event {
    val ResourceGathered = ResourceGatheredEvent::class.java
    val NPCKilled = NPCKillEvent::class.java
    val Teleport = TeleportEvent::class.java
    val FireLit = LitFireEvent::class.java
    val Interaction = InteractionEvent::class.java
}