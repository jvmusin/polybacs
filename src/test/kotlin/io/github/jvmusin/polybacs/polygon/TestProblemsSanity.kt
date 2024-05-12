package io.github.jvmusin.polybacs.polygon

import io.github.jvmusin.polybacs.polygon.api.PolygonApi
import io.github.jvmusin.polybacs.polygon.api.getLatestPackageId
import io.github.jvmusin.polybacs.polygon.api.getSolutionsFromZipPackage
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.maps.shouldHaveKeys
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class TestProblemsSanity(polygonApi: PolygonApi) : BehaviorSpec({
    Given("problem with two solutions id=${TestProblems.problemWithTwoSolutions}") {
        When("checking a folder named 'solutions/'") {
            Then("returns both solutions") {
                val problemId = TestProblems.problemWithTwoSolutions
                val packageId = polygonApi.getLatestPackageId(problemId)
                val solutions = polygonApi.getSolutionsFromZipPackage(problemId, packageId)
                solutions.shouldHaveSize(2)
                solutions.shouldHaveKeys("main.cpp", "wa.cpp")

                val mainCppExpectedContent = """
                    #include <iostream>
                    
                    using namespace std;
                    
                    int main() {
                      int a, b;
                      cin >> a >> b;
                      int ans = 0;
                      for (int i = 0; i < a; i++) ans++;
                      for (int i = 0; i < b; i++) ans++;
                      cout << ans << endl;
                      return 0;
                    }
                """.trimIndent().replace("\n", "\r\n")
                val waCppExpectedContent = """
                    #include <iostream>
                    
                    using namespace std;
                    
                    int main() {
                      int a, b;
                      cin >> a >> b;
                      if (a + b > 100) cout << 99;
                      else cout << a + b;
                      return 0;
                    }
                """.trimIndent().replace("\n", "\r\n")

                solutions["main.cpp"]!!.solution.shouldBe(mainCppExpectedContent)
                solutions["wa.cpp"]!!.solution.shouldBe(waCppExpectedContent)
            }
        }
    }
})
