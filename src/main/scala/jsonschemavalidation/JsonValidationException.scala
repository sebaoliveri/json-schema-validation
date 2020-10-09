package jsonschemavalidation

case class JsonValidationException(errorMessages: Seq[String]) extends RuntimeException {
  override def getMessage: String = errorMessages.mkString(", ")
}
