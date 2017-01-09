package org.scaladebugger.docs

import java.nio.file.{Files, Path, Paths}

import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.ResetCommand.ResetType
import org.eclipse.jgit.api._
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a publisher of content based on a configuration.
 *
 * @param config The configuration to use when publishing files
 */
class Publisher(private val config: Config) {
  /** Logger for this class. */
  private lazy val logger = new Logger(this.getClass)

  /** Represents the repository builder. */
  private lazy val builder = new FileRepositoryBuilder

  /**
   * Publishes the content in the output directory.
   */
  def run(): Unit = {
    // TODO: Copy directory to cache, reset hard to remove any changes,
    //       create/switch to the desired branch in the cache, clear the
    //       repository of all content, copy output directory contents to
    //       repository, commit changes, push
    val repoPath = {
      val rootPath = Paths.get(".").toAbsolutePath
      val git = gitForPath(rootPath)
      copyRepoToCache(git.getRepository, force = false)
    }

    logger.info(s"Rebasing repository found at $repoPath")
    val result = {
      val git = gitForPath(repoPath)
      tryRebase(git, config.publishRemoteName(), config.publishRemoteBranch())
    }

    result.foreach(commitId => logger.info(s"Rebased to $commitId"))
    result.failed.foreach(logger.error)
  }

  /**
   * Copies the repository to the cache directory.
   *
   * @param repository The repository to copy
   * @param force If true, will force copying instead of ignoring if already
   *              cached
   * @return The path to the cached directory containing the repository
   */
  private def copyRepoToCache(repository: Repository, force: Boolean): Path = {
    val cacheRootPath = Paths.get(config.publishCacheDir())
    val alreadyExists = Files.exists(cacheRootPath)

    // If for some reason a file exists as our cache root, fail loudly
    if (alreadyExists && !Files.isDirectory(cacheRootPath)) {
      throw new IllegalStateException(s"Publish cache $cacheRootPath is file!")
    }

    // Create the directory if it doesn't exist
    if (!alreadyExists) {
      logger.verbose(s"Creating $cacheRootPath for first time")
      Files.createDirectories(cacheRootPath)
    }

    // If not already exists or being forced, copy contents to cache
    val workTreePath = repository.getWorkTree.toPath.toAbsolutePath.normalize()
    val destinationPath = cacheRootPath.resolve(workTreePath.getFileName)
    logger.info(s"Work Tree file name: ${workTreePath.getFileName}")
    if (!Files.exists(destinationPath) || force) {
      FileUtils.deleteDirectory(destinationPath.toFile)
      Files.createDirectories(destinationPath)

      logger.info(s"Copying $workTreePath to $destinationPath")
      FileUtils.copyDirectory(workTreePath.toFile, destinationPath.toFile)
    } else {
      logger.info(s"$destinationPath already exists, so not copying!")
    }

    destinationPath
  }

  /**
   * Attempts to rebase the repository to a clean state.
   *
   * @param git The git instance whose repository to rebase
   * @param remoteName The name of the remote repo (e.g. origin) whose commit
   *                   revision history to use when rebasing
   * @param branchName The name of the branch to rebase
   * @return Success containing the top commit id after rebasing, or a Failure
   */
  private def tryRebase(
    git: Git,
    remoteName: String = "origin",
    branchName: String = "gh-pages"
  ): Try[String] = {
    val result = Try({
      val repo = git.getRepository
      val repoPath = repo.getWorkTree.toPath.normalize()
      val fullBranchName = s"$remoteName/$branchName"

      // Reset any pending changes in the copy
      logger.info(s"Clearing any changes in $repoPath")
      git.reset()
        .setMode(ResetType.HARD)
        .call()

      // Switch to desired starting point
      logger.info(s"Checking out $fullBranchName")
      git.checkout().setName(fullBranchName).call()

      // Ensure we have the latest from the remote repo
      logger.info(s"Pulling latest from $fullBranchName")
      git.pull()
        .setRemote(remoteName)
        .setRemoteBranchName(branchName)
        .call()

      // Find the top commit of the branch
      val revWalk = new RevWalk(repo)
      val revIterator = revWalk.iterator().asScala
      val commit = if (revIterator.hasNext) Some(revIterator.next()) else None
      commit.map(_.toObjectId.toString).getOrElse(
        throw new IllegalStateException(s"No commit found in $remoteName/$branchName!")
      )
    })

    result
  }

  /**
   * Creates a git object for a local git repo containing the specified path.
   *
   * @param path The path managed by a git repo
   * @return The Git instance
   */
  private def gitForPath(path: Path): Git = {
    val repository = builder.readEnvironment().findGitDir(path.toFile).build()

    new Git(repository)
  }
}
