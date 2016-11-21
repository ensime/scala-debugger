package org.scaladebugger.tool.frontend
import acyclic.file
import org.scaladebugger.tool.frontend.history.HistoryManager

/**
 * Represents the interface for a terminal that can read and write.
 */
trait Terminal {
  /**
   * Reads the next line from the terminal.
   *
   * @return Some line if found, otherwise None if EOF
   */
  def readLine(): Option[String]

  /**
   * Returns the manager used to keep track of this terminal's history.
   *
   * @return
   */
  def history: HistoryManager

  /**
   * Writes the provided text to the terminal.
   *
   * @param text The text to write out to the terminal
   */
  def write(text: String): Unit

  /**
   * Writes the provided text to the terminal and ends with a newline character.
   *
   * @param text The text to write out to the terminal
   */
  def writeLine(text: String): Unit = write(s"$text\n")

  /** Represents the underlying prompt function to generate new prompts. */
  @volatile private var _promptFunc: () => String = () => "> "

  /**
   * Sets a dynamic prompt using a prompt function.
   *
   * @param promptFunc The new prompt function
   */
  def setPromptFunction(promptFunc: () => String): Unit = {
    _promptFunc = promptFunc
  }

  /**
   * Returns the function used to generate prompts.
   *
   * @return The prompt function
   */
  def getPromptFunction: () => String = _promptFunc

  /**
   * Sets a static prompt for the terminal.
   *
   * @param prompt The static prompt string
   */
  def setPrompt(prompt: String): Unit = setPromptFunction(() => prompt)

  /**
   * Retrieves the prompt by invoking the prompt function.
   *
   * @return The new string prompt
   */
  def prompt(): String = _promptFunc()
}
