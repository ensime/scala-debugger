package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.styles.TabsStyle

import scalatags.Text.all._
import scalatags.stylesheet.Cls

/**
 * Generates a collection of tabs.
 */
object Tabs {
  class Tab private (
    val name: String,
    val content: Seq[Modifier],
    val noInner: Boolean
  )

  object Tab {
    def apply(name: String, content: Modifier*): Tab =
      new Tab(name, content, noInner = false)

    object NoInner {
      def apply(name: String, content: Modifier*): Tab =
        new Tab(name, content, noInner = true)
    }
  }

  /** Used for a series of tabs with a light content background. */
  object Light {
    def apply(identifier: String, tabs: Tab*): Modifier =
      generate(identifier, TabsStyle.tabsLight, tabs)
  }

  /** Used for a series of tabs with a dark content background. */
  object Dark {
    def apply(identifier: String, tabs: Tab*): Modifier =
      generate(identifier, TabsStyle.tabsDark, tabs)
  }

  private def generate(
    identifier: String,
    mainTabClass: Cls,
    tabs: Seq[Tab]
  ): Modifier = {
    @inline def toListItem(index: Int, tab: Tab, totalTabs: Int): Modifier = {
      val tabClass: Cls =
        if (index == 0) TabsStyle.firstTab
        else if (index == totalTabs - 1) TabsStyle.lastTab
        else TabsStyle.normalTab

      def radioInput(content: Modifier*): Modifier = {
        if (index == 0) input(Seq(`type` := "radio", checked) ++ content: _*)
        else input(Seq(`type` := "radio") ++ content: _*)
      }

      def createListItem(content: Modifier*): Modifier = {
        li(
          tabClass,
          radioInput(
            name := s"tabs-$identifier",
            id := s"tab-$identifier-$index"
          ),
          label(
            `for` := s"tab-$identifier-$index",
            role := "tab",
            attr("aria-selected") := true,
            attr("aria-controls") := s"$identifier-panel",
            tabindex := index
          )(tab.name),
          div(
            TabsStyle.tabContent,
            id := s"tab-content-$identifier-$index",
            role := "tabpanel",
            attr("aria-labelledby") := tab.name,
            attr("aria-hidden") := "false"
          )(content: _*)
        )
      }

      if (tab.noInner) createListItem(tab.content: _*)
      else createListItem(div(TabsStyle.tabInnerContent)(tab.content))
    }

    ul(TabsStyle.tabs, mainTabClass, role := "tablist")(
      tabs.zipWithIndex.map { case (tab, index) =>
        toListItem(index, tab, tabs.length)
      }
    )
  }
}
