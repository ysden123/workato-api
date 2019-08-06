/*
 * Copyright (c) 2019. Yuriy Stul
 */

package com.stulsoft.workato.api

import java.io.File
import com.typesafe.config.ConfigFactory

/**
 * @author Yuriy Stul
 */
object AppConfig {
  private lazy val config = ConfigFactory.parseFile(new File("application.conf"))
    .withFallback(ConfigFactory.load())

  def wotkatoToken: String = config.getConfig("workato").getString("token")

  def wotkatoMail: String = config.getConfig("workato").getString("mail")

}
