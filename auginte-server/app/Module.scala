import com.auginte.server.helpers.GuiceOnStart
import com.auginte.server.services.Provisioning
import com.google.inject.AbstractModule

import scala.language.existentials

class Module extends AbstractModule with GuiceOnStart {
  override def configure() = {
    bind(classOf[Provisioning]).asEagerSingleton()

    val (matcher, listener) = onStart(classOf[Provisioning])(_.provision())
    binder.bindListener(matcher, listener)
  }
}
