package ee.takahiro.pekkodemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PekkoDemoApplication

fun main(args: Array<String>) {
    runApplication<PekkoDemoApplication>(*args)
}
