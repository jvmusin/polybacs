package io.github.jvmusin.polybacs.polygon.converter

import io.github.jvmusin.polybacs.ir.IRTestGroupPointsPolicy
import io.github.jvmusin.polybacs.polygon.api.TestGroup

object PolygonPointsPolicyConverter {
    fun convert(pointsPolicyType: TestGroup.PointsPolicyType) = when (pointsPolicyType) {
        TestGroup.PointsPolicyType.COMPLETE_GROUP -> IRTestGroupPointsPolicy.COMPLETE_GROUP
        TestGroup.PointsPolicyType.EACH_TEST -> IRTestGroupPointsPolicy.EACH_TEST
    }
}
