package controllers

import javax.inject._

import models.{HeaderMapping, HeadersMapping}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.utils.UriEncoding
import services.{FileSystem, TripleStore}

import scala.util.matching.Regex

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (tripleStore: TripleStore, fileSystem: FileSystem, val messagesApi: MessagesApi) extends Controller with I18nSupport {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action {
    Ok(views.html.index())
  }

  def upload() = Action(parse.multipartFormData) { request =>
    request.body.file("forimport").map { upload =>
      val filename: String = upload.filename
      val contentType: Option[String] = upload.contentType
      fileSystem.uploadFile(filename, contentType) {
        file => upload.ref.moveTo(file)
      }
      Redirect(routes.HomeController.mapHeaders(filename))
    }.getOrElse {
      Redirect(routes.HomeController.index()).flashing(
        "error" -> "Missing file")
    }
  }

  val headersMappingForm = Form(
    mapping(
      "typeName" -> text,
      "headers" -> list(
        mapping(
          "headerName" -> text,
          "localName" -> text
        )(HeaderMapping.apply)(HeaderMapping.unapply)
      )
    )(HeadersMapping.apply)(HeadersMapping.unapply)
  )

  def mapHeaders(filename: String) = Action { implicit request =>
    fileSystem.fileHeaders(filename) match {
      case Some(headers) =>
        val headersMapping = headersMappingForm.fill(HeadersMapping(filename.split('.')(0), headers.map(header => HeaderMapping(header, toSlug(header)))))
        Ok(views.html.mapColumns(filename, headersMapping))
      case None => Redirect(routes.HomeController.index()).flashing(
        "error" -> "File has no headers")
    }
  }

  def analyze(filename: String) = Action { implicit request =>
    headersMappingForm.bindFromRequest.fold(
      formWithErrors => {
        // binding failure, you retrieve the form containing errors:
        BadRequest(views.html.mapColumns(filename, formWithErrors))
      },
      mappingData => {
        val typeName = mappingData.typeName
        fileSystem.fileStream(filename) { stream =>
          val headersMapped = mappingData.headers.map {
            case HeaderMapping(headerName, localName) => (headerName -> localName)
          }
          tripleStore.saveAsTriples(mappingData.typeName, stream, headersMapped.toMap)
        }
        Redirect(routes.HomeController.listSubjects())
      }
    )
  }

  def listSubjects() = Action {
    val subjects: Set[String] = tripleStore.getSubjects
    Ok(views.html.subjects(subjects.toList.sorted))
  }

  def getSubject(subjectEncoded: String) = Action {
    val subject = UriEncoding.decodePath(subjectEncoded, "US-ASCII")
    val properties: Map[String, String] = tripleStore.getBySubject(subject)
    Ok(views.html.subject(subject, properties))
  }

  def listPredicates() = Action {
    val predicates: Set[String] = tripleStore.getPredicates
    Ok(views.html.predicates(predicates.toList.sorted))
  }

  def getPredicate(predicateEncoded: String) = Action {
    val predicate = UriEncoding.decodePath(predicateEncoded, "US-ASCII")
    val distinctObjects = tripleStore.getDistinctObjects(predicate)
    Ok(views.html.predicate(predicate, distinctObjects.toList.sorted))
  }

  def listUploadsContexts() = Action {
    val uploads = fileSystem.getUploads
    val contexts = tripleStore.getContexts.toList.sorted
    Ok(views.html.uploadsContexts(zip(uploads, contexts)))
  }

  private def zip(uploads: List[String], contexts: List[String]): List[(Option[String], Option[String])] = {
    // TODO
    List((Some(uploads.head), Some(contexts.head)))
  }

  val regex: Regex = "\\w+".r

  private def toSlug(header: String): String = {
    val headerBeforeBraces = header.split("[\\[|\\(]")(0)
    val slug = regex.findAllIn(headerBeforeBraces.toLowerCase).mkString("_")
    slug
  }


}
