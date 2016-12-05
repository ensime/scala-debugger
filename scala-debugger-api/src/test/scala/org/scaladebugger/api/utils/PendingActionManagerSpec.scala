package org.scaladebugger.api.utils

import java.util.concurrent.atomic.AtomicBoolean

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.utils.ActionInfo.ActionId

class PendingActionManagerSpec extends test.ParallelMockFunSpec
{
  private val TestActionId = java.util.UUID.randomUUID().toString

  private case class TestActionData(name: String, value: Int)

  private val pendingActionManager = new PendingActionManager[TestActionData] {
    override protected def newActionId(): ActionId = TestActionId
  }

  describe("PendingActionManager") {
    describe("#addPendingAction") {
      it("should return the id of the new action") {
        val expected = TestActionId

        val actionInfo = TestActionData("name", 3)
        val actual = pendingActionManager.addPendingAction(actionInfo, () => {})

        actual should be (expected)
      }

      it("should create a new action collection with the provided action") {
        val expected = ActionInfo(
          TestActionId,
          TestActionData("name", 3),
          () => {}
        )

        val actionId = pendingActionManager.addPendingAction(
          expected.data,
          expected.action
        )

        val actions = pendingActionManager.getPendingActionsWithId(actionId).get
        val actual = actions.head

        actions should have length 1
        actual should be (expected)
      }
    }

    describe("#addPendingActionWithId") {
      it("should add the action using the provided id") {
        val expected = java.util.UUID.randomUUID().toString

        val actionInfo = TestActionData("name", 3)
        val actual = pendingActionManager.addPendingActionWithId(
          expected,
          actionInfo,
          () => {}
        )

        actual should be (expected)
      }

      it("should append the action to the collection of existing actions") {
        val expected = Seq(
          ActionInfo(TestActionId, TestActionData("name", 3), () => {}),
          ActionInfo(TestActionId, TestActionData("name2", -3), () => {})
        )

        expected.foreach(a => pendingActionManager.addPendingActionWithId(
          TestActionId,
          a.data,
          a.action
        ))

        val actual =
          pendingActionManager.getPendingActionsWithId(TestActionId).get

        actual should contain theSameElementsAs (expected)
      }
    }

    describe("#processAllActions") {
      it("should process all actions") {
        val action1 = new AtomicBoolean(false)
        val action2 = new AtomicBoolean(false)

        pendingActionManager.addPendingAction(null, () => action1.set(true))
        pendingActionManager.addPendingAction(null, () => action2.set(true))

        pendingActionManager.processAllActions()

        // NOTE: Using assert for more helpful error messages
        assert(action1.get(), "First action not invoked!")
        assert(action2.get(), "Second action not invoked!")
      }

      it("should return the info of any successfully-executed actions") {
        val expected = ActionInfo(
          TestActionId,
          TestActionData("name", 3),
          () => {}
        )

        pendingActionManager.addPendingAction(expected.data, expected.action)

        val actual = pendingActionManager.processAllActions()

        actual should contain only (expected)
      }

      it("should remove any successfully-executed actions") {
        val actionId = pendingActionManager.addPendingAction(null, () => {})

        pendingActionManager.processAllActions()

        pendingActionManager.getPendingActionsWithId(actionId) should be (None)
      }

      it("should not remove any action that failed") {
        val actionData = TestActionData("name", 3)
        val action = () => throw new Throwable
        val expected = Some(Seq(ActionInfo(TestActionId, actionData, action)))

        val actionId = pendingActionManager.addPendingAction(actionData, action)

        pendingActionManager.processAllActions()

        val actual = pendingActionManager.getPendingActionsWithId(actionId)

        actual should be (expected)
      }
    }

    describe("#processActions") {
      it("should process all actions whose predicate yields true") {
        val action1 = new AtomicBoolean(false)
        val action2 = new AtomicBoolean(false)

        pendingActionManager.addPendingActionWithId(
          "1",
          null,
          () => action1.set(true)
        )
        pendingActionManager.addPendingActionWithId(
          "2",
          null,
          () => action2.set(true)
        )

        pendingActionManager.processActions(_.id == "1")

        // NOTE: Using assert for more helpful error messages
        assert(action1.get(), "First action not invoked!")
        assert(!action2.get(), "Second action was unexpectedly invoked!")
      }

      it("should return the info of any successfully-executed actions") {
        val expected = Seq(
          ActionInfo(TestActionId, TestActionData("name", 3), () => {})
        )

        pendingActionManager.addPendingAction(
          expected.head.data,
          expected.head.action
        )

        val actual = pendingActionManager.processActions(_.data.name == "name")
        actual should be (expected)
      }

      it("should remove any successfully-executed actions") {
        val actionId = pendingActionManager.addPendingAction(null, () => {})

        pendingActionManager.processActions(_.id == actionId)

        pendingActionManager.getPendingActionsWithId(actionId) should be (None)
      }

      it("should not remove any action that failed") {
        val actionData = TestActionData("name", 3)
        val action = () => throw new Throwable
        val expected = Some(Seq(ActionInfo(TestActionId, actionData, action)))

        val actionId = pendingActionManager.addPendingAction(
          actionData,
          action
        )

        pendingActionManager.processActions(_.data.name == "name")

        val actual = pendingActionManager.getPendingActionsWithId(actionId)

        actual should be (expected)
      }
    }

    describe("#processActionsWithId") {
      it("should process all actions in the collection with the provided id") {
        val action1 = new AtomicBoolean(false)
        val action2 = new AtomicBoolean(false)

        pendingActionManager.addPendingActionWithId(
          "1",
          null,
          () => action1.set(true)
        )
        pendingActionManager.addPendingActionWithId(
          "2",
          null,
          () => action2.set(true)
        )

        pendingActionManager.processActionsWithId("1")

        // NOTE: Using assert for more helpful error messages
        assert(action1.get(), "First action not invoked!")
        assert(!action2.get(), "Second action was unexpectedly invoked!")
      }

      it("should return the info of any successfully-executed actions") {
        val expected = Some(Seq(
          ActionInfo(TestActionId, TestActionData("name", 3), () => {})
        ))

        val actionId = pendingActionManager.addPendingAction(
          expected.get.head.data,
          expected.get.head.action
        )

        val actual = pendingActionManager.processActionsWithId(actionId)
        actual should be (expected)
      }

      it("should remove any successfully-executed actions") {
        val actionId = pendingActionManager.addPendingAction(null, () => {})

        pendingActionManager.processActionsWithId(actionId)

        pendingActionManager.getPendingActionsWithId(actionId) should be (None)
      }

      it("should not remove any action that failed") {
        val actionData = TestActionData("name", 3)
        val action = () => throw new Throwable
        val expected = Some(Seq(ActionInfo(TestActionId, actionData, action)))

        val actionId = pendingActionManager.addPendingAction(
          actionData,
          action
        )

        pendingActionManager.processActionsWithId(actionId)

        val actual = pendingActionManager.getPendingActionsWithId(actionId)

        actual should be (expected)
      }
    }

    describe("#getPendingActionsWithId") {
      it("should get the information for actions with the provided id") {
        val expected = Some(Seq(
          ActionInfo(TestActionId, TestActionData("name1", 1), () => {}),
          ActionInfo(TestActionId, TestActionData("name2", 2), () => {})
        ))

        expected.get.foreach(a => pendingActionManager.addPendingActionWithId(
          TestActionId, a.data, a.action
        ))

        val actual = pendingActionManager.getPendingActionsWithId(TestActionId)

        actual should be (expected)
      }

      it("should return None if the action id does not exist") {
        pendingActionManager.getPendingActionsWithId(TestActionId) should
          be (None)
      }
    }

    describe("#getPendingActions") {
      it("should get the tuple of action info and actions whose predicate yields true") {
        val expected = Seq(
          ActionInfo(TestActionId, TestActionData("name1", 1), () => {}),
          ActionInfo(TestActionId, TestActionData("name1", 2), () => {})
        )

        expected.foreach(a => pendingActionManager.addPendingActionWithId(
          TestActionId, a.data, a.action
        ))
        pendingActionManager.addPendingActionWithId(
          TestActionId,
          TestActionData("othername", 999),
          () => {}
        )

        val actual = pendingActionManager.getPendingActions(
          _.data.name == "name1"
        )

        actual should be (expected)
      }
    }

    describe("#getPendingActionDataWithId") {
      it("should get the action info for all actions with the provided id") {
        val expected = Some(Seq(
          TestActionData("name1", 1),
          TestActionData("name2", 2)
        ))

        expected.get.foreach(pendingActionManager.addPendingActionWithId(
          TestActionId,
          _: TestActionData,
          null
        ))

        val actual =
          pendingActionManager.getPendingActionDataWithId(TestActionId)

        actual should be (expected)
      }

      it("should return None if the action id does not exist") {
        pendingActionManager.getPendingActionDataWithId(TestActionId) should
          be (None)
      }
    }

    describe("#getPendingActionData") {
      it("should get the action info for all actions whose predicate yields true") {
        val expected = Seq(
          TestActionData("name1", 1),
          TestActionData("name1", 2)
        )

        expected.foreach(pendingActionManager.addPendingActionWithId(
          TestActionId,
          _: TestActionData,
          null
        ))
        pendingActionManager.addPendingActionWithId(
          TestActionId,
          TestActionData("othername", 999),
          () => {}
        )

        val actual =
          pendingActionManager.getPendingActionData(_.data.name == "name1")

        actual should be (expected)
      }

      it("should return None if the action id does not exist") {
        pendingActionManager.getPendingActionDataWithId(TestActionId) should
          be (None)
      }
    }

    describe("#removingPendingActionsWithId") {
      it("should remove all actions in the collection with the provided id") {
        pendingActionManager.addPendingActionWithId(
          TestActionId, TestActionData("name1", 1), null
        )
        pendingActionManager.addPendingActionWithId(
          TestActionId, TestActionData("name2", 2), null
        )

        pendingActionManager.removePendingActionsWithId(TestActionId)

        pendingActionManager.getPendingActionDataWithId(TestActionId) should
          be (None)
      }

      it("should return all removed action infos") {
        val expected = Some(Seq(
          ActionInfo(TestActionId, TestActionData("name1", 1), () => {}),
          ActionInfo(TestActionId, TestActionData("name2", 2), () => {})
        ))

        expected.get.foreach(a => pendingActionManager.addPendingActionWithId(
          a.id, a.data, a.action
        ))

        val actual =
          pendingActionManager.removePendingActionsWithId(TestActionId)

        actual should be (expected)
      }

      it("should return None if the action id does not exist") {
        pendingActionManager.removePendingActionsWithId(TestActionId) should
          be (None)
      }
    }

    describe("#removePendingActions") {
      it("should remove any action whose predicates yield true") {
        val expected = Some(Seq(
          ActionInfo(TestActionId, TestActionData("othername", 999), () => {})
        ))

        val actionInfoToRemove = Seq(
          TestActionData("name1", 1),
          TestActionData("name1", 1)
        )

        actionInfoToRemove.foreach(pendingActionManager.addPendingActionWithId(
          TestActionId, _: TestActionData, null
        ))

        expected.get.foreach(a => pendingActionManager.addPendingActionWithId(
          a.id, a.data, a.action
        ))

        pendingActionManager.removePendingActions(
          _.data == TestActionData("name1", 1)
        )

        val actual = pendingActionManager.getPendingActionsWithId(TestActionId)

        actual should be (expected)
      }

      it("should return all action infos of removed actions") {
        val expected = Seq(
          ActionInfo(TestActionId, TestActionData("name1", 1), () => {}),
          ActionInfo(TestActionId, TestActionData("name1", 1), () => {})
        )

        expected.foreach(a => pendingActionManager.addPendingActionWithId(
          a.id, a.data, a.action
        ))

        // Add another action that should not be removed
        pendingActionManager.addPendingActionWithId(
          TestActionId,
          TestActionData("othername", 999),
          null
        )

        val actual = pendingActionManager.removePendingActions(
          _.data == TestActionData("name1", 1)
        )

        actual should be (expected)
      }
    }
  }
}
