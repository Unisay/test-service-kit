package org.zalando.test.kit.service

import java.io.{BufferedReader, FileReader}

import com.opentable.db.postgres.embedded.EmbeddedPostgreSQL
import org.slf4j.LoggerFactory

case class DatabaseTestServiceConfig(port: Int, truncateScript: String)

class DatabaseTestService(val config: DatabaseTestServiceConfig) extends TestService {

  override def name = "Postgres Database"

  private val logger = LoggerFactory.getLogger(classOf[DatabaseTestService])

  var pg: EmbeddedPostgreSQL = _

  def start() = {
    pg = EmbeddedPostgreSQL.builder().setPort(config.port).start()
    logger.info(s"$name started at port ${pg.getPort}")
  }

  def stop() = {
    pg.close()
    logger.info("{} stopped", name)
  }

  private def truncateTables() = {
    val con = pg.getPostgresDatabase.getConnection
    val br: BufferedReader = new BufferedReader(new FileReader(config.truncateScript))
    var str: String = null
    val sb: StringBuffer = new StringBuffer
    while ( {
      str = br.readLine
      str
    } != null) sb.append(str + "\n ")
    val statement = con.createStatement
    statement.execute(sb.toString)
    statement.close()
    con.close()
    logger.debug("Database tables truncated: "+ sb.toString)
  }

  override def beforeTest() = truncateTables()

  def executeQuery(query: String) = {
    val con = pg.getPostgresDatabase.getConnection
    val statement = con.createStatement
    val rs = statement.executeQuery(query)

    val md = rs.getMetaData
    val columns = md.getColumnCount
    val resultList = scala.collection.mutable.Seq()
    while (rs.next()){
      val row = scala.collection.mutable.Map[String, Any]()

      for(i <- 0 to columns){
        row += ((md.getColumnName(i), rs.getObject(i)))
      }
      resultList ++ row
    }
    rs.close()
    statement.closeOnCompletion()
    con.close()

    resultList
  }

  def executeCountQuery(countQuery: String) = {
    val con = pg.getPostgresDatabase.getConnection
    val statement = con.createStatement
    val rs = statement.executeQuery(countQuery)
    val countResult = rs.getInt(1)

    rs.close()
    statement.closeOnCompletion()
    con.close()

    countResult
  }


}
