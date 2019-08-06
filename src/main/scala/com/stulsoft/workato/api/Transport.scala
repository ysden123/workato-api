/*
 * Copyright (c) 2019. Yuriy Stul
 */

package com.stulsoft.workato.api

import org.json4s.{JObject, JValue}

import scala.concurrent.Future
import scala.util.Try

/**
 * @author Yuriy Stul
 */
trait Transport {
  val host:String = "www.workato.com"
  /** Sends GET request with specified parameters and returns result (JSON object)
   *
   * @param params the parameters, e.g. /api/recipes/:recipe_id/jobs
   * @return JSON object or Exception
   */
  def get(params: String): Future[JValue]

  /** Sends PUT request  with specified parameters and returns result (JSON object)
   *
   * @param params he parameters, e.g. /api/recipes/:recipe_id/jobs
   * @return JSON object or Exception
   */
  def put(params: String): Future[JValue]
}
