package com.oradian.infra.serverbanner

import scala.annotation.tailrec

class Combinations(chars: String, depth: Int) extends Traversable[String] {
  val length: Long = {
    var sum = 0L
    var level = 0
    while (level < depth) {
      sum = (sum + 1) * chars.length
      level += 1
    }
    sum
  }

  @tailrec
  private[this] def resolve(sb: StringBuilder, length: Long, index: Long): String = {
    val chunk = length / chars.length
    val div = index / chunk
    sb += chars(div.toInt)
    val mul = div * chunk
    if (mul == index) {
      sb.toString
    } else {
      resolve(sb, chunk - 1, index - mul - 1)
    }
  }

  def apply(idx: Long): String =
    resolve(new StringBuilder, length, idx)

  override def foreach[U](f: String => U): Unit = {
    var i = 0L
    val sb = new StringBuilder
    while (i < length) {
      sb.clear()
      f(resolve(sb, length, i))
      i += 1
    }
  }
}

/*
object Combinations extends App {
  val Level = 3
  val Chars = Slant.AllowedChars.mkString.sorted

  val os = new java.io.BufferedOutputStream(java.io.new FileOutputStream(s"${Level}-combinations.txt"))
  new Combinations(Chars, Level) foreach { line =>
    os.write(line getBytes "ISO-8859-1")
    os.write('\n')
  }
  os.close()
}
*/
