package org.scaladebugger.api.pipelines

import java.io.Closeable

import scala.collection.GenTraversableOnce
import scala.concurrent.{Promise, Future}
import scala.util.Try

/**
 * Represents a pipeline of instructions used to perform a series of operations
 * over an arbitrary collection of data.
 *
 * @tparam A The incoming data type
 * @tparam B The outgoing data type
 * @param operation The operation to apply to incoming data
 * @param closeFunc The function to invoke to close the pipeline
 * @param metadataMap The map of metadata to hold in the pipeline instance
 */
class Pipeline[A, B] private[pipelines] (
  val operation: Operation[A, B],
  private[pipelines] val closeFunc: Pipeline.CloseFunctionWithData,
  private val metadataMap: Pipeline.Metadata
) extends Closeable {
  /**
   * Creates a new instance of the pipeline with the specified operation. The
   * close function is considered a no-op on this pipeline. The metadata is an
   * empty map for this pipeline.
   *
   * @param operation The operation to apply to incoming data
   *
   * @return The new pipeline instance
   */
  private[pipelines] def this(operation: Operation[A, B]) =
    this(operation, Pipeline.DefaultCloseFunc, Pipeline.DefaultMetadataMap)

  /**
   * Creates a new instance of the pipeline with the specified operation and
   * close function. The metadata is an empty map for this pipeline.
   *
   * @param operation The operation to apply to incoming data
   * @param closeFunc The function to invoke to close the pipeline
   *
   * @return The new pipeline instance
   */
  private[pipelines] def this(
    operation: Operation[A, B],
    closeFunc: Pipeline.CloseFunctionWithData
  ) = this(operation, closeFunc, Pipeline.DefaultMetadataMap)

  /**
   * Creates a new instance of the pipeline with the specified operation and
   * close function. The metadata is an empty map for this pipeline.
   *
   * @param operation The operation to apply to incoming data
   * @param closeFunc The function to invoke to close the pipeline
   *
   * @return The new pipeline instance
   */
  private[pipelines] def this(
    operation: Operation[A, B],
    closeFunc: Pipeline.CloseFunction
  ) = this(operation, (_) => closeFunc(), Pipeline.DefaultMetadataMap)

  /**
   * Creates a new instance of the pipeline with the specified operation and
   * metadata. The close function is considered a no-op on this pipeline.
   *
   * @param operation The operation to apply to incoming data
   * @param metadataMap The map of metadata to hold in the pipeline instance
   *
   * @return The new pipeline instance
   */
  private[pipelines] def this(
    operation: Operation[A, B],
    metadataMap: Pipeline.Metadata
  ) = this(operation, Pipeline.DefaultCloseFunc, metadataMap)

  /**
   * Creates a new instance of the pipeline with the specified operation and
   * close function. The metadata is an empty map for this pipeline.
   *
   * @param operation The operation to apply to incoming data
   * @param closeFunc The function to invoke to close the pipeline
   * @param metadataMap The map of metadata to hold in the pipeline instance
   *
   * @return The new pipeline instance
   */
  private[pipelines] def this(
    operation: Operation[A, B],
    closeFunc: Pipeline.CloseFunction,
    metadataMap: Pipeline.Metadata
  ) = this(operation, (_) => closeFunc(), metadataMap)

  /** Any child pipelines whose input is the output of this pipeline. */
  @volatile private var _children: Seq[Pipeline[B, _]] = Nil

  /** Failure route at this stage in the pipeline. */
  private lazy val _failure = newPipeline(new NoOperation[Throwable])

  /** Metadata route at this stage in the pipeline. */
  private lazy val _metadata = map(data => (data, currentMetadata))

  /**
   * Retrieves the metadata route of the current pipeline stage.
   *
   * @return The pipeline to process the metadata for the pipeline, containing
   *         the data and metadata as a tuple
   */
  def metadata: Pipeline[B, (B, Pipeline.Metadata)] = _metadata

  /**
   * Retrieves the current metadata at this stage in the pipeline.
   *
   * @return The map of metadata
   */
  def currentMetadata: Pipeline.Metadata = metadataMap

  /**
   * Adds additional metadata by creating a new stage in the pipeline with the
   * additional metadata merged with the existing metadata.
   *
   * @param metadataMap The new metadata to add to future pipeline stages
   *
   * @return The resulting pipeline instance with the updated metadata
   */
  def withMetadata(metadataMap: Pipeline.Metadata): Pipeline[B, B] = {
    val childPipeline = newPipeline(
      new NoOperation[B],
      closeFunc,
      currentMetadata ++ metadataMap
    )

    addChildPipeline(childPipeline)
  }

  /**
   * Retrieves the failure route of the current pipeline stage.
   *
   * @return The pipeline to process the throwable from the failure
   */
  def failed: Pipeline[Throwable, Throwable] = _failure

  /**
   * Retrieves the collection of children pipelines for the current pipeline.
   *
   * @return The collection of pipelines that are children to this pipeline
   */
  def children: Seq[Pipeline[B, _]] = _children

  /**
   * Adds the specified pipeline as a child of this pipeline.
   *
   * @param childPipeline The pipeline to add as a child
   * @tparam C The outgoing type of the child pipeline
   *
   * @return The added child pipeline
   */
  protected def addChildPipeline[C](
    childPipeline: Pipeline[B, C]
  ): Pipeline[B, C] = {
    _children :+= childPipeline
    childPipeline
  }

  /**
   * Creates a new pipeline using the given operation. This is used to generate
   * pipelines within the pipeline itself and can be overridden to generate
   * a different kind of pipeline. It is recommended to override this method
   * when subclassing pipeline.
   *
   * @param operation The operation to provide to the new pipeline
   * @param closeFunc The function used to close the new pipeline
   * @param metadataMap The map of metadata to hold in the pipeline instance
   * @tparam C The input type of the new pipeline
   * @tparam D The output type of the new pipeline
   *
   * @return The new pipeline
   */
  protected def newPipeline[C, D](
    operation: Operation[C, D],
    closeFunc: Pipeline.CloseFunctionWithData,
    metadataMap: Pipeline.Metadata
  ): Pipeline[C, D] = new Pipeline[C, D](operation, closeFunc, metadataMap)

  /**
   * Creates a new pipeline using the given operation. This is used to generate
   * pipelines within the pipeline itself and can be overridden to generate
   * a different kind of pipeline. It is recommended to override this method
   * when subclassing pipeline.
   *
   * @note This implementation passes down the current pipeline's
   *       close function and metadata map.
   *
   * @param operation The operation to provide to the new pipeline
   * @tparam C The input type of the new pipeline
   * @tparam D The output type of the new pipeline
   *
   * @return The new pipeline
   */
  protected def newPipeline[C, D](operation: Operation[C, D]): Pipeline[C, D] =
    newPipeline[C, D](operation, closeFunc, metadataMap)

  /**
   * Processes the provided data through this specific pipeline instance and
   * all subsequent children of this pipeline instance. No parent pipeline
   * instance will be used during the processing of the data.
   *
   * @param data The data to process
   *
   * @return If successful, the transformed collection of data at this
   *         specific pipeline instance, otherwise the thrown exception
   */
  def process(data: A*): Try[Seq[B]] = {
    val results = Try(operation.process(data))

    // If successful, continue down children paths
    results.foreach(r => children.foreach(_.process(r: _*)))

    // If failed, continue down failure path
    results.failed.foreach(throwable => failed.process(throwable))

    results
  }

  /**
   * Transforms the output of this pipeline using the provided operation.
   *
   * @note Inherits the close function of the pipeline.
   *
   * @param operation The operation to use to transform the output of this
   *                  pipeline instance
   * @tparam C The resulting type of the output from the operation
   *
   * @return The resulting pipeline instance from applying the operation
   */
  def transform[C](operation: Operation[B, C]): Pipeline[B, C] = {
    val childPipeline = newPipeline(operation)
    addChildPipeline(childPipeline)
  }

  /**
   * Maps the output of this pipeline instance to new values.
   *
   * @note Inherits the close function of the pipeline.
   *
   * @param f The function to use for mapping data to new values
   * @tparam C The resulting type of the new values
   *
   * @return The resulting pipeline instance with the mapped data
   */
  def map[C](f: (B) => C): Pipeline[B, C] = {
    transform(new MapOperation(f))
  }

  /**
   * Maps the output of this pipeline instance to new values and then flattens
   * the results.
   *
   * @param f The function to use for mapping data to new values that will
   *          be flattened
   * @tparam C The resulting type of the new values
   *
   * @return The resulting pipeline instance with the mapped and flattened data
   */
  def flatMap[C](f: (B) => GenTraversableOnce[C]): Pipeline[B, C] = {
    transform(new FlatMapOperation(f))
  }

  /**
   * Filters the output of this pipeline instance.
   *
   * @param f The function to use for filtering data (only true results will
   *          remain in output)
   *
   * @return The resulting pipeline instance with the filtered data
   */
  def filter(f: (B) => Boolean): Pipeline[B, B] = {
    transform(new FilterOperation(f))
  }

  /**
   * Filters the output of this pipeline instance.
   *
   * @param f The function to use for filtering data (only false results will
   *          remain in output)
   *
   * @return The resulting pipeline instance with the filtered data
   */
  def filterNot(f: (B) => Boolean): Pipeline[B, B] = {
    transform(new FilterNotOperation(f))
  }

  /**
   * Applies the provided function to the output of this pipeline, returning
   * nothing from the function.
   *
   * @param f The function to apply
   */
  def foreach(f: (B) => Unit): Unit = {
    transform(new ForeachOperation(f))
  }

  /**
   * Unions this pipeline with another pipeline that has the same input such
   * that input from either pipeline is used for both.
   *
   * @param other The other pipeline whose input to union together
   *
   * @return The unioned pipeline
   */
  def unionInput(other: Pipeline[A, _]): Pipeline[A, A] = {
    val parentPipeline = newPipeline(
      new NoOperation[A],
      Pipeline.DefaultCloseFunc,
      Pipeline.DefaultMetadataMap
    )

    parentPipeline.addChildPipeline(this)
    parentPipeline.addChildPipeline(other)

    parentPipeline
  }

  /**
   * Unions this pipeline with another pipeline that has the same output such
   * that output from either pipeline flows through the union.
   *
   * @param other The other pipeline to whose output to union together
   *
   * @return The unioned pipeline
   */
  def unionOutput(other: Pipeline[_, B]): Pipeline[B, B] = {
    val childPipeline = newPipeline(new NoOperation[B], (data) => {
      val result1 = Try(this.close(now = true, data = data.orNull))
      val result2 = Try(other.close(now = true, data = data.orNull))

      // Throw the exceptions if they occur AFTER processing both closes
      result1.get
      result2.get
    }, this.currentMetadata ++ other.currentMetadata)

    this.addChildPipeline(childPipeline)
    other.addChildPipeline(childPipeline)

    childPipeline
  }

  /**
   * Applies a no-op on the current pipeline.
   *
   * @return The pipeline after a no-op has been applied
   */
  def noop(): Pipeline[B, B] = transform(new NoOperation[B])

  /**
   * Closes the pipeline.
   *
   * @param now If true, should perform the closing action immediately rather
   *            than on the next data fed through the pipeline
   * @param data Any data to be provided to the close function
   */
  def close(now: Boolean = true, data: Any = null): Unit = {
    if (now) closeFunc(Option(data))
    else transform(new CloseOperation(() => closeFunc(Option(data))))
  }

  /**
   * Closes the pipeline immediately. No data is provided.
   */
  def close(): Unit = close(now = true, data = null)

  /**
   * Transforms the pipeline into a future that will be evaluated once and then
   * closes the underlying pipeline.
   *
   * @return The future representing this pipeline
   */
  def toFuture: Future[B] = {
    val pipelinePromise = Promise[B]()

    foreach(value => {
      pipelinePromise.success(value)
      close()
    })

    failed.foreach(throwable => {
      pipelinePromise.failure(throwable)
      close()
    })

    pipelinePromise.future
  }
}

