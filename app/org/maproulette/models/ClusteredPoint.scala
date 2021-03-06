// Copyright (C) 2016 MapRoulette contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).
package org.maproulette.models

import play.api.libs.json.{Json, Reads, Writes}

/**
  * @author cuthbertm
  */
case class Point(lat:Double, lng:Double)

/**
  * This is the clustered point that will be displayed on the map. The popup will contain the title
  * of object with a blurb or description of said object. If the object is a challenge then below
  * that will be a start button so you can jump into editing tasks in the challenge
  *
  * @param id The id of the object for this clustered point
  * @param title The title of the object (or name)
  * @param point The latitude and longitude of the point
  * @param blurb A short descriptive text for the object
  * @param status The status of the task, only used for task points, ie. not challenge points
  */
case class ClusteredPoint(id:Long, title:String, point:Point, blurb:String, difficulty:Int, `type`:Int, status:Int = -1)

object ClusteredPoint {
  implicit val pointWrites: Writes[Point] = Json.writes[Point]
  implicit val pointReads: Reads[Point] = Json.reads[Point]
  implicit val clusteredPointWrites: Writes[ClusteredPoint] = Json.writes[ClusteredPoint]
  implicit val clusteredPointReads: Reads[ClusteredPoint] = Json.reads[ClusteredPoint]
}
