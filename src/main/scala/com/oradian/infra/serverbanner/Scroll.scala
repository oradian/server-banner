package com.oradian.infra.serverbanner

import scala.io.Source

object Scroll {
  final val Height = 12

  private[this] class ScrollPart private (val left: Array[Char], val body: Array[Char], val right: Array[Char])
  private[this] object ScrollPart {
    final val BodyLineSize = 100

    def apply(left: String, body: Option[Char], right: String): ScrollPart = {
      val bodyLine = body.map(ch => Array.fill(BodyLineSize){ch}).getOrElse(Array.emptyCharArray)
      new ScrollPart(left.toCharArray, bodyLine, right.toCharArray)
    }
  }

  private[this] val scrollParts: List[ScrollPart] = {
    val lines = Source.fromInputStream(getClass.getResourceAsStream("scroll.txt"))
      .getLines().toList.ensuring(_.length == Height)

    val breaks = {
      val maxLength = lines.map(_.length).max
      val paddedLines = lines map { line => line + " " * (maxLength - line.length) }
      val columnBreaks = paddedLines.transpose.map(_.forall(' ' ==))
      columnBreaks.zipWithIndex.filter(_._1).map(_._2)
    }

    val leftBreak :: rightBreak :: Nil = breaks
    assert(leftBreak + 2 == rightBreak)

    lines map { line =>
      ScrollPart(
        left = line.take(leftBreak)
      , body = line.drop(leftBreak + 1).headOption
      , right = line.drop(rightBreak + 1)
      )
    }
  }

  def apply(bodyLength: Int): String = {
    val sb = new java.lang.StringBuilder
    scrollParts foreach { part =>
      sb.append(part.left)
      if (part.body ne Array.emptyCharArray) {
        var x = bodyLength
        do {
          val chunkLength = math.min(x, ScrollPart.BodyLineSize)
          sb.append(part.body, 0, chunkLength)
          x -= chunkLength
        } while(x > 0)
        sb.append(part.right)
      }
      sb.append('\n')
    }
    sb.toString
  }
}
