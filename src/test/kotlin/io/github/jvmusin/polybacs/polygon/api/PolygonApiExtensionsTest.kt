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
    Given("getProblemXmlFromZipPackage") {
        When("problem is ok") {
            Then("returns an actual problem.xml") {
                val problemId = TestProblems.problemWithTwoSolutions
                val packageId = polygonApi.getLatestPackageId(problemId)
                val problemXml = polygonApi.getProblemXmlFromZipPackage(problemId, packageId)

                val expected = """
                    <?xml version="1.0" encoding="utf-8" standalone="no"?>
                    <problem revision="1" short-name="polybacs-test-two-solutions" url="https://polygon.codeforces.com/p6RRkdP/Musin/polybacs-test-two-solutions">
                        <names>
                            <name language="english" value="A+B"/>
                        </names>
                        <statements>
                            <statement charset="UTF-8" language="english" mathjax="true" path="statements/english/problem.tex" type="application/x-tex"/>
                            <statement charset="UTF-8" language="english" mathjax="true" path="statements/.html/english/problem.html" type="text/html"/>
                            <statement language="english" path="statements/.pdf/english/problem.pdf" type="application/pdf"/>
                        </statements>
                        <judging cpu-name="Intel(R) Core(TM) i3-8100 CPU @ 3.60GHz" cpu-speed="3600" input-file="" output-file="" run-count="1">
                            <testset name="tests">
                                <time-limit>1000</time-limit>
                                <memory-limit>268435456</memory-limit>
                                <test-count>2</test-count>
                                <input-path-pattern>tests/%02d</input-path-pattern>
                                <answer-path-pattern>tests/%02d.a</answer-path-pattern>
                                <tests>
                                    <test method="manual" sample="true"/>
                                    <test method="manual"/>
                                </tests>
                            </testset>
                        </judging>
                        <files>
                            <resources>
                                <file path="files/olymp.sty"/>
                                <file path="files/problem.tex"/>
                                <file path="files/statements.ftl"/>
                                <file path="files/testlib.h" type="h.g++"/>
                            </resources>
                        </files>
                        <assets>
                            <checker name="std::ncmp.cpp" type="testlib">
                                <source path="files/check.cpp" type="cpp.g++17"/>
                                <binary path="check.exe" type="exe.win32"/>
                                <copy path="check.cpp"/>
                                <testset>
                                    <test-count>0</test-count>
                                    <input-path-pattern>files/tests/checker-tests/%02d</input-path-pattern>
                                    <output-path-pattern>files/tests/checker-tests/%02d.o</output-path-pattern>
                                    <answer-path-pattern>files/tests/checker-tests/%02d.a</answer-path-pattern>
                                    <tests/>
                                </testset>
                            </checker>
                            <solutions>
                                <solution tag="main">
                                    <source path="solutions/main.cpp" type="cpp.g++17"/>
                                    <binary path="solutions/main.exe" type="exe.win32"/>
                                </solution>
                                <solution tag="wrong-answer">
                                    <source path="solutions/wa.cpp" type="cpp.g++17"/>
                                    <binary path="solutions/wa.exe" type="exe.win32"/>
                                </solution>
                            </solutions>
                        </assets>
                        <properties>
                            <property name="tests-wellformed" value="true"/>
                        </properties>
                        <stresses>
                            <stress-count>0</stress-count>
                            <stress-path-pattern>stresses/%03d</stress-path-pattern>
                            <list/>
                        </stresses>
                    </problem>
                    
                """.trimIndent()
                problemXml.shouldBe(expected)
            }
        }
    }
})
