package services

import java.io.File
import javax.inject.{Inject, Singleton}

import com.github.tototoshi.csv.{CSVReader, DefaultCSVFormat}

/**
  * Created by jeroen@kransen.nl on 29-10-16.
  */
@Singleton
class FileUploader @Inject() (tripleStore: TripleStore, fileSystem: FileSystem) {

  def getUploads: List[String] = {
    fileSystem.uploadsDir.list().toList.sorted
  }

  def uploadFile(writeTo: File => Unit, filename: String, contentType: Option[String]): Unit = {

    val file = new File(fileSystem.uploadsDir, filename)

    writeTo(file)

    implicit object MyFormat extends DefaultCSVFormat {
      override val delimiter = ';'
    }

    val reader = CSVReader.open(file)

    val fileStream = reader.toStreamWithHeaders

    tripleStore.saveAsTriples(filename, fileStream)
  }

}
