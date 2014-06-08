package fi.pyppe.cmsexport

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.commons.dbcp.BasicDataSource
import scalikejdbc._
import org.joda.time.DateTime

case class NodeRevision(
  nid: Long,
  vid: Long,
  uid: Long,
  title: String,
  body: String,
  teaser: String,
  log: String,
  timestamp: Long,
  format: Long
) {
  def time: DateTime = new DateTime(timestamp*1000L)
}
object NodeRevision extends SQLSyntaxSupport[NodeRevision] {
  override val tableName = "drupal_node_revisions"
  def apply(rs: WrappedResultSet) =
    new NodeRevision(rs.long("nid"), rs.long("vid"), rs.long("uid"), rs.string("title"),
                 rs.string("body"), rs.string("teaser"), rs.string("log"),
                 rs.long("timestamp"), rs.long("format"))
}

case class Node(nid: Long, vid:Long, changed: Long, created: Long, kind: String)
object Node extends SQLSyntaxSupport[Node] {
  override val tableName = "drupal_node"
  def apply(rs: WrappedResultSet) =
    new Node(rs.long("nid"), rs.long("vid"), rs.long("changed"), rs.long("created"), rs.string("type"))
}

case class Entry(id: Long, title: String, createTime: DateTime, body: String, teaser: String, kind: String)


object Drupal5Export extends App with LazyLogging {


  args match {
    case Array(url, user, password) =>
      //val ds = initDataSource(url, user, password)
      Class.forName("com.mysql.jdbc.Driver")
      ConnectionPool.singleton("jdbc:mysql://" + url, user, password)
      implicit val session = AutoSession

      val revisions = sql"select * from drupal_node_revisions".map(NodeRevision.apply).list.apply()
      val nodes = sql"select * from drupal_node".map(Node.apply).list.apply()
      val entries = revisions.map { rev =>
        val node = nodes.find(n => n.nid == rev.nid && n.vid == rev.vid).get
        val createTime = new DateTime(node.created * 1000L)
        Entry(rev.nid, rev.title, createTime, rev.body, rev.teaser, node.kind)
      }
      entries.sortBy(_.createTime.getMillis).foreach(r => println(s"${r.title}, ${r.createTime}"))
      

    case _ =>
      System.err.println("Usage: run <url> <user> <password>")
      System.err.println("e.g. run localhost:3306/example drupal foo123")
      sys.exit(1)
  }

}
