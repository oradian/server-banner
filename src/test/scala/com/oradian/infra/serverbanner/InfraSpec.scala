package com.oradian.infra.serverbanner

import org.slf4j.LoggerFactory
import org.specs2.Specification

import scala.io.Source

trait InfraSpec extends Specification {
  protected lazy val logger = LoggerFactory.getLogger(getClass)

  // ### Utils ###

  def time[T](section: String)(runSection: => T): T = {
    logger.trace(s"${section} started ...")
    val startAt = System.currentTimeMillis
    val res = runSection
    val endAt = System.currentTimeMillis
    logger.debug(s"${section} took ${endAt - startAt}ms ###")
    res
  }

  def format(number: Int): String =
    java.text.NumberFormat
      .getInstance(java.util.Locale.ROOT)
      .format(number.toLong)

  // ### Aliases ###

  val Random = scala.util.Random
  type Random = scala.util.Random

  val Result = org.specs2.execute.Result

  // ### Resources ###

  private[this] def sourceFrom(resource: String) =
    Source.fromInputStream(getClass.getResourceAsStream(resource))

  protected def getResourceAsString(resource: String): String =
    new String(sourceFrom(resource).toArray)

  protected def getResourceAsLines(resource: String): Iterator[String] =
    sourceFrom(resource).getLines
}
