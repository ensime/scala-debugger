package org.scaladebugger.docs.styles

import scalatags.Text.all._
import scalatags.stylesheet._

/**
 * Represents stylesheet for tab-oriented content.
 */
object TabsStyle extends CascadingStyleSheet {
  /** To be placed on <ul> element. */
  lazy val tabs: Cls = cls(
    display := "flex",
    flexWrap := "nowrap",
    justifyContent := "flex-start",
    alignItems := "center",

    position := "relative",
    listStyle := "none",
    width := "100%",
    padding := "0px",
    margin := "0px",

    minWidth := "750px",
    maxWidth := "800px",

    li(
      display := "inline-block",
      flexGrow := "1",
      margin := "0px",
      padding := "0px"
    ),

    Selector("input[type='radio']")(
      display := "none",
      position := "absolute"
    ),

    label(
      display := "inline-block",
      width := "100%",
      textAlign := "center",
      padding := "0.8em 0em",
      fontSize := "1.3em",
      fontWeight := "normal",
      textTransform := "uppercase",
      color := "#ECF0F1",
      background := "#F1626B",
      cursor := "pointer",
      position := "relative",
      outline := "none",
      top := "0.1em",

      &.hover(
        background := "#EF4551"
      )
    ),

    li(
      &.firstChild(
        &.>(label)(
          borderRadius := "8px 0px 0px 0px"
        )
      ),
      &.lastChild(
        &.>(label)(
          borderRadius := "0px 8px 0px 0px"
        )
      )
    ),

    // .tabs [id^="tab"]:checked + label
    (new Selector(Selector("[id^='tab']").checked.built ++ Seq("+") ++ label.built))(
      top := "0",
      paddingTop := "0.79em",
      background := "#EF4551"
    ),

    // .tabs [id^="tab"]:checked ~ [id^="tab-content"]
    (new Selector(Selector("[id^='tab']").checked.built ++ Seq("~") ++ Selector("[id^='tab-content']").built))(
      visibility := "visible"
    )
  )

  /** To be placed on content element. */
  lazy val tabContent: Cls = cls(
    display := "block",
    zIndex := "2",
    visibility := "hidden",
    overflow := "hidden",

    width := "100%",
    minHeight := "305px",
    maxHeight := "400px",

    fontSize := "1.3em",
    lineHeight := "25px",

    position := "absolute",
    top := "2.7em",
    left := "0em",

    borderRadius := "0px 0px 8px 8px"
  )

  lazy val tabsDark: Cls = cls(
    tabContent(
      background := "#3B3E43",
      color := "#EBF0F1"
    )
  )

  lazy val tabsLight: Cls = cls(
    tabContent(
      background := "#EAEAEC",
      color := "#3B3E43"
    )
  )

  /** To be placed on inner content element. */
  lazy val tabInnerContent: Cls = cls(
    padding := "1em", {
      import FrontPageStyle.textbox
      textbox(
        maxHeight := "300px"
      )
    }
  )
}
