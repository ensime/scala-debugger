package org.scaladebugger.docs.structures

import java.nio.file.{Files, Path, Paths}

import org.scaladebugger.docs.Config
import org.scaladebugger.docs.utils.FileUtils

/**
 * Represents a generic menu item.
 *
 * @param name The name of the menu item
 * @param link The link for the menu item, or None if the menu item does not
 *             link to any content
 * @param children The children of the menu item
 * @param selected Whether or not the menu item is selected
 */
case class MenuItem(
  name: String,
  link: Option[String] = None,
  children: Seq[MenuItem] = Nil,
  selected: Boolean = false
)

object MenuItem {
  /**
   * Generates a collection of menu items from the given path by searching
   * for markdown files and using them as the basis of children menu items.
   *
   * @param config Used for defaults
   * @param path The path to use as the basis for generating
   *             menu items
   * @param dirUseFirstChild If true, will use the link of the first child
   *                         under the menu item if the menu item's provided
   *                         path is a directory
   * @return The collection of menu items
   */
  def fromPath(
    config: Config,
    path: Path,
    dirUseFirstChild: Boolean = false
  ): Seq[MenuItem] = {
    val mdFiles = FileUtils.markdownFiles(path)

    // Find all directories of src dir
    val directoryPaths = FileUtils.directories(path)

    // All paths excluding top-level index.md
    val allPaths: Seq[Path] = (mdFiles ++ directoryPaths)
      .filterNot(p => path.relativize(p) == Paths.get("index.md")).toSeq

    allPaths
      .filter(_.getParent == path)
      .map(p => createLinkedMenuItem(
        config,
        p,
        allPaths,
        dirUseFirstChild = dirUseFirstChild
      ))
  }

  /**
   * Creates a menu item using the given configuration, series of paths for
   * potential children, and path to be the menu item.
   *
   * @param config Used for defaults
   * @param candidateChildren All paths to consider as children for the new
   *                          menu item
   * @param path The path to serve as the menu item
   * @param dirUseFirstChild If true, will use the link of the first child
   *                         under the menu item if the menu item's provided
   *                         path is a directory
   * @return The new menu item
   */
  private def createLinkedMenuItem(
    config: Config,
    path: Path,
    candidateChildren: Seq[Path],
    dirUseFirstChild: Boolean
  ): MenuItem = {
    val children = candidateChildren.filter(_.getParent == path).map(p =>
      createLinkedMenuItem(config, p, candidateChildren, dirUseFirstChild))

    val page = Page.newInstance(config, path)

    // Directories use first child as link
    val isDir = Files.isDirectory(path)
    val link =
      if (isDir && dirUseFirstChild)
        children.find(_.link.nonEmpty).flatMap(_.link)
      else if (isDir && !dirUseFirstChild)
        None
      else
        Some(page.absoluteLink)

    MenuItem(
      name = page.name,
      link = link,
      children = children
    )
  }
}
