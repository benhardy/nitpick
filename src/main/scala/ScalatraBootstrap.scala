import net.bhardy.nitpick.config.RuntimeConfig
import org.scalatra._
import javax.servlet.ServletContext

/**
 * This is the Scalatra bootstrap file. You can use it to mount servlets or
 * filters. It's also a good place to put initialization code which needs to
 * run at application start (e.g. database configurations), and init params.
 */
class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {

    // Mount one or more servlets
    context.mount(RuntimeConfig.nitpickServlet, "/*")
  }
}
