package com.auginte.distribution.data

import com.auginte.test.UnitSpec

/**
 * Unit test for [[com.auginte.distribution.data.Version]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class VersionSpec extends UnitSpec {

  "Version object" must {
    val v1_2_3 = Version("1.2.3")
    val v1_2_3_snapshot = Version("1.2.3-SNAPSHOT")
    val v99_99_99_snapshot = Version("99.99.99-SNAPSHOT")
    val v999_999_999 = Version("999.999.999")
    val v999_999_999_snapshot = Version("999.999.999-SNAPSHOT")
    "have textual representation" in {
      assert("1.2.3" === v1_2_3.toString)
      assert("1.2.3-SNAPSHOT" === v1_2_3_snapshot.toString)
      assert("99.99.99-SNAPSHOT" === v99_99_99_snapshot.toString)
      assert("999.999.999" === v999_999_999.toString)
      assert("999.999.999-SNAPSHOT" === v999_999_999_snapshot.toString)
    }
    "have numeric representation" in {
      assert(1.002003 === v1_2_3.numeric)
      assert(1.002003 === v1_2_3_snapshot.numeric)
      assert(99.099099 === v99_99_99_snapshot.numeric)
      assert(999.999999 === v999_999_999.numeric)
      assert(999.999999 === v999_999_999_snapshot.numeric)
    }
    "have stability attribute" in {
      assert(v1_2_3.stable)
      assert(!v1_2_3_snapshot.stable)
      assert(!v99_99_99_snapshot.stable)
      assert(v999_999_999.stable)
      assert(!v999_999_999_snapshot.stable)
    }
    "be comparable" in {
      assert(v1_2_3 < v99_99_99_snapshot)
      assert(v99_99_99_snapshot < v999_999_999)
      assert(v99_99_99_snapshot > v1_2_3)
      assert(v999_999_999 > v99_99_99_snapshot)
      val v1_2_4 = Version("1.2.4")
      val v1_2_2 = Version("1.2.2")
      val v1_2_03 = Version("1.2.3")
      assert(v1_2_3 < v1_2_4)
      assert(v1_2_3 <= v1_2_4)
      assert(v1_2_3 <= v1_2_03)
      assert(v1_2_3 > v1_2_2)
      assert(v1_2_3 >= v1_2_2)
      assert(v1_2_3 >= v1_2_03)
      val v1_3_3 = Version("1.2.4")
      val v1_1_3 = Version("1.2.2")
      assert(v1_2_3 < v1_3_3)
      assert(v1_2_3 <= v1_3_3)
      assert(v1_2_3 > v1_1_3)
      assert(v1_2_3 >= v1_1_3)
    }
  }
}
