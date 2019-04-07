package org.downtowndailybread.json

import org.downtowndailybread.model._
import spray.json._
import DefaultJsonProtocol._
import org.downtowndailybread.exception.MalformedJsonErrorException
import org.downtowndailybread.request.{ClientAttributeTypeRequest, DatabaseSource}


trait ClientJson extends BaseSupport {

  implicit val clientAttributeTypeAttribFormat = new RootJsonFormat[ClientAttributeTypeAttribute] {
    override def read(json: JsValue): ClientAttributeTypeAttribute = {
      json match {
        case JsObject(fields) =>
          ClientAttributeTypeAttribute(
            fields("name").convertTo[String],
            fields("datatype").convertTo[String],
            fields("required").convertTo[Boolean],
            fields("requiredForOnboarding").convertTo[Boolean],
            fields("ordering").convertTo[Int]
          )
        case _ => throw new MalformedJsonErrorException("could not parse client attribute type attrib")
      }
    }

    override def write(obj: ClientAttributeTypeAttribute): JsValue = {
      JsObject(
        ("name", JsString(obj.displayName)),
        ("datatype", JsString(obj.dataType)),
        ("required", JsBoolean(obj.required)),
        ("requiredForOnboarding", JsBoolean(obj.requiredForOnboarding)),
        ("ordering", JsNumber(obj.ordering))
      )
    }
  }

  implicit val clientAttributeTypeFormat = new RootJsonFormat[ClientAttributeType] {
    override def read(json: JsValue): ClientAttributeType = {
      json match {
        case JsObject(fields) => ClientAttributeType(
          fields("id").convertTo[String],
          clientAttributeTypeAttribFormat.read(json)
        )
        case _ => throw new MalformedJsonErrorException("could not parse client attribute type")
      }
    }

    override def write(obj: ClientAttributeType): JsValue = {
      val oMap =
        JsObject.unapply(clientAttributeTypeAttribFormat.write(obj.clientAttributeTypeAttribute).asJsObject).get
      JsObject(oMap + (("id", JsString(obj.id))))
    }
  }

  implicit val seqClientAttributeTypeFormat = seqFormat[ClientAttributeType]

  implicit val seqClientAttributeFormat = new RootJsonFormat[Seq[ClientAttribute]] {

    override def read(json: JsValue): Seq[ClientAttribute] = {
      val attTypes = DatabaseSource.runSql(c => new ClientAttributeTypeRequest(c).getClientAttributeTypes())
      json match {
        case JsArray(arr) => arr.map {
          attrib =>
            attrib match {
              case JsObject(m) => attTypes.find(_.id == m("clientAttributeTypeId").convertTo[String]) match {
                case Some(tpe) => ClientAttribute(tpe, m("value").asJsObject)
              }
            }
        }
      }
    }

    override def write(objSeq: Seq[ClientAttribute]): JsValue = {
      JsArray(objSeq.map(obj =>
        JsObject(
          ("clientAttributeTypeId", JsString(obj.attributeType.id)),
          ("value", obj.attributeValue)
        )
      ).toVector
      )
    }
  }

  implicit val clientFormat = new RootJsonFormat[Client] {
    override def read(json: JsValue): Client = {
      json match {
        case JsObject(obj) => Client(
          parseUUID(obj("id").convertTo[String]),
          obj("attributes") match {
            case s: JsObject => seqClientAttributeFormat.read(s)
          }
        )
      }
    }

    override def write(obj: Client): JsValue = {
      JsObject(
        ("id", JsString(obj.id.toString)),
        ("attributes", seqClientAttributeFormat.write(obj.attributes))
      )

    }
  }
  implicit val seqClientFormat = seqFormat[Client]
}

