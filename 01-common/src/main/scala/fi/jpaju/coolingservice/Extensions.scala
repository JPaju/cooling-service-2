package fi.jpaju
package coolingservice

extension [T](t: T | Null)
  inline def toOption: Option[T] =
    if t == null then None else Some(t.asInstanceOf[T])
