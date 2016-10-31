package services

import java.io.File
import javax.inject._

import play.api.Configuration

/*
 * This class represents files to be stored on the filesystem
 */
@Singleton
class FileSystem @Inject() (configuration: Configuration) {

  val repodir = configuration.underlying.getString("importnow.dir")
  val uploadsDir = new File(repodir + "/uploads")
  uploadsDir.mkdirs()
  val sailDir = new File(repodir + "/sail")
  sailDir.mkdirs()

}
