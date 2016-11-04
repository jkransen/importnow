package controllers

import javax.inject._

import play.api.mvc._
import play.utils.UriEncoding
import services.{FileUploader, TripleStore}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (tripleStore: TripleStore, fileUploader: FileUploader) extends Controller {

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
      fileUploader.uploadFile(file => upload.ref.moveTo(file), filename, contentType)
      Ok(s"File uploaded: $filename, content type: $contentType")
    }.getOrElse {
      Redirect(routes.HomeController.index).flashing(
        "error" -> "Missing file")
    }
  }

  def listSubjects = Action {
    val subjects: Set[String] = tripleStore.getSubjects
    Ok(views.html.subjects(subjects.toList.sorted))
  }

  def getSubject(subjectEncoded: String) = Action {
    val subject = UriEncoding.decodePath(subjectEncoded, "US-ASCII")
    val properties: Map[String, String] = tripleStore.getBySubject(subject)
    Ok(views.html.subject(subject, properties))
  }

  def listPredicates = Action {
    val predicates: Set[String] = tripleStore.getPredicates
    Ok(views.html.predicates(predicates.toList.sorted))
  }

  def getPredicate(predicateEncoded: String) = Action {
    val predicate = UriEncoding.decodePath(predicateEncoded, "US-ASCII")
    val distinctObjects = tripleStore.getDistinctObjects(predicate)
    Ok(views.html.predicate(predicate, distinctObjects.toList.sorted))
  }

  def listUploadsContexts() = Action {
    val uploads = fileUploader.getUploads
    val contexts = tripleStore.getContexts.toList.sorted
    Ok(views.html.uploadsContexts(zip(uploads, contexts)))
  }

  private def zip(uploads: List[String], contexts: List[String]): List[(Option[String], Option[String])] = {
    // TODO
    List((Some(uploads.head), Some(contexts.head)))
  }

}
