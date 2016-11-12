package models

/**
  * Created by jeroen on 11-11-16.
  */
case class HeaderMapping(headerName: String, localName: String)
case class HeadersMapping(typeName: String, headers: List[HeaderMapping])

