import net.bhardy.nitpick._
import org.scalatra._
import javax.servlet.ServletContext
import service.{ReviewServiceImpl, ReviewService}

/**
 * This is the Scalatra bootstrap file. You can use it to mount servlets or
 * filters. It's also a good place to put initialization code which needs to
 * run at application start (e.g. database configurations), and init params.
 */
class Scalatra extends LifeCycle {
  override def init(context: ServletContext) {

    // Mount one or more servlets
    context.mount(RuntimeConfig.nitpickServlet, "/*")
  }
}

object RuntimeConfig {
  implicit val reviewService: ReviewService = new ReviewServiceImpl
  implicit val nitpickServlet = new NitpickServlet
}

trait EnvironmentProperties {
  def apply(key:String) : Option[String]
}




