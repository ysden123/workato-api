/*
 * Copyright (c) 2019. Yuriy Stul
 */

package com.stulsoft.workato.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.scalalogging.LazyLogging
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future, Promise}
import scala.util.{Failure, Success}

/**
 * @author Yuriy Stul
 */
class AkkaTransport(theSystem: ActorSystem, theMaterializer: ActorMaterializer) extends Transport with LazyLogging {
  implicit val executionContext: ExecutionContextExecutor = theSystem.dispatcher
  implicit val system: ActorSystem = theSystem
  implicit val materializer: ActorMaterializer = theMaterializer

  /** Sends GET request with specified parameters and returns result (JSON object)
   *
   * @param params the parameters, e.g. /api/recipes/:recipe_id/jobs
   * @return JSON object or Exception
   */
  override def get(params: String): Future[JValue] = {
    val promise = Promise[JValue]

    val connectionFlow = Http().outgoingConnectionHttps(host)
    val request = HttpRequest(uri = "/api/" + buildRequestUrl(params))
    val responseFuture = Source.single(request)
        .via(connectionFlow)
        .runWith(Sink.head)
    responseFuture.andThen{
      case Success(response)=>
        response
          .entity
          .toStrict(5.seconds)
          .map(_.data.decodeString("UTF-8"))
          .andThen{
            case Success(json)=>
//              logger.debug(s"json: $json")
              promise.success(parse(json))
              response.discardEntityBytes()
            case Failure(ex)=>
              logger.error(s"Failed getting json text: ${ex.getMessage}")
              promise.failure(ex)
              response.discardEntityBytes()
          }
      case Failure(ex) =>
        logger.error(s"Failed getting response: ${ex.getMessage}")
        promise.failure(ex)
    }
    promise.future
  }

  /** Sends PUT request  with specified parameters and returns result (JSON object)
   *
   * @param params he parameters, e.g. /api/recipes/:recipe_id/jobs
   * @return JSON object or Exception
   */
  override def put(params: String): Future[JValue] = Future.failed(new RuntimeException("PUT not implemented yet"))

  private def credentials: String = s"user_token=${AppConfig.wotkatoToken}&user_email=${AppConfig.wotkatoMail}"

  private def buildRequestUrl(params: String): String = {
    if (params.contains("?"))
      params + "&" + credentials
    else
      params + "?" + credentials
  }
}
