package com.oradian.infra.serverbanner
package slant

object Slant {
  val AllowedChars: Set[Char] =
    Set('-', '_') ++ ('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9')

  private[slant] object Glyph {
    final val Height = 6
    final val OneMaxWidth = 13
    final val TwoWidth = 23

    private[this] final val BufferRowLength = TwoWidth * AllowedChars.size + 1
    private[this] val buffer: Array[Byte] = {
      val len = BufferRowLength * Height * (AllowedChars.size + 1)
      val tmp = new Array[Byte](len)
      getClass.getResourceAsStream("/matrix.txt")
        .read(tmp)
        .ensuring(_ == len, "Incompatible buffer size (read from slant.txt)")
      tmp
    }

    private[this] val lookup: Map[(Char, Option[Char]), Glyph] = {
      val chars = AllowedChars.toIndexedSeq.sorted
      val reverse = chars.zipWithIndex.toMap

      (chars flatMap { ch1 =>
        val offsetX = TwoWidth * reverse(ch1)
        val g1 = new Glyph(offsetX, None)
        val head = (ch1, None) -> g1

        val tail = chars map { ch2 =>
          val offsetY = (reverse(ch2) + 1) * BufferRowLength * Height
          val g12 = new Glyph(offsetX + offsetY, Some(g1))
          (ch2, Some(ch1)) -> g12
        }

        head +: tail
      }).toMap
    }

    def find(ch: Char, prev: Option[Char]): Glyph = lookup((ch, prev))

    private def charAt(offset: Int, x: Int, y: Int): Char =
      buffer(offset + x + y * BufferRowLength).toChar
  }

  private[slant] class Glyph private(offset: Int, val parent: Option[Glyph]) {
    def charAt(x: Int, y: Int): Char =
      Glyph.charAt(offset, x, y)

    val lineEndings =
      for (y <- 0 until Glyph.Height) yield {
        (Glyph.TwoWidth - 1 to 0 by -1) find { x =>
          charAt(x, y) != ' '
        } getOrElse -1
      }

    val maxLineLength = lineEndings.max + 1

    override def toString: String = {
      val sb = new StringBuilder()
      for (y <- 0 until Glyph.Height) {
        for (x <- 0 to lineEndings(y)) {
          sb += charAt(x, y)
        }
        sb += '\n'
      }
      sb.toString
    }
  }

  private[this] val _pushConditions = Set(
    "FY", "TY", "fY"
  , "P4", "P7", "PZ"
  , "r4"
  )

  private[this] val _smushConditions = Set(
    "FV/", "rV/", "rv/", "fV/", "Pv/", "TV/", "PV/"
  , "FW/", "rW/", "rw/", "fW/", "Pw/", "TW/", "PW/"
  , "PV\\"
  , "PW\\"
  , "P-/", "r-/"
  )

  private[this] val NoPrev = IndexedSeq.fill(Glyph.Height){-1}

  def apply(text: String): String = {
    locally {
      val disallowed = text.filterNot(AllowedChars)
      if (disallowed.nonEmpty) {
        sys.error(s"Characters ${disallowed.mkString("'", "', '", "'")}")
      }
    }

    val outputRowLength = text.length * Glyph.OneMaxWidth
    val output = Array.fill(outputRowLength * Glyph.Height){' '}

    def writeChar(ch: Char, last: Option[Char], prev: Option[Char], pos: Int): Int = {
      val glyph = Glyph.find(ch, last)

      val _underscore = prev.isDefined && last.get == '_'
      val _push = _underscore && _pushConditions(new String(Array(prev.get, ch)))

      val lastLineEndings = if (last.isDefined) {
        val offsets = glyph.parent.get.lineEndings
        if (_push) offsets.map(_ - 1) else offsets
      } else {
        NoPrev
      }

      val lastMaxLineLength = lastLineEndings.max + 1

      var y = 0
      while (y < Glyph.Height) {
        val offset = pos - lastMaxLineLength + y * outputRowLength
        val lineEnding = glyph.lineEndings(y)
        var x = math.max(0, lastLineEndings(y))
        while (x <= lineEnding) {
          val to = glyph.charAt(x, y)
          if (to != ' ') {
            val index = offset + x
            val old = output(index)
            val to_fix = if (_underscore && old != ' ' && _smushConditions(new String(Array(prev.get, ch, old)))) old else to
            output(index) = to_fix
          }
          x += 1
        }
        y += 1
      }

      if (_push) {
        val lastSlash = output.lastIndexOf('/')
        output(lastSlash - 1) = '/'
        output(lastSlash) = ' '
      }

      pos + glyph.maxLineLength - lastMaxLineLength
    }

    var index = 0
    var pos = 0
    var last = Option.empty[Char]
    var prev = Option.empty[Char]
    while (index < text.length) {
      val ch = text.charAt(index)
      pos = writeChar(ch, last, prev, pos)
      prev = last
      last = Some(ch)
      index += 1
    }

    val sb = new StringBuilder
    for (y <- 0 until Glyph.Height) yield {
      sb ++= new String(output, outputRowLength * y, pos).replaceFirst(" *$", "\n")
    }
    sb.toString
  }
}
