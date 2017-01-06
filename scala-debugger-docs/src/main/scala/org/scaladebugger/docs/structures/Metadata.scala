package org.scaladebugger.docs.structures

/**
 * Represents metadata for a page.
 *
 * @param layout The fully-qualified class name for the layout to use
 * @param other All other metadata properties that were provided that
 *              do not match reserved properties
 */
case class Metadata(
  layout: Option[String],
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
   * @param data The data to parse
   * @return The new metadata instance
   */
  def fromMap(data: Map[String, Seq[String]]): Metadata = {
    // Only support a single layout and ignore the rest
    val layout = data.get("layout").flatMap(_.headOption)

    Metadata(
      layout = layout,
      other = data.filterKeys(k => !reservedKeys.contains(k))
    )
  }

  /**
   * Converts a Java map of keys and associated values into a
   * metadata construct.
   *
   * @param data The data to parse
   * @return The new metadata instance
   */
  def fromJavaMap(
    data: java.util.Map[String, java.util.List[String]]
  ): Metadata = {
    import scala.collection.JavaConverters._

    val scalaData = data.asScala.mapValues(_.asScala.toSeq).toMap

    fromMap(scalaData)
  }
}
