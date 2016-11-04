package services

import javax.inject._

import com.google.inject.ImplementedBy
import org.eclipse.rdf4j.IsolationLevels
import org.eclipse.rdf4j.model.{IRI, Resource, Statement}
import org.eclipse.rdf4j.repository.RepositoryResult
import org.eclipse.rdf4j.repository.sail.{SailRepository, SailRepositoryConnection}
import org.eclipse.rdf4j.sail.nativerdf.NativeStore
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 */
@ImplementedBy(classOf[TripleStoreImpl])
trait TripleStore {

  def saveAsTriples(uploadName: String, stream: Stream[Map[String, String]]): Unit

  def getSubjects: Set[String]

  def getPredicates: Set[String]

  def getContexts: Set[String]

  def getBySubject(subject: String): Map[String, String]

  def getDistinctObjects(predicate: String): Set[String]
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

  lifecycle.addStopHook { () =>
    Future(repo.shutDown())
  }

  val valueFactory = repo.getValueFactory
  val baseUri = configuration.underlying.getString("importnow.uriroot")

  override def saveAsTriples(uploadName: String, stream: Stream[Map[String, String]]): Unit = {
    var i = 0

    transactional { implicit conn =>
      stream.foreach {
        keyValues => keyValues.foreach {
          case (key, value) =>
            save(uploadName, i , key, value)
        }
        i = i + 1
      }
    }
  }

  private def transactional[T](block: SailRepositoryConnection => T): T = {
    val conn = repo.getConnection
    conn.begin(IsolationLevels.READ_COMMITTED)

    try {
      val t = block(conn)
      conn.commit()
      t
    } catch {
      case ex: Exception =>
        conn.rollback()
        throw ex
    } finally {
      conn.close()
    }
  }

  private def save(uploadName: String, index: Int, predicateString: String, valueString: String)(implicit conn: SailRepositoryConnection): Unit = {
    val context = valueFactory.createIRI(baseUri, s"uploads/$uploadName" )
    val subject = valueFactory.createIRI(baseUri, s"uploads/$uploadName/records/$index" )
    val predicate = valueFactory.createIRI(baseUri, s"$uploadName/$predicateString")
    val value = valueFactory.createLiteral(valueString)
    conn.add(subject, predicate, value, context)
  }

  private def toIri(localName: String): IRI = {
    valueFactory.createIRI(baseUri, localName)
  }

  private def getAll[T](selection: Statement => T) = {
    def collect(collected: Set[T], repositoryResult: RepositoryResult[Statement]): Set[T] = {
      if (repositoryResult.hasNext) {
        val next = repositoryResult.next()
        collect(collected + selection(next), repositoryResult)
      } else {
        collected
      }
    }

    transactional { conn =>
      val statements = conn.getStatements(null, null, null)
      collect(Set(), statements)
    }
  }

  override def getSubjects(): Set[String] = {
    getAll(result => result.getSubject.stringValue())
  }


  override def getPredicates(): Set[String] = {
    getAll(result => result.getPredicate.stringValue())
  }

  override def getBySubject(subjectString: String): Map[String, String] = {
    val subjectResource = valueFactory.createIRI(subjectString)

    def collect(collected: Map[String, String], repositoryResult: RepositoryResult[Statement]): Map[String, String] = {
      if (repositoryResult.hasNext) {
        val next = repositoryResult.next()
        collect(collected + (next.getPredicate.toString -> next.getObject.stringValue()), repositoryResult)
      } else {
        collected
      }
    }

    transactional { conn =>
      val statements = conn.getStatements(subjectResource, null, null)
      collect(Map(), statements)
    }
  }

  override def getDistinctObjects(predicateString: String): Set[String] = {
    val predicateResource = valueFactory.createIRI(predicateString)

    def collect(collected: Set[String], repositoryResult: RepositoryResult[Statement]): Set[String] = {
      if (repositoryResult.hasNext) {
        val next = repositoryResult.next()
        collect(collected + next.getObject.stringValue(), repositoryResult)
      } else {
        collected
      }
    }

    transactional { conn =>
      val statements = conn.getStatements(null, predicateResource, null)
      collect(Set(), statements)
    }
  }

  override def getContexts: Set[String] = {
    def collect(collected: Set[String], repositoryResult: RepositoryResult[Resource]): Set[String] = {
      if (repositoryResult.hasNext) {
        val next = repositoryResult.next()
        collect(collected + next.stringValue(), repositoryResult)
      } else {
        collected
      }
    }

    transactional { conn =>
      collect(Set(), conn.getContextIDs)
    }
  }
}
