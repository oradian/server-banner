package com.oradian.infra.serverbanner

object Greeting {
  private[this] val transform = Map(
    ' ' -> " "
  , '\t' -> "  "
  ).withDefault(_.toUpper + " ")

  def apply(text: String) = {
    val sb = new StringBuilder
    for (ch <- text) {
      sb ++= transform(ch)
    }
    sb.toString.trim + "\n"
  }
}
