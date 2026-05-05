package ee.takahiro.pekkodemo.controller

import ee.takahiro.pekkodemo.actor.GetMembers
import ee.takahiro.pekkodemo.actor.Join
import ee.takahiro.pekkodemo.actor.Leave
import ee.takahiro.pekkodemo.actor.MembersResponse
import ee.takahiro.pekkodemo.actor.VoiceSessionActor
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@RestController
@RequestMapping("/sessions")
class SessionController(private val sharding: ClusterSharding) {

    @PostMapping("/{sessionId}/members/{memberId}/join")
    fun join(
        @PathVariable sessionId: String,
        @PathVariable memberId: String,
    ): ResponseEntity<Void> {
        sharding.entityRefFor(VoiceSessionActor.TYPE_KEY, sessionId)
            .tell(Join(memberId))
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{sessionId}/members/{memberId}/leave")
    fun leave(
        @PathVariable sessionId: String,
        @PathVariable memberId: String,
    ): ResponseEntity<Void> {
        sharding.entityRefFor(VoiceSessionActor.TYPE_KEY, sessionId)
            .tell(Leave(memberId))
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{sessionId}/members")
    fun getMembers(@PathVariable sessionId: String): ResponseEntity<List<String>> {
        val response = sharding.entityRefFor(VoiceSessionActor.TYPE_KEY, sessionId)
            .ask<MembersResponse>(::GetMembers, Duration.ofSeconds(3))
            .toCompletableFuture()
            .get()
        return ResponseEntity.ok(response.members)
    }
}
