package org.scaladebugger.test.bugs

/**
 * Bug from Gitter discussion with @dickwall and @rorygraves.
 *
 * Need to be able to inspect actualTimes, times, and names variables from
 * the specified breakpoint.
 */
object BugFromGitter {
  def main(args: Array[String]): Unit = {
    val h = new BugFromGitter("Rory", 5)
    h.sayHello()
  }
}

class BugFromGitter(name: String, times: Int) {
  def sayHello(): Unit = {
    val actualTimes = times * 2

    for (i <- 1 to actualTimes) // Put breakpoint on this line
      println(s"Hello $name")
  }
}

