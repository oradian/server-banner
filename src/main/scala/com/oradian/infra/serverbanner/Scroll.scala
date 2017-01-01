package com.oradian.infra.serverbanner

import scala.io.Source

object Scroll {
  private[this] val template: String =
    new String(Source.fromInputStream(getClass.getResourceAsStream("scroll.txt")).toArray)

  private[this] final val Row = "\n|       |"
  private[this] def factory(height: Int): String = height match {
    case 1 => template
      .replace("x", "")
      .replace("y", "")
      .replace("z", "")
    case 2 => template
      .replace("u/", "s |")
      .replace("x", "u/")
      .replace("y", "")
      .replace("z", "")
    case 3 => template
      .replace("u/", "s |")
      .replace("x", "s |")
      .replace("y", "u/")
      .replace("z", Row)
    case 4 => template
      .replace("u/", "s |")
      .replace("x", "s |")
      .replace("y", "s |")
      .replace("z", "u/" + Row)
    case x => template
      .replace("u/", "s |")
      .replace("x", "s |")
      .replace("y", "s |")
      .replace("z", ("s |" + Row) * (x - 4) + "u/" + Row)
  }

  def apply(bodyWidth: Int, bodyHeight: Int): String = {
    require(bodyWidth > 0, "Scroll body width must be positive, got: " + bodyWidth)
    require(bodyHeight > 0, "Scroll body height must be positive, got: " + bodyHeight)

    val bufferSize = 100
    val spaces = Array.fill(bufferSize){' '}
    val underscores = Array.fill(bufferSize){'_'}

    val sb = new java.lang.StringBuilder
    for (ch <- factory(bodyHeight)) {
      if (ch == 's' || ch == 'u') {
        val rep = if (ch == 's') spaces else underscores
        var i = bodyWidth - 1
        do {
          val chunkSize = math.min(i, bufferSize)
          sb.append(rep, 0, chunkSize)
          i -= chunkSize
        } while (i > 1)
      } else sb.append(ch)
    }
    sb.toString
  }
}
