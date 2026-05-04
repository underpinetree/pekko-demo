package ee.takahiro.pekkodemo.actor

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior
import org.apache.pekko.actor.typed.javadsl.ActorContext
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.actor.typed.javadsl.Receive
import org.apache.pekko.cluster.Cluster
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityTypeKey
import java.io.Serializable

class VoiceSessionActor private constructor(
    context: ActorContext<Command>,
    private val sessionId: String,
) : AbstractBehavior<VoiceSessionActor.Command>(context) {

    companion object {
        val TYPE_KEY: EntityTypeKey<Command> = EntityTypeKey.create(Command::class.java, "VoiceSession")

        fun create(sessionId: String): Behavior<Command> =
            Behaviors.setup { ctx -> VoiceSessionActor(ctx, sessionId) }
    }

    init {
        val address = Cluster.get(context.system.classicSystem()).selfMember().address()
        context.log.info("VoiceSessionActor started: sessionId={} on node={}", sessionId, address)
    }

    private val members = mutableSetOf<String>()

    override fun createReceive(): Receive<Command> =
        newReceiveBuilder()
            .onMessage(Join::class.java) { cmd ->
                context.log.info("VoiceSessionActor command=Join sessionId={} memberId={}", sessionId, cmd.memberId)
                members.add(cmd.memberId)
                this
            }
            .onMessage(Leave::class.java) { cmd ->
                context.log.info("VoiceSessionActor command=Leave sessionId={} memberId={}", sessionId, cmd.memberId)
                members.remove(cmd.memberId)
                this
            }
            .onMessage(GetMembers::class.java) { cmd ->
                context.log.info("VoiceSessionActor command=GetMembers sessionId={} memberCount={}", sessionId, members.size)
                cmd.replyTo.tell(members.toList())
                this
            }
            .build()

    sealed interface Command : Serializable
    data class Join(val memberId: String) : Command
    data class Leave(val memberId: String) : Command
    data class GetMembers(val replyTo: ActorRef<List<String>>) : Command
}
