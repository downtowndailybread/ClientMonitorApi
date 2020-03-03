package org.downtowndailybread.bethsaida.model

import java.util.UUID

import org.downtowndailybread.bethsaida.model.parameters.{LoginParameters, UserParameters}
import spray.json.{JsNull, JsObject, JsString, JsValue, RootJsonFormat, RootJsonWriter}

case class InternalUser(
                         id: UUID,
                         email: String,
                         name: String,
                         salt: String,
                         hash: String,
                         confirmed: Boolean,
                         resetToken: Option[UUID],
                         userLock: Boolean,
                         adminLock: Boolean,
                         admin: Boolean
                       ) {
  def getUserParameters(withPassword: String): UserParameters = UserParameters(
    name,
    LoginParameters(email, withPassword)
  )
}

object InternalUser {
  implicit val converter = new RootJsonFormat[Option[InternalUser]] {
    override def write(o: Option[InternalUser]): JsValue = {
      o match {
        case Some(obj) =>
          JsObject(
            Map(
              "id" -> JsString(obj.id.toString),
              "name" -> JsString(obj.name.toString)
            )
          )
        case None => JsNull
      }

    }

    override def read(json: JsValue): Option[InternalUser] = None
  }
}

object AnonymousUser extends InternalUser(
  UUID.fromString("00000000-0000-0000-0000-000000000000"),
  "", "", "", "", true, None, false, false, false)