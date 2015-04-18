package com.michalrus.nofatty

import java.util.Locale

import com.michalrus.nofatty.ui.Ui
import com.typesafe.scalalogging.StrictLogging

object Main extends App with StrictLogging {
  logger.info("entered `main()`")
  Locale.setDefault(Locale.US)
  Ui.initialize()
}
