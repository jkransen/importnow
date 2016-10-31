package services

import java.io.File
import javax.inject.{Inject, Singleton}

import com.github.tototoshi.csv.CSVReader

/**
  * Created by jeroen@kransen.nl on 29-10-16.
  */
@Singleton
class FileUploader @Inject() (tripleStore: TripleStore) {

  def uploadFile(file: File, filename: String, contentType: Option[String]): Unit = {

    val reader = CSVReader.open(file)

    val fileStream = reader.toStreamWithHeaders

    tripleStore.saveAsTriples(filename, fileStream)
  }

}
