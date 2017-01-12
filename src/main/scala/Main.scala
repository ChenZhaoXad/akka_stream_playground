import java.nio.file.Paths

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.{ThrottleMode, IOResult, ActorMaterializer}
import akka.stream.scaladsl._
import akka.util.ByteString
import scala.concurrent.duration._

import scala.concurrent.Future

object Main extends App {

  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()

//  example1
//  example2
  example4

  def example1 = {
    val source: Source[Int, NotUsed] = Source(1 to 100)
    source.runForeach { i => println(i) }(materializer)
  }

  def example2 = {
    val source: Source[Int, NotUsed] = Source(1 to 100)
    val factorials = source.scan(BigInt(1))((acc, next) => acc * next)
    val result: Future[IOResult] =
      factorials.
        map(num => ByteString(s"$num\n")).
        runWith(FileIO.toPath(Paths.get("factorials.txt")))
  }

  // reusable pieces
  def example3LineSink(filename: String): Sink[String, Future[IOResult]] =
    Flow[String]
      .map(s => ByteString(s + "\n"))
      .toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)


  def example4 = {
    val source: Source[Int, NotUsed] = Source(1 to 100)
    val factorials = source.scan(BigInt(1))((acc, next) => acc * next)
    val done: Future[Done] =
      factorials
        .zipWith(Source(1 to 100))((num, idx) => s"$idx! = $num")
        .throttle(1, 1.second, 1, ThrottleMode.shaping)
        .runForeach(println)
  }
}
