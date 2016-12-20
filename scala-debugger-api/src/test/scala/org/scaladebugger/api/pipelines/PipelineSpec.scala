package org.scaladebugger.api.pipelines

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

import scala.collection.GenTraversableOnce
import scala.reflect.ClassTag
import scala.util.{Failure, Success}

class PipelineSpec extends FunSpec with Matchers with ParallelTestExecution
  with MockFactory
{
  describe("Pipeline") {
    describe("#process") {
      it("should perform the pipeline's operation on the provided data") {
        val mockOperation = mock[Operation[Int, Int]]
        val pipeline = new Pipeline(mockOperation)
        val data = Seq(1, 2, 3)

        (mockOperation.process _).expects(data).once()

        pipeline.process(data: _*)
      }

      it("should recursively call children pipelines based on the process results") {
        val mockOperation = mock[Operation[Int, Int]]
        val mockChildOperation = mock[Operation[Int, Int]]
        val pipeline = new Pipeline(mockOperation)

        val data = Seq(1, 2, 3)
        val result = Seq(7, 8, 9, 10)

        (mockOperation.process _).expects(data).returning(result).once()
        (mockChildOperation.process _).expects(result).once()

        // Add a child pipeline
        pipeline.transform(mockChildOperation)

        pipeline.process(data: _*)
      }

      it("should return the transformed data at this point in the pipeline") {
        val expected = Success(Seq(1, 2, 3))

        val mockOperation = mock[Operation[Int, Int]]
        val pipeline = new Pipeline(mockOperation)
        val data = expected.get.map(_ - 1)

        (mockOperation.process _).expects(data).returning(expected.get).once()

        val actual = pipeline.process(data: _*)
        actual should be (expected)
      }

      it("should recursively call the failed pipeline if the process fails") {
        val throwable = new Throwable
        val expected = Failure(throwable)

        val mockFailedOperation = mock[Operation[Throwable, Throwable]]
        val failedPipeline = new Pipeline(mockFailedOperation)

        val mockOperation = mock[Operation[Int, Int]]
        val pipeline = new Pipeline(mockOperation) {
          override def failed: Pipeline[Throwable, Throwable] = failedPipeline
        }

        val data = Seq(1, 2, 3)

        (mockOperation.process _).expects(data).throwing(throwable).once()
        (mockFailedOperation.process _).expects(Seq(throwable)).once()

        val actual = pipeline.process(data: _*)
        actual should be (expected)
      }

      it("should call the metadata pipeline if it has been added") {
        val data = Seq(1, 2, 3)
        val metadata = Map("a" -> 3)

        val expected = data.map(d => (d, metadata))
        val pipeline = new Pipeline(new NoOperation[Int], metadata)

        var actual: Seq[(Int, Pipeline.Metadata)] = Nil

        pipeline.metadata.foreach(actual :+= _)
        pipeline.process(data: _*)

        actual should be (expected)
      }
    }

    describe("#withMetadata") {
      it("should combine the existing metadata with the new metadata") {
        val p1Metadata = Map("a" -> 3, "b" -> 4)
        val p2Metadata = Map("b" -> 999, "c" -> 5)
        val expected = p1Metadata ++ p2Metadata

        val pipeline = new Pipeline(mock[Operation[Int, Int]], p1Metadata)
        val childPipeline = pipeline.withMetadata(p2Metadata)

        val actual = childPipeline.currentMetadata
        actual should be (expected)
      }

      it("should add a new child pipeline with no operation") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])
        val childPipeline = pipeline.withMetadata(Map("a" -> 3))

        pipeline.children should contain (childPipeline)
      }
    }

    describe("#metadata") {
      it("should add a new child pipeline with a map operation") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        pipeline.metadata

        pipeline.children.head.operation shouldBe a [MapOperation[_, _]]
      }

      it("should only add a metadata child pipeline once") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        pipeline.metadata
        pipeline.metadata

        pipeline.children should have length (1)
      }

      it("should have the same metadata as its parent") {
        val expected = Map("a" -> 3)
        val pipeline = new Pipeline(mock[Operation[Int, Int]], expected)

        val childPipeline = pipeline.metadata

        val actual = childPipeline.currentMetadata
        actual should be (expected)
      }
    }

    describe("#failed") {
      it("should have the same metadata as its parent") {
        val expected = Map("a" -> 3)
        val pipeline = new Pipeline(mock[Operation[Int, Int]], expected)

        val childPipeline = pipeline.failed

        val actual = childPipeline.currentMetadata
        actual should be (expected)
      }
    }

    describe("#currentMetadata") {
      it("should return the map of metadata for the current pipeline stage") {
        val expected = Map("something" -> 3)

        val pipeline = new Pipeline(mock[Operation[Int, Int]], expected)

        val actual = pipeline.currentMetadata
        actual should be (expected)
      }
    }

    describe("#transform") {
      it("should create a new child pipeline using the provided operation") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val mockOperation = mock[Operation[Int, Int]]
        val childPipeline = pipeline.transform(mockOperation)

        childPipeline.operation should be (mockOperation)
      }

      it("should add the created child pipeline to the list of children") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val mockOperation = mock[Operation[Int, Int]]
        val childPipeline = pipeline.transform(mockOperation)

        pipeline.children should contain (childPipeline)
      }

      it("should have the same metadata as its parent") {
        val expected = Map("a" -> 3)
        val pipeline = new Pipeline(mock[Operation[Int, Int]], expected)

        val childPipeline = pipeline.transform(mock[Operation[Int, Int]])

        val actual = childPipeline.currentMetadata
        actual should be (expected)
      }
    }

    describe("#map") {
      it("should create a new child pipeline using the map operation") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.map(mockFunction[Int, Int])

        childPipeline.operation shouldBe a [MapOperation[_, _]]
      }

      it("should add the created child pipeline to the list of children") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.map(mockFunction[Int, Int])

        pipeline.children should contain (childPipeline)
      }

      it("should have the same metadata as its parent") {
        val expected = Map("a" -> 3)
        val pipeline = new Pipeline(mock[Operation[Int, Int]], expected)

        val childPipeline = pipeline.map(mockFunction[Int, Int])

        val actual = childPipeline.currentMetadata
        actual should be (expected)
      }
    }

    describe("#flatMap") {
      it("should create a new child pipeline using the flatMap operation") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.flatMap(
          mockFunction[Int, GenTraversableOnce[Int]]
        )

        childPipeline.operation shouldBe a [FlatMapOperation[_, _]]
      }

      it("should add the created child pipeline to the list of children") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.flatMap(
          mockFunction[Int, GenTraversableOnce[Int]]
        )

        pipeline.children should contain (childPipeline)
      }

      it("should have the same metadata as its parent") {
        val expected = Map("a" -> 3)
        val pipeline = new Pipeline(mock[Operation[Int, Int]], expected)

        val childPipeline = pipeline.flatMap(
          mockFunction[Int, GenTraversableOnce[Int]]
        )

        val actual = childPipeline.currentMetadata
        actual should be (expected)
      }
    }

    describe("#filter") {
      it("should create a new child pipeline using the filter operation") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.filter(mockFunction[Int, Boolean])

        childPipeline.operation shouldBe a [FilterOperation[_]]
      }

      it("should add the created child pipeline to the list of children") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.filter(mockFunction[Int, Boolean])

        pipeline.children should contain (childPipeline)
      }

      it("should have the same metadata as its parent") {
        val expected = Map("a" -> 3)
        val pipeline = new Pipeline(mock[Operation[Int, Int]], expected)

        val childPipeline = pipeline.filter(mockFunction[Int, Boolean])

        val actual = childPipeline.currentMetadata
        actual should be (expected)
      }
    }

    describe("#filterNot") {
      it("should create a new child pipeline using the filterNot operation") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.filterNot(mockFunction[Int, Boolean])

        childPipeline.operation shouldBe a [FilterNotOperation[_]]
      }

      it("should add the created child pipeline to the list of children") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.filterNot(mockFunction[Int, Boolean])

        pipeline.children should contain (childPipeline)
      }

      it("should have the same metadata as its parent") {
        val expected = Map("a" -> 3)
        val pipeline = new Pipeline(mock[Operation[Int, Int]], expected)

        val childPipeline = pipeline.filterNot(mockFunction[Int, Boolean])

        val actual = childPipeline.currentMetadata
        actual should be (expected)
      }
    }

    describe("#foreach") {
      it("should create a new child pipeline using the foreach operation") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        pipeline.foreach(mockFunction[Int, Unit])

        pipeline.children.head.operation shouldBe a [ForeachOperation[_]]
      }

      it("should add the created child pipeline to the list of children") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        pipeline.foreach(mockFunction[Int, Unit])

        pipeline.children.head.operation shouldBe a [ForeachOperation[_]]
      }
    }

    describe("#unionInput") {
      it("should union with another pipeline such that processing the union processes both pipelines") {
        val pipeline1 = new Pipeline(mock[Operation[Int, Int]])
        val pipeline2 = new Pipeline(mock[Operation[Int, Int]])

        val unionPipeline = pipeline1.unionInput(pipeline2)
        val data = Seq(1, 2, 3)

        // NOTE: Checking operations of pipelines as it is easier to test
        (pipeline1.operation.process _).expects(data).once()
        (pipeline2.operation.process _).expects(data).once()

        unionPipeline.process(data: _*)
      }

      it("should create a pipeline whose close invokes neither unioned pipelines' close methods") {
        val mockCloseFunc1 = mockFunction[Unit]
        val pipeline1 = new Pipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc1
        )

        val mockCloseFunc2 = mockFunction[Unit]
        val pipeline2 = new Pipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc2
        )

        val unionInputPipeline = pipeline1.unionInput(pipeline2)

        mockCloseFunc1.expects().never()
        mockCloseFunc2.expects().never()

        unionInputPipeline.close(now = true)
      }

      it("should create a pipeline with the default metadata") {
        val expected = Pipeline.DefaultMetadataMap

        val pipeline1 = new Pipeline(mock[Operation[Int, Int]], Map("a" -> 3))
        val pipeline2 = new Pipeline(mock[Operation[Int, Int]], Map("b" -> 4))

        val unionInputPipeline = pipeline1.unionInput(pipeline2)

        val actual = unionInputPipeline.currentMetadata
        actual should be (expected)
      }
    }

    describe("#unionOutput") {
      it("should union with another pipeline such that processing the either pipeline processes the union") {
        val mockOperation1 = mock[Operation[Int, Int]]
        val pipeline1 = new Pipeline(mockOperation1)

        val mockOperation2 = mock[Operation[Int, Int]]
        val pipeline2 = new Pipeline(mockOperation2)

        val unionPipeline = pipeline1.unionOutput(pipeline2)
        val data = Seq(1, 2, 3)

        // NOTE: Adding mock operation as easier to test
        val mockAfterUnionOperation = mock[Operation[Int, Int]]
        unionPipeline.transform(mockAfterUnionOperation)

        (mockOperation1.process _).expects(data).returning(data).once()
        (mockOperation2.process _).expects(data).returning(data).once()
        (mockAfterUnionOperation.process _).expects(data).twice()

        pipeline1.process(data: _*)
        pipeline2.process(data: _*)
      }

      it("should create a pipeline whose close invokes both unioned pipelines' close methods") {
        val mockCloseFunc1 = mockFunction[Unit]
        val pipeline1 = new Pipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc1
        )

        val mockCloseFunc2 = mockFunction[Unit]
        val pipeline2 = new Pipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc2
        )

        val unionOutputPipeline = pipeline1.unionOutput(pipeline2)

        mockCloseFunc1.expects().once()
        mockCloseFunc2.expects().once()

        unionOutputPipeline.close(now = true)
      }

      it("should create a pipeline with metadata from combining the two pipelines' metadata") {
        val p1Metadata = Map("a" -> 3, "b" -> 4)
        val p2Metadata = Map("b" -> 999, "c" -> 5)
        val expected = p1Metadata ++ p2Metadata

        val pipeline1 = new Pipeline(mock[Operation[Int, Int]], p1Metadata)
        val pipeline2 = new Pipeline(mock[Operation[Int, Int]], p2Metadata)

        val unionOutputPipeline = pipeline1.unionOutput(pipeline2)

        val actual = unionOutputPipeline.currentMetadata
        actual should be (expected)
      }
    }

    describe("#close") {
      it("should invoke the close function immediately if 'now' is true") {
        val mockCloseFunc = mockFunction[Unit]
        val pipeline = new Pipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc
        )

        mockCloseFunc.expects().once()

        pipeline.close(now = true)
      }

      it("should generate a new pipeline containing the close operation if 'now' is false") {
        val mockCloseFunc = mockFunction[Unit]
        val pipeline = new Pipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc
        )

        mockCloseFunc.expects().never()

        pipeline.close(now = false)

        pipeline.children.head shouldBe a [Pipeline[_, _]]
      }

      it("should invoke the close function immediately if no flag given") {
        val mockCloseFunc = mockFunction[Unit]
        val pipeline = new Pipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc
        )

        mockCloseFunc.expects().once()

        pipeline.close()
      }

      it("should invoke the close function immediately if only data is given") {
        val mockCloseFunc = mockFunction[Unit]
        val pipeline = new Pipeline(
          mock[Operation[Int, Int]],
          mockCloseFunc
        )

        mockCloseFunc.expects().once()

        pipeline.close(data = new Object)
      }

      it("should pass the provided data to the close function when close invoked on event") {
        val expected = Some(new Object)
        val mockCloseFuncWithData = mockFunction[Option[Any], Unit]
        val pipeline = new Pipeline(
          stub[Operation[Int, Int]],
          mockCloseFuncWithData
        )

        pipeline.close(now = false, data = expected.get)

        mockCloseFuncWithData.expects(expected).once()

        pipeline.process(999)
      }

      it("should pass the provided data to the close function when close invoked immediately") {
        val expected = Some(new Object)
        val mockCloseFuncWithData = mockFunction[Option[Any], Unit]
        val pipeline = new Pipeline(
          mock[Operation[Int, Int]],
          mockCloseFuncWithData
        )

        mockCloseFuncWithData.expects(expected).once()

        pipeline.close(now = true, data = expected.get)
      }

      it("should pass None to the close function when close invoked without data") {
        val expected: Option[Any] = None

        val mockCloseFuncWithData = mockFunction[Option[Any], Unit]
        val pipeline = new Pipeline(
          mock[Operation[Int, Int]],
          mockCloseFuncWithData
        )

        mockCloseFuncWithData.expects(expected).once()

        pipeline.close()
      }
    }

    describe("#toFuture") {
      it("should return a future that evaluates the next time the pipeline is evaluated") {
        val mockOperation = mock[Operation[Int, Int]]
        val pipeline = new Pipeline(mockOperation)

        val pipelineFuture = pipeline.toFuture

        (mockOperation.process _).expects(Seq(1, 2, 3))
          .returning(Seq(1, 2, 3)).once()

        pipeline.process(1, 2, 3)
        pipelineFuture.value.get.isSuccess should be (true)
      }

      it("should return a future that closes the pipeline upon success") {
        val mockOperation = mock[Operation[Int, Int]]
        val mockCloseFunc = mockFunction[Unit]
        val pipeline = new Pipeline(mockOperation, mockCloseFunc)

        val pipelineFuture = pipeline.toFuture

        (mockOperation.process _).expects(Seq(1, 2, 3))
          .returning(Seq(1, 2, 3)).once()
        mockCloseFunc.expects().once()

        pipeline.process(1, 2, 3)
        pipelineFuture.value.get.isSuccess should be (true)
      }

      it("should return a future that evaluates to a failure when the pipeline fails") {
        val mockOperation = mock[Operation[Int, Int]]
        val pipeline = new Pipeline(mockOperation)

        val pipelineFuture = pipeline.toFuture

        (mockOperation.process _).expects(Seq(1, 2, 3))
          .throwing(new Throwable).once()

        pipeline.process(1, 2, 3)
        pipelineFuture.value.get.isFailure should be (true)
      }

      it("should return a future that closes the pipeline upon failure") {
        val mockOperation = mock[Operation[Int, Int]]
        val mockCloseFunc = mockFunction[Unit]
        val pipeline = new Pipeline(mockOperation, mockCloseFunc)

        val pipelineFuture = pipeline.toFuture

        (mockOperation.process _).expects(Seq(1, 2, 3))
          .throwing(new Throwable).once()
        mockCloseFunc.expects().once()

        pipeline.process(1, 2, 3)
        pipelineFuture.value.get.isFailure should be (true)
      }
    }

    describe("#toIterable") {
      it("should return an iterable containing data as it funnels through the pipeline") {
        val mockOperation = mock[Operation[Int, Int]]
        val pipeline = new Pipeline(mockOperation)

        (mockOperation.process _).expects(Seq(1, 2, 3))
          .returning(Seq(1, 2, 3)).once()

        val iterable = pipeline.toIterable

        pipeline.process(1, 2, 3)

        iterable.toSeq should contain theSameElementsInOrderAs Seq(1, 2, 3)
      }

      it("should not containing any data prior to first invoking the method") {
        val mockOperation = mock[Operation[Int, Int]]
        val pipeline = new Pipeline(mockOperation)

        (mockOperation.process _).expects(Seq(1)).returning(Seq(1)).once()
        (mockOperation.process _).expects(Seq(2, 3)).returning(Seq(2, 3)).once()

        pipeline.process(1)

        val iterable = pipeline.toIterable

        pipeline.process(2, 3)

        iterable.toSeq should contain theSameElementsInOrderAs Seq(2, 3)
      }

      it("should not contain any new data after the pipeline is closed") {
        val mockOperation = mock[Operation[Int, Int]]
        val pipeline = new Pipeline(mockOperation)

        (mockOperation.process _).expects(Seq(1, 2, 3))
          .returning(Seq(1, 2, 3)).once()

        val iterable = pipeline.toIterable

        pipeline.process(1, 2, 3)
        pipeline.close()
        pipeline.process(4, 5, 6)

        iterable.toSeq should contain theSameElementsInOrderAs Seq(1, 2, 3)
      }
    }

    describe("#noop") {
      it("should create a new child pipeline using a no-op") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.noop()

        childPipeline.operation shouldBe a [NoOperation[_]]
      }

      it("should add the created child pipeline to the list of children") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        val childPipeline = pipeline.noop()

        pipeline.children should contain (childPipeline)
      }

      it("should have the same metadata as its parent") {
        val expected = Map("a" -> 3)
        val pipeline = new Pipeline(mock[Operation[Int, Int]], expected)

        val childPipeline = pipeline.noop()

        val actual = childPipeline.currentMetadata
        actual should be (expected)
      }
    }

    describe("#children") {
      it("should be empty when the pipeline is first created") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        pipeline.children should be (empty)
      }

      it("should return the current children contained by the pipeline") {
        val pipeline = new Pipeline(mock[Operation[Int, Int]])

        pipeline.transform(mock[Operation[Int, Int]])

        pipeline.children should have length (1)
      }
    }

    describe("#newPipeline") {
      it("should create a new pipeline with a no-op based on the class type") {
        import scala.language.existentials

        def getErasure[A, B](
          operation: Operation[A, B]
        )(implicit aClassTag: ClassTag[A], bClassTag: ClassTag[B]) = {
          (aClassTag.runtimeClass, bClassTag.runtimeClass)
        }

        val operation = Pipeline.newPipeline(classOf[AnyRef]).operation

        val (inputClass, outputClass) = getErasure(operation)

        operation shouldBe a [NoOperation[_]]
        inputClass should be (classOf[AnyRef])
        outputClass should be (classOf[AnyRef])
      }

      it("should create a pipeline with the default close function when only the class type is provided") {
        val expected = Pipeline.DefaultCloseFunc

        val pipeline = Pipeline.newPipeline(classOf[AnyRef])

        val actual = pipeline.closeFunc
        actual should be (expected)
      }

      it("should create a pipeline with the default metadata map when only the class type is provided") {
        val expected = Pipeline.DefaultMetadataMap

        val pipeline = Pipeline.newPipeline(classOf[AnyRef])

        val actual = pipeline.currentMetadata
        actual should be (expected)
      }

      it("should create a new pipeline with the specified close function") {
        val mockCloseFunc = mockFunction[Unit]
        val pipeline = Pipeline.newPipeline(classOf[AnyRef], mockCloseFunc)

        mockCloseFunc.expects().once()

        pipeline.close()
      }

      it("should create a new pipeline with the specified close function with data") {
        val expected: Option[Any] = Some(new Object)
        val mockCloseFunc = mockFunction[Option[Any], Unit]
        val pipeline = Pipeline.newPipeline(classOf[AnyRef], mockCloseFunc)

        mockCloseFunc.expects(expected).once()

        pipeline.close(now = true, data = expected.get)
      }

      it("should create a new pipeline with default metadata when only the class type and close function are provided") {
        val expected = Pipeline.DefaultMetadataMap

        val pipeline = Pipeline.newPipeline(classOf[AnyRef], mockFunction[Unit])

        val actual = pipeline.currentMetadata
        actual should be (expected)
      }

      it("should create a new pipeline with the specified metadata map") {
        val expected = Map("a" -> 3) //mock[Pipeline.Metadata]

        val pipeline = Pipeline.newPipeline(classOf[AnyRef], expected)

        val actual = pipeline.currentMetadata
        actual should be (expected)
      }

      it("should create a new pipeline with default close function when only the class type and metadata map are provided") {
        val expected = Pipeline.DefaultCloseFunc

        val pipeline = Pipeline.newPipeline(classOf[AnyRef], Map("a" -> 3))

        val actual = pipeline.closeFunc
        actual should be (expected)
      }

      it("should create a new pipeline with the provided close function and metadata map") {
        val mockCloseFunc = mockFunction[Unit]
        val metadata = Map("a" -> 3)

        val pipeline = Pipeline.newPipeline(classOf[AnyRef], mockCloseFunc, metadata)

        pipeline.currentMetadata should be (metadata)

        mockCloseFunc.expects().once()
        pipeline.close()
      }

      it("should create a new pipeline with the provided close function with data and metadata map") {
        val mockCloseFunc = mockFunction[Option[Any], Unit]
        val metadata = Map("a" -> 3)

        val pipeline = Pipeline.newPipeline(classOf[AnyRef], mockCloseFunc, metadata)

        pipeline.currentMetadata should be (metadata)

        val data = 999
        mockCloseFunc.expects(Some(data)).once()
        pipeline.close(now = true, data = data)
      }
    }
  }
}
