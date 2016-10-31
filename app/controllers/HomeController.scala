package controllers

import java.io.File
import javax.inject._

import play.api.mvc._
import services.{FileSystem, FileUploader}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (fileUploader: FileUploader, fileSystem: FileSystem) extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("picture").map { upload =>
      val filename: String = upload.filename
      val contentType: Option[String] = upload.contentType
      val uploadFile = new File(fileSystem.uploadsDir, filename)
      upload.ref.moveTo(uploadFile)
      fileUploader.uploadFile(uploadFile, filename, contentType)
      Ok(s"File uploaded: $filename, content type: $contentType")
    }.getOrElse {
      Redirect(routes.HomeController.index).flashing(
        "error" -> "Missing file")
    }
  }

}
