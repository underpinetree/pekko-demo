package ee.takahiro.pekkodemo

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
class PekkoDemoApplicationTests {

    @MockitoBean
    private lateinit var actorSystem: ActorSystem<Void>

    @MockitoBean
    private lateinit var clusterSharding: ClusterSharding

    @Test
    fun contextLoads() {
    }
}
