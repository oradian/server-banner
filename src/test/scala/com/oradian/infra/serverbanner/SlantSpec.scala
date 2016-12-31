package com.oradian.infra.serverbanner

import Slant.Glyph

class SlantSpec extends InfraSpec {
  def is = s2"""
  Basics
    allowed                        ${testAllowed}
    disallowed                     ${testDisallowed}
    single glyph                   ${testGlyphs}
    glyph pairs                    ${testGlyphPairs}

  Conformity
    use cases                      ${testUseCases}
    3-combinations and variations  ${test3CombinationsAndVariations}

  Performance
    speed test (100K chars)        ${testSpeed(100 * 1000)}
"""

  // ### Basics ###

  private[this] val SortedChars: IndexedSeq[Char] =
    Slant.AllowedChars.toIndexedSeq.sorted

  def testAllowed() =
    Slant(SortedChars.mkString) must not be 'empty

  def testDisallowed() =
    Slant("Dis ? is not allowed ?!?") must throwA(
      new RuntimeException("Characters ' ', '?', '!' are disallowed - valid characters are A-Z, a-z, 0-9, underscore and hyphen"))

  def testGlyphs() = Result.foreach(SortedChars) { ch =>
    val actual = Slant(new String(Array(ch)))
    val expected = Glyph.find(ch, None).toString
    actual ==== expected
  }

  private[this] val TwoCharCombinations: IndexedSeq[(Char, Char)] =
    SortedChars flatMap { ch1 => SortedChars map(ch1 ->) }

  def testGlyphPairs() = Result.foreach(TwoCharCombinations) { case (ch1, ch2) =>
    val actual = Slant(new String(Array(ch1, ch2)))
    val expected = Glyph.find(ch2, Some(ch1)).toString
    actual ==== expected
  }

  // ### Conformity ###

  def testUseCases() =
    getResourceAsLines("slant/use-cases.txt").grouped(1 + Glyph.Height).toSeq map { test =>
      val actual = Slant(test.head)
      val expected = test.tail.mkString("", "\n", "\n")
      actual ==== expected
    }

  def test3CombinationsAndVariations() = {
    val lines = getResourceAsLines("slant/3-combinations.txt").grouped(Glyph.Height)

    def readNextTest(chars: String): Boolean = {
      val actual = Slant(chars)
      val expected = lines.next().mkString("", "\n", "\n")

      if (actual != expected) {
        logger.error(s"""#################################
Expected "${chars.mkString}":
$expected
Actual "${chars.mkString}":
$actual""")
        false
      } else {
        true
      }
    }

    val combs = new Combinations(SortedChars.mkString, 3)
    var passed = 0L
    for (chars <- combs) {
      passed += (if (readNextTest(chars)) 1 else 0)
    }
    passed ==== combs.length
  }

  // ### Performance ###

  def testSpeed(charsLength: Int) = {
    val text = {
      val seed = Random.nextLong()
      logger.debug(f"Speed test is using seed: 0x$seed%016XL")
      val rnd = new Random(seed)
      val sb = new StringBuilder
      (1 to charsLength) foreach { _ =>
        sb += SortedChars(rnd.nextInt(SortedChars.length))
      }
      sb.toString
    }

    val render = time(s"Formatting ${format(charsLength)} chars") {
      Slant(text)
    }

    (render.filter(_ == '\n').size ==== Glyph.Height) and
    (render.length must be_>(Glyph.Height * charsLength))
  }
}
