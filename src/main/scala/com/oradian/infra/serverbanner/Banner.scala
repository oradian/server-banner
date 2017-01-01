package com.oradian.infra.serverbanner

import scala.collection.mutable.ArrayBuffer

object Banner extends App {
  val greeting = ColorText(Greeting("Pure  Scala  Server  MOTD  generator"), Color.Green)
  val slant = ColorText(Slant("server-banner"), Color.Yellow)
  val scroll = ColorText(Scroll(slant.width + 6, slant.height), Color.Red)

  val canvas = new Canvas(scroll.width, scroll.height)
    .draw(Drawing(greeting, 8, 2, 0))
    .draw(Drawing(slant,   11, 4, 1))
    .draw(Drawing(scroll,   0, 0, 0))

  println(canvas.renderText)
}

sealed trait Color
object Color {
  case object Red extends Color
  case object Yellow extends Color
  case object Green extends Color
}

case class ColorText(text: String, color: Color) {
  val lines = text split '\n'
  val height = lines.length
  val width = lines.map(_.length).max
}
case class Drawing(colorText: ColorText, x: Int, y: Int, z: Int)

class Canvas(val width: Int, val height: Int) {
  private[this] var drawings = new ArrayBuffer[Drawing]

  def draw(drawing: Drawing): this.type = {
    drawings += drawing
    this
  }

  def renderText: String = {
    val buffer = Array.fill(width * height){' '}

    for (drawing <- drawings.sortBy(_.z)) {
      for (y <- 0 until drawing.colorText.height) {
        val pY = y + drawing.y
        if (pY >= 0 && pY < height) {
          for (x <- 0 until drawing.colorText.width) {
            if (x < drawing.colorText.lines(y).length) {
              val pX = x + drawing.x
              if (pX >= 0 && pX < width) {
                val ch = drawing.colorText.lines(y)(x)
                if (ch != ' ') {
                  buffer(pX + pY * width) = ch
                }
              }
            }
          }
        }
      }
    }

    val sb = new StringBuilder
    for (y <- 0 until height) {
      sb ++= new String(buffer, y * width, width).replaceFirst(" *$", "\n")
    }
    sb.toString
  }
}
