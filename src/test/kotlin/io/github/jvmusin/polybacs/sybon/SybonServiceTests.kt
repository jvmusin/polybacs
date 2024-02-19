package io.github.jvmusin.polybacs.sybon

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SybonServiceTests(testSybonArchiveService: SybonArchiveService) : StringSpec({
    "Import problem" {
        val problem = testSybonArchiveService.importProblem(A_PLUS_B_BACS_PROBLEM_ID)
        problem.shouldNotBeNull()
        problem.id shouldBe A_PLUS_B_SYBON_PROBLEM_ID
        problem.internalProblemId shouldBe A_PLUS_B_BACS_PROBLEM_ID
    }
}) {
    private companion object {
        const val A_PLUS_B_BACS_PROBLEM_ID = "1000pre"
        const val A_PLUS_B_SYBON_PROBLEM_ID = 82120
    }
}
