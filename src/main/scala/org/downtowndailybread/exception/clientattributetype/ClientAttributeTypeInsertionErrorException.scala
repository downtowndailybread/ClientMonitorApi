package org.downtowndailybread.exception.clientattributetype

import org.downtowndailybread.exception.DDBException

class ClientAttributeTypeInsertionErrorException(exception: Exception)
  extends DDBException(s"could not insert client attribute type: ${exception.getMessage}") {

  def this(urlId: String, objId: String) {
    this(new Exception(s"attribName of $urlId in uri does not match attribName in json of $objId"))
  }
}
