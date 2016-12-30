package com.oradian.infra.serverbanner
package slant

import org.slf4j.LoggerFactory
import org.specs2._

import scala.io.Source
import scala.util.Random

import Slant._

class SlantSpec extends Specification {
  val is = s2"""
  Basics
    single glyph                     ${testGlyphs}
    glyph pairs                      ${testGlyphPairs}

  Conformity tests
    use cases                        ${testUseCases}
    3-combinations and variations    ${test3CombinationsAndVariations}

  Performance
    speed test (1M chars under 60s)  ${testSpeed(1000 * 1000, 60)}
"""

  private[this] val SortedChars: IndexedSeq[Char] =
    AllowedChars.toIndexedSeq.sorted

  def testGlyphs() =
    for (ch <- SortedChars) yield {
      val actual = Slant(new String(Array(ch)))
      val expected = Glyph.find(ch, None).toString
      actual ==== expected
    }

  def testGlyphPairs() =
    for (ch1 <- SortedChars; ch2 <- SortedChars) yield {
      val actual = Slant(new String(Array(ch1, ch2)))
      val expected = Glyph.find(ch2, Some(ch1)).toString
      actual ==== expected
    }

  private[this] def getLines(resource: String): Iterator[String] =
    Source.fromInputStream(getClass.getResourceAsStream(resource)).getLines

  def testUseCases() =
    getLines("use-cases.txt").grouped(1 + Glyph.Height).toSeq map { test =>
      val actual = Slant(test.head)
      val expected = test.tail.mkString("", "\n", "\n")
      actual ==== expected
    }

  private[this] lazy val logger = LoggerFactory.getLogger(getClass)

  def test3CombinationsAndVariations() = {
    val lines = getLines("3-combinations.txt").grouped(Glyph.Height)

    def readNextTest(chars: Char*): Int = {
      val actual = Slant(new String(chars.toArray))
      val expected = lines.next().mkString("", "\n", "\n")

      if (actual != expected) {
        logger.error(s"""#################################
Expected "${chars.mkString}":
$expected
Actual "${chars.mkString}":
$actual""")
        0
      } else {
        1
      }
    }

    var passed = 0
    for (ch1 <- SortedChars) {
      passed += readNextTest(ch1)
      for (ch2 <- SortedChars) {
        passed += readNextTest(ch1, ch2)
        for (ch3 <- SortedChars) {
          passed += readNextTest(ch1, ch2, ch3)
        }
      }
    }

    val size = SortedChars.size
    passed ==== (size + size * size + size * size * size)
  }

  def testSpeed(count: Int, belowSeconds: Long) = {
    val text = {
      val seed = Random.nextLong()
      logger.debug(f"Speed test is using seed: 0x$seed%016XL")
      val rnd = new Random(seed)
      val sb = new StringBuilder
      (1 to count) foreach { _ =>
        sb += SortedChars(rnd.nextInt(SortedChars.length))
      }
      sb.toString
    }

    val startAt = System.currentTimeMillis()
    val render = Slant(text)
    val endAt = System.currentTimeMillis()

    val tookMs = endAt - startAt
    logger.debug(s"Speed test for ${count} took ${tookMs}ms")
    (render.filter(_ == '\n').size ==== Glyph.Height) and
    (render.length must be_>(Glyph.Height * count)) and
    (tookMs must be < belowSeconds * 1000L)
  }
}
