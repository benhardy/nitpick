package net.bhardy.nitpick.service

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.{GitAPIException,InvalidRemoteException}
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import org.eclipse.jgit.diff.DiffEntry.ChangeType._
import org.mockito.Matchers.anyObject
import org.mockito.Mockito.when
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verify
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.mock.MockitoSugar

/**
 */
class ChangeSummarySpec extends FunSpec with MustMatchers with MockitoSugar {

  def mockDiffEntry(oldPath:String, newPath:String, changeType:ChangeType): DiffEntry = {
    val diff = mock[DiffEntry]
    when(diff.getOldPath).thenReturn(oldPath)
    when(diff.getNewPath).thenReturn(newPath)
    when(diff.getChangeType).thenReturn(changeType)
    when(diff.toString).thenReturn("["+changeType.toString+"]")
    diff
  } 

  describe("PathSegment") {
    describe("from String List") {
      it("creates a tree") {
        val path = "a/b/c/d"
        val entry = mockDiffEntry(path, path, MODIFY)
        val tree = PathSegment(path, entry)
        tree must be === PathSegment("a", Nil, 
          List(PathSegment("b", Nil, 
            List(PathSegment("c", Nil, 
              List(PathSegment("d", List(entry), Nil))
            ))
          ))
        )
      }
    }
  }

  describe("ChangeSummary") {
    describe("fromDiffs") {
      it("turns a simple diff into a tree") {
        val diffs = Array( 
          mockDiffEntry("a/b", "a/b", MODIFY)
        )
        val summary = ChangeSummary.fromDiffs(diffs.toList)
        summary must be === ChangeSummary(
          List(PathSegment("a", Nil,
            List(PathSegment("b", diffs(0), Nil))
          ))
        )
      }

      it("filters out /dev/null references") {
        val diffs = Array( 
          mockDiffEntry("/dev/null", "a/b", ADD)
        )
        val summary = ChangeSummary.fromDiffs(diffs.toList)
        summary must be === ChangeSummary(
          List(PathSegment("a", Nil,
            List(PathSegment("b", diffs(0), Nil))
          ))
        )
      }

      it("should turn a buncha DiffEntries into a tree") {
        val diffs = Array( 
          mockDiffEntry("a/b/c/d", "a/b/c/d", MODIFY),
          mockDiffEntry("a/b/c/x", "/dev/null", DELETE),
          mockDiffEntry("/dev/null", "a/b/c/e", ADD),
          mockDiffEntry("a/b/c/f", "a/b/c/n", MODIFY),
          mockDiffEntry("a/b/c/g", "a/b/c/h", COPY)
        )
        val summary = ChangeSummary.fromDiffs(diffs.toList)
        summary must be === ChangeSummary(List(PathSegment("a", Nil,
          List(PathSegment("b", Nil,
            List(PathSegment("c", Nil,
              List(
                PathSegment("d", diffs(0), Nil),
                PathSegment("e", diffs(2), Nil),
                PathSegment("f", diffs(3), Nil),
                PathSegment("g", diffs(4), Nil),
                PathSegment("h", diffs(4), Nil),
                PathSegment("n", diffs(3), Nil),
                PathSegment("x", diffs(1), Nil)
              )
            ))
          ))
        )))
      }
    }
  }
}
