package org.downtowndailybread.bethsaida.model

import spray.json._

sealed abstract class Gender(val idx: Int, val string: String)

case object Male extends Gender(0, "male")
case object Female extends Gender(1, "female")
case object Other extends Gender(2, "other")

object Gender {

  private val all = List(Male, Female, Other)

  def apply(str: String): Gender = all.find(_.string == str).get

  implicit val genderFormat = new RootJsonFormat[Gender] {
    override def read(json: JsValue): Gender = {
      json match {
        case JsNumber(num) =>
          val idx = num.toInt
          idx match {
            case 0 => Male
            case 1 => Female
            case 2 => Other
          }
      }
    }

    override def write(obj: Gender): JsValue = {
      JsNumber(obj.idx)
    }
  }
}