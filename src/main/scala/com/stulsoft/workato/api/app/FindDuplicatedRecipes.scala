/*
 * Copyright (c) 2019. Yuriy Stul
 */

package com.stulsoft.workato.api.app

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.stulsoft.workato.api.AkkaTransport
import com.typesafe.scalalogging.LazyLogging
import org.json4s.JsonAST.JString

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

/**
 * @author Yuriy Stul
 */
object FindDuplicatedRecipes extends LazyLogging {
  val system: ActorSystem = ActorSystem("WorkatoService")
  val materializer: ActorMaterializer = ActorMaterializer()(system)
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  def main(args: Array[String]): Unit = {
    new AkkaTransport(system, materializer).get("recipes?per_page=10000").onComplete {
      case Success(json) =>
        val items = json \ "items"
        val names = items \ "name"
        val duplicates = names
          .children
          .groupBy(identity)
          .collect { case (x, List(_, _, _*)) => x }
          .asInstanceOf[List[JString]]
          .map(n => n.s)
        if (duplicates.isEmpty)
          println("Do duplicates were found")
        else
          println(s"Duplicates (${duplicates.size}): $duplicates")
        println(s"Processed ${items.children.size} recipes")
        system.terminate()
      case Failure(exception) =>
        println(exception.getMessage)
        system.terminate()
    }
  }
}
