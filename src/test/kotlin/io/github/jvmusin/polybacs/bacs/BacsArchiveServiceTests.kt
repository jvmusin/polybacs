package io.github.jvmusin.polybacs.bacs

import io.github.jvmusin.polybacs.bacs.BacsProblemState.IMPORTED
import io.github.jvmusin.polybacs.bacs.BacsProblemState.NOT_FOUND
import io.kotest.assertions.retry
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SpringBootTest
class BacsArchiveServiceTests(service: BacsArchiveService) : BehaviorSpec({
    Given("getProblemState") {
        When("problem is imported correctly") {
            Then("returns status IMPORTED") {
                retry(3, 5.minutes, 3.seconds) {
                    service.getProblemState("polybacs-frog-and-polygon-ok") shouldBe IMPORTED
                }
            }
        }
        When("problem does not exist") {
            Then("returns status NOT_FOUND") {
                retry(3, 5.minutes, 3.seconds) {
                    service.getProblemState("not-existing-problem") shouldBe NOT_FOUND
                }
            }
        }
    }
})
