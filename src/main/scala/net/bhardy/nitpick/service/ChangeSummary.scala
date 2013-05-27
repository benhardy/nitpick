package net.bhardy.nitpick.service

import org.eclipse.jgit.diff.DiffEntry

/**
 * @param name - this node in the path (i.e. a single dir or file name)
 * @param entries - DiffEntries which target this node (usually empty for dirs)
 * @param children - child segments (always empty for files)
 */
case class PathSegment(name:String, entries:List[DiffEntry], children:List[PathSegment]) 

object PathSegment {
  def apply(segmentName:String, entry: DiffEntry, children:List[PathSegment]):PathSegment = {
    PathSegment(segmentName, List(entry), children)
  }
  /** assemble a tree of PathSegments from a complete path string */
  def apply(completePath:String, entry: DiffEntry): PathSegment = {
    apply(completePath.split("/").toList, entry)
  }
  /** assemble a tree of PathSegments from a list of path segment names */
  def apply(pathBits:List[String], entry: DiffEntry):PathSegment = {
    val empty:List[PathSegment] = Nil
    val segs:List[PathSegment] = pathBits.foldRight(empty) { (current,kids) =>
      val entries: List[DiffEntry] = if (kids.isEmpty) List(entry) else Nil
      List(PathSegment(current, entries, kids))
    }
    segs.head
  }
  /** add a new path to an existing PathSegment tree */
  def add(paths:List[PathSegment], newPath:List[String], entry:DiffEntry): List[PathSegment] = {
    val res = (paths, newPath) match {
      case (existing, current::tail) => {
        val foundIndex = existing.indexWhere(_.name.equals(current))
        if (foundIndex >= 0) {
          val old = existing(foundIndex)
          val fixed = old.copy(children=add(old.children, tail, entry))
          existing.updated(foundIndex, fixed) // this'll be slow. use a hash or something TODO
        } else {
          PathSegment(newPath, entry) :: existing
        }
      }
      case (existing, _) => {
        PathSegment(newPath, entry) :: existing
      }
    }
    res.sortBy(_.name)
  }
}

case class ChangeSummary(val trees: List[PathSegment]) {
  def add(path: String, entry:DiffEntry): ChangeSummary = {
    val parts = path.split("/").toList
    ChangeSummary(PathSegment.add(trees, parts, entry))
  }
/*
  def find(pathBits: List[String], segment: PathSegment, result:Option[PathSegment]=None): Option[PathSegment] = {
    (pathBits, segment) match {
      case (Nil, _) => result
      case (head :: rest, PathSegment(name, ents, kids)) => {
        ???
      }
    }
  }
*/
}

object ChangeSummary {
  def fromDiffs(diffs:Iterable[DiffEntry]): ChangeSummary = {
    val diffsAndPaths = diffs.flatMap { diff =>
      List(diff.getOldPath, diff.getNewPath)
        .distinct
        .filter(p => p != null && ! p.equals("/dev/null"))
        .toList
        .map { path => (diff, path) }
    }
    val start: List[PathSegment] = Nil
    val trees = diffsAndPaths.foldLeft(start) { case(segments, (diff, path)) => 
      PathSegment.add(segments, path.split("/").toList, diff)
    }
    ChangeSummary(trees)
  }
}
  

