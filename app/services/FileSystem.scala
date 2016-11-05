package services

import java.io.File
import javax.inject._

import com.github.tototoshi.csv.{CSVReader, DefaultCSVFormat}
import play.api.{Configuration, Logger}

/*
 * This class represents uploaded files to be stored on the filesystem
 */
@Singleton
class FileSystem @Inject() (configuration: Configuration) {

  val repodir = configuration.underlying.getString("importnow.dir")
  val uploadsDir = new File(repodir + "/uploads")
  uploadsDir.mkdirs()
  Logger.info(s"Saving upload files to $uploadsDir")

  def getUploads: List[String] = {
    uploadsDir.list().toList.sorted
  }

  def uploadFile(filename: String, contentType: Option[String])(writeTo: File => Unit): Unit = {
    val file = new File(uploadsDir, filename)
    writeTo(file)
  }

  def fileHeaders(filename: String): Option[List[String]] = {
    withCsvReader(filename) {
      reader => reader.readNext()
    }
  }

  def fileStream[T](filename: String)(streamFunction: Stream[Map[String, String]] => T): T = {
    withCsvReader(filename) {
      reader => streamFunction(reader.toStreamWithHeaders)
    }
  }

  private def withCsvReader[T](filename: String)(function: CSVReader => T): T = {
    val file = new File(uploadsDir, filename)

    implicit object MyFormat extends DefaultCSVFormat {
      override val delimiter = ';'
    }

    val reader = CSVReader.open(file)
    val result = try {
      function(reader)
    } finally {
      reader.close
    }
    result
  }

}
