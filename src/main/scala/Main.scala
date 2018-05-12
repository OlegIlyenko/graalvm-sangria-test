import language._

import sangria.schema._
import sangria.execution.{Executor}
import sangria.marshalling.circe._
import sangria.macros.derive._
import sangria.parser.QueryParser
import sangria.parser.DeliveryScheme.Throw

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Main {
  def main(args: Array[String]): Unit = {    
    val FruitType = InterfaceType("Fruit", fields[Unit, Fruit](
      Field("name", StringType, resolve = _.value.name)))

    val AppleType = deriveObjectType[Unit, Apple](
      Interfaces(FruitType))
    val BananaType = deriveObjectType[Unit, Banana](
      Interfaces(FruitType))

    val QueryType = ObjectType("Query", fields[Unit, Unit](
      Field("fruits", ListType(FruitType), resolve = _ ⇒ Seq(Apple("a1", "green"), Banana("b1", 112)))))
    val schema = Schema(QueryType, additionalTypes = AppleType :: BananaType :: Nil)
    val query = QueryParser.parse("""
      { 
        fruits {
          __typename
          name

          ... on Apple {color}
          ... on Banana {length}
        }
      }
    """)
    val result = Await.result(Executor.execute(schema, query), 10 seconds)

    println(result.spaces2)
    
    //  Supposed to print:
    // 
    //  {
    //    "data" : {
    //      "fruits" : [
    //        {
    //          "__typename" : "Apple",
    //          "name" : "a1",
    //          "color" : "green"
    //        },
    //        {
    //          "__typename" : "Banana",
    //          "name" : "b1",
    //          "length" : 112
    //        }
    //      ]
    //    }
    //  }
  }
}

trait Fruit {
  def name: String
}
case class Apple(name: String, color: String) extends Fruit
case class Banana(name: String, length: Int) extends Fruit