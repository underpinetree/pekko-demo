package ee.takahiro.pekkodemo.actor

import org.apache.pekko.actor.typed.ActorRef

interface VoiceSessionSerializable

data class MembersResponse(val members: List<String>) : VoiceSessionSerializable

sealed interface Command : VoiceSessionSerializable
data class Join(val memberId: String) : Command
data class Leave(val memberId: String) : Command
data class GetMembers(val replyTo: ActorRef<MembersResponse>) : Command

sealed interface Event : VoiceSessionSerializable
data class MemberJoined(val memberId: String) : Event
data class MemberLeft(val memberId: String) : Event

data class State(val members: Set<String> = emptySet()) : VoiceSessionSerializable