/**
 * Contains helper utilities for pipeline creation.
 */
object Pipeline {
  /** Represents a pipeline whose input and output types are the same. */
  type IdentityPipeline[A] = Pipeline[A, A]

  /** Represents the metadata for a pipeline. */
  type Metadata = Map[String, Any]

  /** Represents a close function with data. */
  type CloseFunctionWithData = (Option[Any]) => Unit

  /** Represents a close function with no data. */
  type CloseFunction = () => Unit

  /** Default close function. Does nothing. */
  val DefaultCloseFunc: CloseFunctionWithData = (_) => {}

  /** Default metadata map. Is empty. */
  val DefaultMetadataMap: Metadata = Map()

  /**
   * Creates an empty pipeline expecting data of the specified type. The
   * associated close function is equivalent to a no-op.
   *
   * @tparam A The type of incoming data
   * @param klass The class representing the input of the new pipeline
   *
   * @return The new pipeline
   */
  def newPipeline[A](klass: Class[A]): IdentityPipeline[A] =
    new Pipeline(new NoOperation[A])

  /**
   * Creates an empty, closeable pipeline expecting data of the specified type.
   *
   * @tparam A The type of incoming data
   * @param klass The class representing the input of the new pipeline
   * @param closeFunc The function to invoke when closing the pipeline
   *
   * @return The new pipeline
   */
  def newPipeline[A](
    klass: Class[A],
    closeFunc: CloseFunction
  ): Pipeline[A, A] = new Pipeline(new NoOperation[A], closeFunc)

