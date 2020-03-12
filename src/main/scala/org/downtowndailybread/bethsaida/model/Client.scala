package org.downtowndailybread.bethsaida.model

import java.time.LocalDate
import java.util.UUID

case class Client(
                   id: UUID,
                   firstName: String,
                   middleName: Option[String],
                   lastName: String,
                   dateOfBirth: LocalDate,
                   gender: Gender,
                   race: Race,
                   phone: Option[String],
                   clientPhoto: Option[UUID],
                   photoId: Option[UUID],
                   intakeDate: Option[LocalDate],
                   intakeUser: Option[InternalUser],
                   isBanned: Boolean,
                   banId: Option[UUID]
                 )
