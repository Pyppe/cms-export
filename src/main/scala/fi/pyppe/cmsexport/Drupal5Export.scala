package fi.pyppe.cmsexport

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.commons.dbcp.BasicDataSource
import scalikejdbc._
import org.joda.time.DateTime
import java.text.Normalizer
import java.io.File

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
      entries.sortBy(_.createTime.getMillis).foreach(writeJekyllFile)


    case _ =>
      System.err.println("Usage: run <url> <user> <password>")
      System.err.println("e.g. run localhost:3306/example drupal foo123")
      sys.exit(1)
  }

  def writeJekyllFile(entry: Entry) = {
    val dir = new File("/home/pyppe/dev/own_projects/infocrea.fi/_posts")
    val t = entry.createTime
    val file = new File(dir, s"${t.getYear}-${t.toString("MM")}-${t.toString("dd")}-${slugify(entry.title)}.html")
    val post =
      s"""
---
layout: post
title: "${entry.title.replaceAll("\"" ,"""\\"""")}"
date: ${t.toString("yyyy-MM-dd HH:mm")}
categories: general
excerpt: |
  ${cleanText(entry.teaser).replace("\n", "\n  ")}
---
${cleanText(entry.body)}

""".trim

    val fw = new java.io.FileWriter(file)
    fw.write(post)
    fw.close()

  }

  def cleanText(text: String) = {
    text.replace("\r\n", "\n")
  }

  def slugify(str: String): String =
    noAccents(str).
      // Apostrophes
      replaceAll("([a-z])'s([^a-z])", "$1s$2").
      replaceAll("[^\\w]", "-").replaceAll("-{2,}", "-").
      // Get rid of any - at the start and end.
      replaceAll("-+$", "").replaceAll("^-+", "").
      toLowerCase

  def noAccents(string: String): String =
    Normalizer.normalize(string, Normalizer.Form.NFKC).
      replaceAll("[àáâãäåāąă]", "a").
      replaceAll("[çćčĉċ]", "c").replaceAll("[ďđð]", "d").
      replaceAll("[èéêëēęěĕė]", "e").replaceAll("[ƒſ]", "f").
      replaceAll("[ĝğġģ]", "g").replaceAll("[ĥħ]", "h").
      replaceAll("[ìíîïīĩĭįı]", "i").replaceAll("[ĳĵ]", "j").
      replaceAll("[ķĸ]", "k").replaceAll("[łľĺļŀ]", "l").
      replaceAll("[ñńňņŉŋ]", "n").replaceAll("[òóôõöøōőŏœ]", "o").
      replaceAll("[Þþ]", "p").replaceAll("[ŕřŗ]", "r").
      replaceAll("[śšşŝș]", "s").replaceAll("[ťţŧț]", "t").
      replaceAll("[ùúûüūůűŭũų]", "u").replaceAll("[ŵ]", "w").
      replaceAll("[ýÿŷ]", "y").replaceAll("[žżź]", "z").
      replaceAll("[æ]", "ae").replaceAll("[ÀÁÂÃÄÅĀĄĂ]", "A").
      replaceAll("[ÇĆČĈĊ]", "C").replaceAll("[ĎĐÐ]", "D").
      replaceAll("[ÈÉÊËĒĘĚĔĖ]", "E").replaceAll("[ĜĞĠĢ]", "G").
      replaceAll("[ĤĦ]", "H").replaceAll("[ÌÍÎÏĪĨĬĮİ]", "I").
      replaceAll("[Ĵ]", "J").replaceAll("[Ķ]", "K").
      replaceAll("[ŁĽĹĻĿ]", "L").replaceAll("[ÑŃŇŅŊ]", "N").
      replaceAll("[ÒÓÔÕÖØŌŐŎ]", "O").replaceAll("[ŔŘŖ]", "R").
      replaceAll("[ŚŠŞŜȘ]", "S").replaceAll("[ÙÚÛÜŪŮŰŬŨŲ]", "U").
      replaceAll("[Ŵ]", "W").replaceAll("[ÝŶŸ]", "Y").
      replaceAll("[ŹŽŻ]", "Z").replaceAll("[ß]", "ss")


}
