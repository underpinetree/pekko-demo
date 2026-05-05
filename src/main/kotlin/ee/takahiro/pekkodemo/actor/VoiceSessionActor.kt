package ee.takahiro.pekkodemo.actor

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.ActorContext
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.cluster.Cluster
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityTypeKey
import org.apache.pekko.persistence.typed.PersistenceId
import org.apache.pekko.persistence.typed.javadsl.CommandHandler
import org.apache.pekko.persistence.typed.javadsl.Effect
import org.apache.pekko.persistence.typed.javadsl.EventHandler
import org.apache.pekko.persistence.typed.javadsl.EventSourcedBehavior

class VoiceSessionActor private constructor(
    private val sessionId: String,
    private val ctx: ActorContext<Command>,
    persistenceId: PersistenceId,
) : EventSourcedBehavior<Command, Event, State>(persistenceId) {

    companion object {
        val TYPE_KEY: EntityTypeKey<Command> = EntityTypeKey.create(Command::class.java, "VoiceSession")

        fun create(sessionId: String): Behavior<Command> =
            Behaviors.setup { ctx ->
                val address = Cluster.get(ctx.system.classicSystem()).selfMember().address()
                ctx.log.info("VoiceSessionActor started: sessionId={} on node={}", sessionId, address)
                VoiceSessionActor(sessionId, ctx, PersistenceId.ofUniqueId("VoiceSession|$sessionId"))
            }
    }

    override fun emptyState(): State = State()

    override fun commandHandler(): CommandHandler<Command, Event, State> =
        newCommandHandlerBuilder()
            .forAnyState()
            .onCommand(Join::class.java) { _, cmd ->
                Effect().persist(MemberJoined(cmd.memberId))
                    .thenRun { _: State ->
                        ctx.log.info("VoiceSessionActor command=Join sessionId={} memberId={}", sessionId, cmd.memberId)
                    }
            }
            .onCommand(Leave::class.java) { _, cmd ->
                Effect().persist(MemberLeft(cmd.memberId))
                    .thenRun { _: State ->
                        ctx.log.info("VoiceSessionActor command=Leave sessionId={} memberId={}", sessionId, cmd.memberId)
                    }
            }
            .onCommand(GetMembers::class.java) { state, cmd ->
                ctx.log.info("VoiceSessionActor command=GetMembers sessionId={} memberCount={}", sessionId, state.members.size)
                cmd.replyTo.tell(MembersResponse(state.members.toList()))
                Effect().none()
            }
            .build()

    override fun eventHandler(): EventHandler<State, Event> =
        newEventHandlerBuilder()
            .forAnyState()
            .onEvent(MemberJoined::class.java) { state, event ->
                state.copy(members = state.members + event.memberId)
            }
            .onEvent(MemberLeft::class.java) { state, event ->
                state.copy(members = state.members - event.memberId)
            }
            .build()
}
