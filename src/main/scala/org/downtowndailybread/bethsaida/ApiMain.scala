package org.downtowndailybread.bethsaida

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.typesafe.config.ConfigFactory
import org.downtowndailybread.bethsaida.controller.ApplicationRoutes
import org.downtowndailybread.bethsaida.json._
import org.downtowndailybread.bethsaida.request.UserRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource
import org.downtowndailybread.bethsaida.providers._
import org.downtowndailybread.bethsaida.service.{ExceptionHandlers, RejectionHandlers}
import org.downtowndailybread.bethsaida.worker.EventScheduler

import scala.io.StdIn

object ApiMain {
  def main(args: Array[String]): Unit = {
    val settings = new Settings(ConfigFactory.load())

    val server = new ApiMain(settings)

    server.run()
  }
}

class ApiMain(val settings: Settings)
  extends JsonSupport
    with ApplicationRoutes
    with AuthenticationProvider
    with SettingsProvider {

  val anonymousUser = DatabaseSource.runSql(c => new UserRequest(c, settings).getAnonymousUser())

  def run() = {
    implicit val system = ActorSystem("bethsaida-api")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val workerSystem = ActorSystem("worker-api")
    workerSystem.actorOf(Props(classOf[EventScheduler], settings), "event-scheduler")

    implicit def exceptionHandler: ExceptionHandler = ExceptionHandlers.exceptionHandlers

    implicit def rejectionHandler = RejectionHandler.newBuilder.handle(RejectionHandlers.rejectionHanders).result

    val routes = cors() {
        pathPrefix(settings.prefix / settings.version) {
          path("") {
            complete(s"ddb api ${settings.version}")
          } ~
            allRoutes
        }
    }

    val bindingFuture = Http().bindAndHandle(Route.handlerFlow(routes), "localhost", settings.port)

    println(s"Server online at http://localhost:${settings.port}/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
