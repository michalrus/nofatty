package com.michalrus.nofatty

import com.typesafe.scalalogging.StrictLogging

trait Logging extends StrictLogging {

  def timed[F](what: String)(block: ⇒ F): F = {
    val start = System.currentTimeMillis
    logger.info(what)
    val r = block
    val took = (System.currentTimeMillis - start) / 1000.0
    logger.info(s"done $what, took $took s")
    r
  }

}
