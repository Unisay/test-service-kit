package com.github.unisay.test.kit.service

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import org.slf4j.LoggerFactory

class DatabaseTestService(override val name: String,
                          val port: Int,
                          val afterStartScripts: List[String] = Nil,
                          val resetScripts: List[String] = Nil) extends TestService {

  private val logger = LoggerFactory.getLogger(classOf[DatabaseTestService])

  var pg: EmbeddedPostgres = _

  def start() = {
    pg = EmbeddedPostgres.builder().setPort(port).start()
    logger.info(s"$name started at port ${pg.getPort}")
    afterStartScripts.foreach(executeScript)
  }

  def reset() = resetScripts.foreach(executeScript)

  def stop() = {
    pg.close()
    logger.info("{} stopped", name)
  }

  def executeScript(script: String) = {
    val con = pg.getPostgresDatabase.getConnection
    val statement = con.createStatement
    statement.execute(script)
    statement.close()
    con.close()
  }

  def executeQuery(query: String) = {
    val con = pg.getPostgresDatabase.getConnection
    val statement = con.createStatement
    val rs = statement.executeQuery(query)

    val md = rs.getMetaData
    val columns = md.getColumnCount
    val resultList = scala.collection.mutable.Seq()
    while (rs.next()){
      val row = scala.collection.mutable.Map[String, Any]()

      for(i <- 1 to columns){
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
