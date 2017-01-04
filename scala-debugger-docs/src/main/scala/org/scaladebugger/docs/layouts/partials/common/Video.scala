package org.scaladebugger.docs.layouts.partials.common

import org.scaladebugger.docs.styles.PageStyle

import scalatags.Text.all._

/**
 * Generates a lined content structure.
 */
object Video {
  def apply(rootPath: String, fileName: String): Modifier = {
    val vidUrl = (ext: String) => buildUrl(rootPath, fileName, ext)

    video(
      PageStyle.videoCls,
      attr("poster") := vidUrl("jpg"),
      attr("preload") := "none",
      attr("controls") := "true",
      attr("loop") := "true",
      attr("muted") := "true"
    )(
      source(src := vidUrl("webm"), `type` := "video/webm"),
      source(src := vidUrl("mp4"), `type` := "video/mp4"),
      source(src := vidUrl("ogv"), `type` := "video/ogv"),
      span("Your browser doesn't support HTML video tag.")
    )
  }

  private def buildUrl(rootPath: String, fileName: String, ext: String): String =
    s"$rootPath/$fileName.$ext"
}
