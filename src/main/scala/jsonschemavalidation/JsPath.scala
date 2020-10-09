package jsonschemavalidation

import play.api.libs.json.JsValue

trait PathToken {
  def valueIn(json: JsValue): Option[JsValue]
}
case class PositionToken(int: Int) extends PathToken {
  override def valueIn(json: JsValue): Option[JsValue] = (json \ int).toOption
  override def toString: String = "["+int.toString+"]"
}
case class PropertyToken(string: String) extends PathToken {
  override def valueIn(json: JsValue): Option[JsValue] = (json \ string).toOption
  override def toString: String = string
}
case class NoPropertyToken(maybeTitle: Option[String]) extends PathToken {
  override def valueIn(json: JsValue): Option[JsValue] = Some(json)
  override def toString: String = maybeTitle.getOrElse("")
}
object Path {
  def apply(title: Option[String]): Path = Path(Nil.::(NoPropertyToken(title)))
}
case class Path(tokens: Seq[PathToken]) {
  def valueIn[U](json: JsValue): Option[JsValue] =
    tokens.foldLeft[Option[JsValue]] (Some(json)) {
      (option, token) =>  option.flatMap(token.valueIn) }
  def add(index: Int): Path = copy(tokens = tokens :+ PositionToken(index))
  def add(property: String): Path = copy(tokens = tokens :+ PropertyToken(property))
  override def toString: String = tokens.map(_.toString).mkString(".")
}