  /**
   * Creates an empty, closeable pipeline expecting data of the specified type.
   *
   * @tparam A The type of incoming data
   * @param klass The class representing the input of the new pipeline
   * @param closeFunc The function to invoke when closing the pipeline
   *
   * @return The new pipeline
   */
  def newPipeline[A](
    klass: Class[A],
    closeFunc: CloseFunctionWithData
  ): Pipeline[A, A] = new Pipeline(new NoOperation[A], closeFunc)

  /**
   * Creates an empty pipeline expecting data of the specified type and
   * containing the provided metadata. The associated close function is
   * equivalent to a no-op.
   *
   * @tparam A The type of incoming data
   * @param klass The class representing the input of the new pipeline
   * @param metadataMap The map of metadata to hold in the pipeline instance
   *
   * @return The new pipeline instance
   */
  def newPipeline[A](
    klass: Class[A],
    metadataMap: Pipeline.Metadata
  ): Pipeline[A, A] = new Pipeline(new NoOperation[A], metadataMap)

  /**
   * Creates an empty pipeline expecting data of the specified type and
   * containing the provided metadata.
   *
   * @tparam A The type of incoming data
   * @param klass The class representing the input of the new pipeline
   * @param closeFunc The function to invoke when closing the pipeline
   * @param metadataMap The map of metadata to hold in the pipeline instance
   *
   * @return The new pipeline instance
   */
  def newPipeline[A](
    klass: Class[A],
    closeFunc: CloseFunction,
    metadataMap: Pipeline.Metadata
  ): Pipeline[A, A] = new Pipeline(new NoOperation[A], closeFunc, metadataMap)

  /**
   * Creates an empty pipeline expecting data of the specified type and
   * containing the provided metadata.
   *
   * @tparam A The type of incoming data
   * @param klass The class representing the input of the new pipeline
   * @param closeFunc The function to invoke when closing the pipeline
   * @param metadataMap The map of metadata to hold in the pipeline instance
   *
   * @return The new pipeline instance
   */
  def newPipeline[A](
    klass: Class[A],
    closeFunc: CloseFunctionWithData,
    metadataMap: Pipeline.Metadata
  ): Pipeline[A, A] = new Pipeline(new NoOperation[A], closeFunc, metadataMap)
}
