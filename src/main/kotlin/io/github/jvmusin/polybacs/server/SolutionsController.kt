//package io.github.jvmusin.polybacs.server
//
//import io.github.jvmusin.polybacs.api.Language
//import io.github.jvmusin.polybacs.api.Solution
//import io.github.jvmusin.polybacs.api.Verdict
//import io.github.jvmusin.polybacs.polygon.PolygonService
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.PathVariable
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//@RequestMapping("/api/problems/{problemId}/solutions")
//class SolutionsController(
//    private val polygonService: PolygonService,
//) {
//    @GetMapping
//    suspend fun getSolutions(@PathVariable problemId: Int): List<Solution> {
//        val problem = polygonService.downloadProblem(problemId)
//        return problem.solutions.map {
//            Solution(
//                it.name,
//                Language.valueOf(it.language.name),
//                Verdict.valueOf(it.verdict.name)
//            )
//        }
//    }
//}
