package ee.takahiro.pekkodemo.config

import com.typesafe.config.ConfigFactory
import ee.takahiro.pekkodemo.actor.VoiceSessionActor
import jakarta.annotation.PreDestroy
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding
import org.apache.pekko.cluster.sharding.typed.javadsl.Entity
import org.apache.pekko.management.cluster.bootstrap.ClusterBootstrap
import org.apache.pekko.management.javadsl.PekkoManagement
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PekkoConfig {

    private lateinit var system: ActorSystem<Void>

    @Bean
    fun actorSystem(): ActorSystem<Void> {
        val config = ConfigFactory.load()
        system = ActorSystem.create(Behaviors.empty(), "VoiceSessionSystem", config)
        return system
    }

    @Bean
    fun clusterSharding(actorSystem: ActorSystem<Void>): ClusterSharding {
        val config = actorSystem.settings().config()
        if (config.hasPath("pekko.use-cluster-bootstrap") && config.getBoolean("pekko.use-cluster-bootstrap")) {
            PekkoManagement.get(actorSystem).start()
            ClusterBootstrap.get(actorSystem).start()
        }

        val sharding = ClusterSharding.get(actorSystem)
        sharding.init(
            Entity.of(VoiceSessionActor.TYPE_KEY) { ctx ->
                VoiceSessionActor.create(ctx.entityId)
            }
        )
        return sharding
    }

    @PreDestroy
    fun shutdown() {
        if (::system.isInitialized) {
            system.terminate()
        }
    }
}
