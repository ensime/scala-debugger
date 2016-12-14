package org.scaladebugger.docs

import org.apache.commons.codec.binary.Base64

/**
 * Loads images from resources.
 */
object ImageLoader {
  private lazy val cl = getClass.getClassLoader

  /**
   * Retrieves an image as a byte array.
   * @param name The name of the image
   * @param rootPath The root path in resources containing the image
   * @return The array of bytes representing the image
   */
  def imageBytes(name: String, rootPath: String = ""): Array[Byte] = {
    val inputStream = cl.getResourceAsStream(rootPath + name)
    Stream.continually(inputStream.read)
      .takeWhile(_ >= 0)
      .map(_.toByte).toArray
  }

  /**
   * Retrieves an image as a base64 encoded string.
   * @param name The name of the image
   * @param rootPath The root path in resources containing the image
   * @return The base64 encoded string representing the image
   */
  def imageBase64(name: String, rootPath: String = ""): String = {
    val byteArray = imageBytes(name, rootPath)
    Base64.encodeBase64String(byteArray)
  }
}
