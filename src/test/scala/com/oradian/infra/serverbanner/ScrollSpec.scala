package com.oradian.infra.serverbanner

class ScrollSpec extends InfraSpec {
  def is = s2"""
  Basics
    0 x 0 fail      ${testDisallowed}

    1 x 1 scroll    ${testScroll(1, 1)}
    5 x 2 scroll    ${testScroll(5, 2)}
    12 x 3 scroll   ${testScroll(12, 3)}
    17 x 4 scroll   ${testScroll(17, 4)}
    27 x 5 scroll   ${testScroll(27, 5)}
    113 x 9 scroll  ${testScroll(113, 9)}

  Performance
    huge-ass scroll (100M body)  ${testSpeed(7 * 1000 * 1000, 9)}
"""

  // ### Basics ###

  def testDisallowed() =
    (Scroll(0, 1) must throwA(new IllegalArgumentException("requirement failed: Scroll body width must be positive, got: 0"))) and
    (Scroll(1, 0) must throwA(new IllegalArgumentException("requirement failed: Scroll body height must be positive, got: 0")))

  def testScroll(bodyWidth: Int, bodyHeight: Int) =
    Scroll(bodyWidth, bodyHeight) ==== getResourceAsString(s"scroll/${bodyWidth}x${bodyHeight}.txt")

  // ### Performance ###

  def testSpeed(bodyWidth: Int, bodyHeight: Int) = {
    val reference = getResourceAsString("scroll/113x9.txt")
    val resizableHeight = reference.count(_ == '\n') - 3

    val render = time(s"Creating ~ ${format(bodyWidth * resizableHeight)}-char wide scroll") {
      Scroll(bodyWidth, bodyHeight)
    }

    val lengthDifference = bodyWidth - 113
    render.length ==== (reference.length + lengthDifference * resizableHeight)
  }
}
