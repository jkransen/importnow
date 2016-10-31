package services

import javax.inject._

import com.google.inject.ImplementedBy
import org.eclipse.rdf4j.IsolationLevels
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.sail.{SailRepository, SailRepositoryConnection}
import org.eclipse.rdf4j.sail.nativerdf.NativeStore
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

/**
 */
@ImplementedBy(classOf[TripleStoreImpl])
trait TripleStore {

  def saveAsTriples(uploadName: String, stream: Stream[Map[String, String]]): Unit;
}

/**
 * This class is a concrete implementation of the [[TripleStore]] trait.
 *
 * This class has a `Singleton` annotation because we need to make
 * sure we only use one TripleStore per application. Without this
 * annotation we would get a new instance every time a [[TripleStore]] is
 * injected.
 */
@Singleton
class TripleStoreImpl @Inject() (filesystem: FileSystem, lifecycle: ApplicationLifecycle, configuration: Configuration) extends TripleStore {

  val repo = new SailRepository(new NativeStore(filesystem.sailDir))
  if (!repo.isInitialized) {
    repo.initialize()
  }

  val valueFactory = repo.getValueFactory
  val baseUri = configuration.underlying.getString("importnow.uriroot")

  override def saveAsTriples(uploadName: String, stream: Stream[Map[String, String]]): Unit = {

    val conn = repo.getConnection
    conn.begin(IsolationLevels.READ_COMMITTED)

    var i = 0

    stream.foreach {
      keyValues => keyValues.foreach {
        case (key, value) => {
          save(uploadName, i , key, value, conn)
          i = i + 1
        }
      }
    }

    conn.commit()
    conn.close()
  }

  private def save(uploadName: String, index: Int, predicateString: String, valueString: String, conn: SailRepositoryConnection): Unit = {
    val context = valueFactory.createIRI(baseUri, s"uploads/$uploadName" )
    val subject = valueFactory.createIRI(baseUri, s"uploads/$uploadName/records/$index" )
    val predicate = valueFactory.createIRI(baseUri, s"$uploadName/$predicateString")
    val value = valueFactory.createLiteral(valueString)
    conn.add(subject, predicate, value, context)
  }

  private def toIri(localName: String): IRI = {
    valueFactory.createIRI(baseUri, localName)
  }
}
