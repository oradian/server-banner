package com.oradian.infra.serverbanner

class ScrollSpec extends InfraSpec {
  def is = s2"""
  Basics
    empty scroll                ${testScroll(0)}
    two-char wide scroll        ${testScroll(2)}
    128-char wide scroll        ${testScroll(128)}

  Performance
    huge-ass scroll (10M body)  ${testSpeed(10 * 1000 * 1000)}
"""

  // ### Basics ###

  def testScroll(bodyLength: Int) =
    Scroll(bodyLength) ==== getResourceAsString(s"scroll/${bodyLength}-scroll.txt")

  // ### Performance ###

  def testSpeed(bodyLength: Int) = {
    val render = time(s"Creating ${format(bodyLength)}-char wide scroll") {
      Scroll(bodyLength)
    }

    val referenceLength = getResourceAsString("scroll/0-scroll.txt").length
    val nonEmptyLines = Scroll.Height - 2
    render.length ==== (referenceLength + bodyLength * nonEmptyLines)
  }
}
