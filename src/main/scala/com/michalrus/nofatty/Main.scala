package com.michalrus.nofatty

import com.michalrus.nofatty.ui.Ui
import com.typesafe.scalalogging.StrictLogging

object Main extends App with StrictLogging {
  logger.info("entered `main()`")
  Ui.initialize()
}
