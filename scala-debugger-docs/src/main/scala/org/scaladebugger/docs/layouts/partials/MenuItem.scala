package org.scaladebugger.docs.layouts.partials

case class MenuItem(
  name: String,
  link: String = "#!",
  children: Seq[MenuItem] = Nil
)
