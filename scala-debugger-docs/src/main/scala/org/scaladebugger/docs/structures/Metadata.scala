package org.scaladebugger.docs.structures

import org.scaladebugger.docs.Config

import scala.util.Try

/**
 * Represents metadata for a page.
 *
 * @param layout The fully-qualified class name for the layout to use
 * @param usingDefaultLayout If true, indicates that the page is using the
 *                           default layout
 * @param weight A weight used for page ordering in menus and other structures
 * @param render Whether or not to render the page
 * @param other All other metadata properties that were provided that
 *              do not match reserved properties
 */
case class Metadata(
  layout: String,
  usingDefaultLayout: Boolean,
  weight: Int,
  render: Boolean,
  other: Map[String, Seq[String]]
)

object Metadata {
  /**
   * Represents the property names reserved for use in the metadata.
   */
  lazy val reservedKeys: Seq[String] =
    classOf[Metadata].getDeclaredFields.map(_.getName).filterNot(_ == "other")

  /**
   * Converts a map of keys and associated values into a metadata construct.
   *
   * @param config The global configuration used to fill in defaults
   * @param data The data to parse
   * @return The new metadata instance
   */
  def fromMap(config: Config, data: Map[String, Seq[String]]): Metadata = {
    val layout = data.get("layout").flatMap(_.headOption)
    val weight = data.get("weight").flatMap(_.headOption)
      .flatMap(w => Try(w.toInt).toOption)
    val render = data.get("render").flatMap(_.headOption)
      .flatMap(r => Try(r.toBoolean).toOption)

    Metadata(
      layout = layout.getOrElse(config.defaultPageLayout()),
      usingDefaultLayout = layout.isEmpty,
      weight = weight.getOrElse(config.defaultPageWeight()),
      render = render.getOrElse(config.defaultPageRender()),
      other = data.filterKeys(k => !reservedKeys.contains(k))
    )
  }

  /**
   * Converts a Java map of keys and associated values into a
   * metadata construct.
   *
   * @param config The global configuration used to fill in defaults
   * @param data The data to parse
   * @return The new metadata instance
   */
  def fromJavaMap(
    config: Config,
    data: java.util.Map[String, java.util.List[String]]
  ): Metadata = {
    import scala.collection.JavaConverters._

    val scalaData = data.asScala.mapValues(_.asScala.toSeq).toMap

    fromMap(config, scalaData)
  }
}
