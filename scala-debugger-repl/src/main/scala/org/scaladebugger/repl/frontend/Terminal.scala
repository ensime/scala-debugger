package org.scaladebugger.repl.frontend
import acyclic.file

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
