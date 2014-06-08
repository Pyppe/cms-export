package fi.pyppe.cmsexport

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.commons.dbcp.BasicDataSource
import scalikejdbc._

object Drupal5Export extends App with LazyLogging {


  args match {
    case Array(url, user, password) =>
      //val ds = initDataSource(url, user, password)
      Class.forName("com.mysql.jdbc.Driver")
      ConnectionPool.singleton("jdbc:mysql://" + url, user, password)
      implicit val session = AutoSession

      val entities: List[Map[String, Any]] = sql"select * from node_revisions".map(_.toMap).list.apply()
      println(entities)

    case _ =>
      System.err.println("Usage: run <url> <user> <password>")
      System.err.println("e.g. run localhost:3306/example drupal foo123")
      sys.exit(1)
  }

  /*
  def initDataSource(url: String, user: String, password: String): BasicDataSource = {
    val ds = new BasicDataSource()
    ds.setUsername(user)
    ds.setPassword(password)
    ds.setUrl(url)
    ds.setDriverClassName("com.mysql.jdbc.Driver")
    ds
  }
  */

}
