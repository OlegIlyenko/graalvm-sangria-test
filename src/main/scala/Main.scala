import sangria.schema.{Schema, ObjectType, StringType, Field, fields}
import sangria.execution.{Executor}
import sangria.marshalling.circe._
import sangria.parser.QueryParser
import sangria.parser.DeliveryScheme.Throw

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Main {
  def main(args: Array[String]): Unit = {
    val QueryType = ObjectType("Query", fields[Unit, Unit](
      Field("hello", StringType, resolve = _ ⇒ "Hello world!")))
    val schema = Schema(QueryType)
    val query = QueryParser.parse("{ hello }")
    val result = Await.result(Executor.execute(schema, query), 10 seconds)

    println(result.spaces2)
    
    //  Supposed to print:
    // 
    //  {
    //   "data" : {
    //     "hello" : "Hello world!"
    //   }
    // }
  }
}