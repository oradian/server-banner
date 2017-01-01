package com.oradian.infra.serverbanner

class GreetingSpec extends InfraSpec {
  def is = s2"""
  Basics
    empty greeting  ${testEmpty}
    welcome         ${testWelcome}
    testWhitespace  ${testWhitespace}
"""

  // ### Basics ###

  def testEmpty() =
    Greeting("") ==== "\n"

  def testWelcome() =
    Greeting("Welcome to") ==== "W E L C O M E  T O\n"

  def testWhitespace() =
    Greeting("I am\tTAB\tto meet you!") ==== "I  A M   T A B   T O  M E E T  Y O U !\n"
}
