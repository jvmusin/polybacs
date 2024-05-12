package io.github.jvmusin.polybacs.polygon.api

import io.github.jvmusin.polybacs.polygon.TestProblems
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.maps.shouldHaveKeys
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PolygonApiExtensionsTest(polygonApi: PolygonApi) : BehaviorSpec({
    Given("getFileFromZipPackage") {
        When("The file exists") {
            Then("returns the file content") {
                val problemId = TestProblems.problemWithTwoSolutions
                val packageId = polygonApi.getLatestPackageId(problemId)
                val file = polygonApi.getFileFromZipPackage(problemId, packageId, "solutions/main.cpp")
                file.shouldNotBeNull()
                val expected = """
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
                file.decodeToString().shouldBe(expected)
            }
        }
    }
    Given("getSolutionsFromZipPackage") {
        When("There are multiple solutions") {
            Then("Returns them all") {
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
