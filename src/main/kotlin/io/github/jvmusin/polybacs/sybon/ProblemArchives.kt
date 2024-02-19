package io.github.jvmusin.polybacs.sybon

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

interface ProblemArchive {
    val collectionId: Int
}

object MainProblemArchive : ProblemArchive {
    override val collectionId = 1
}

object TestProblemArchive : ProblemArchive {
    override val collectionId = 10023
}

@Configuration
class ProblemArchivesConfig {
    @Bean
    fun mainProblemArchive(): ProblemArchive = MainProblemArchive

    @Bean
    fun testProblemArchive(): ProblemArchive = TestProblemArchive
}