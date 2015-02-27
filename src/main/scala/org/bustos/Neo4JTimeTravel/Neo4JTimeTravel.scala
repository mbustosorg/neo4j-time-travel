package org.bustos.Neo4JTimeTravel

// http://www.anormcypher.org/

import org.anormcypher._
import akka.actor.ActorSystem
import scala.util.{ Success, Failure }
import org.slf4j.LoggerFactory
import scala.concurrent._
import scala.concurrent.duration._
import spray.http._
import spray.client.pipelining._
import java.net.URL

object Neo4JTimeTravel {

  implicit val system = ActorSystem()
  import system.dispatcher

  val logger = LoggerFactory.getLogger(getClass)

  implicit val connection = Neo4jREST()
  var timeNodes = Map.empty[Long, Int]
  val tenMinutes: Long = 600 * 1000
  val fiveMinutes: Long = 300 * 1000
  val oneMinute: Long = 60 * 1000
  val thirtySeconds: Long = 30 * 1000

  def createValueChange(i: Int, variableId: Long, initTime: Long) {
    //val url = new URL("http://localhost:7474/graphaware/timetree/single/event")
    val newNode = Cypher(s"""CREATE (a: VariableValue {value:$i}) return id(a) as theId""").apply.head
    val valueId = newNode[Long]("theId")
    val updateTime = initTime + i * thirtySeconds
    //var postData = s"""{"nodeId":$valueId,"relationshipType":"UPDATE_EVENT","timezone":"UTC","resolution":"SECOND","time": $updateTime}"""
    //val response = Await.rxesult(POST(url).addHeaders("Content-Type" -> "application/json").addBody(postData).apply, 1.second) //this will throw if the response doesn't return within 1 second
    //println(s"Response returned from ${url.toString} with code ${response.code}, body ${response.bodyString}")
    Cypher(s"""MATCH (a:VariableValue),(b:Variable)
  			WHERE id(a) = $valueId AND id(b) = $variableId
  			CREATE (b)-[r:VARIABLE_ITEM]->(a)
  			RETURN r
  	    """).execute
    if (!timeNodes.contains(updateTime)) {
      println("")
    }
    val updateTimeId = timeNodes(updateTime)
    Cypher(s"""MATCH (a:Second),(b:VariableValue)
  			WHERE id(a) = $updateTimeId AND id(b) = $valueId
  			CREATE (b)-[r:UPDATE_EVENT]->(a)
  			RETURN r
  	    """).execute
  }

  def main(args: Array[String]) = {

    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

    logger.info("Delete previous graph...")
    Cypher("start n=node(*) match n-[r]-()  delete r").execute
    Cypher("start n=node(*) delete n").execute
    logger.info("Delete completed.")

    logger.info("Create time nodes...")

    val currentTime = System.currentTimeMillis()

    val timetreeCreates = (currentTime to (currentTime + tenMinutes * 40) by thirtySeconds).map({ x =>
      val timetreeMap: Future[(Long, HttpResponse)] = Future {
        (x, Await.result(pipeline(Get("http://localhost:7474/graphaware/timetree/single/" + x + "?resolution=second")), Duration.Inf))
      }
      timetreeMap
    })

    var cnt = 0
    val futuresList = Future.sequence(timetreeCreates.toList)
    Await.result(futuresList.map { x =>
      {
        x.foreach({
          case (timestamp, response) => timeNodes += (timestamp -> response.entity.asString.toInt)
        })
      }
    }, Duration.Inf)
    logger.info(timeNodes.size + " time nodes created.")

    val numberOfRecords = 100
    val numberOfVariables = 20
    val numberOfUpdates = 5

    // create some test nodes
    logger.info("Creating records...")
    for (i <- 1 to numberOfRecords) {
      print(".")
      val recordName = s"record_${i}_name"
      Cypher(s"""CREATE (record_$i: Record {name:"$recordName"})""").execute
      for (j <- 1 to numberOfVariables) {
        val variableName = s"variable_${j}_name"
        val variableIdentifier = s"variable_$j"
        val newNode = Cypher(s"""CREATE ($variableIdentifier: Variable {name:"$variableName"}) return id($variableIdentifier) as theId""").apply.head
        val variableId = newNode[Long]("theId")
        Cypher(s"""MATCH (a:Record),(b:Variable)
      			WHERE a.name = "$recordName" AND id(b) = $variableId
      			CREATE (a)-[r:RECORD_VARIABLE]->(b)
      			RETURN r
      	    """).execute
        (1 to numberOfUpdates) map { k => createValueChange(k, variableId, currentTime + oneMinute * i) }
      }
    }
    println(".")
    logger.info("Records created.")

    // a simple query
    val req = Cypher("start n=node(*) where not n.name is null return n.name")

    // get a stream of results back
    val stream = req()

    // get the results and put them into a list
    println(stream.map(row => { row[String]("n.name") }).toList)
  }

}

/*
   * cd ..MATCH (p0:Second), (p2:Record),
    (p2)-[:RECORD_VARIABLE]->(p1),
    r3 = shortestPath((p1)-[:VARIABLE_ITEM|:UPDATE_EVENT|:NEXT]->(p0))
  WHERE
    id(p0) = 354271 and (p2.name =~ 'record_._name')
  RETURN r3, p1, p2
  */
