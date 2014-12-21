Useful configurations for Jetbrains IDEA
========================================

Live templates
--------------

    class $CLASS_NAME$(var _$var$: $TYPE$ = $DEFAULT$) extends Representation {

        import PersistableImplicits._

        override protected[orientdb] def tableName: String = "$CLASS_NAME$"

        override protected[orientdb] def fields = super.fields ++ Map[String, (this.type) => Object](
            "$var$" -> (_.$var$.boxed)
        )

        def $var$ = get[$TYPE$]("$var$", _$var$)

        def $var$_=($var$: $TYPE$): Unit = set[$TYPE$]("$var$", $var$, _$var$ = _)

        override def toString = "{$CLASS_NAME$: $var$=" + $var$ + " {" + super.toString + "}}"
    }

    object $CLASS_NAME$ {
        def apply($var$: $TYPE$): $CLASS_NAME$ = new $CLASS_NAME$($var$)

        def apply($var$: $TYPE$, r: Representation): $CLASS_NAME$ = {
            val element = apply($var$)
            element.representation = r
            element
        }

        def unapply(data: $CLASS_NAME$) = Some(data.$var$, data.representation)
    }
