
trait Connection {
  def begin(): Unit
  def commit(): Unit
  def rollback(): Unit
  def close(): Unit
}

class ConnectionImpl extends Connection {
  override def begin(): Unit = {
    println("begin")
  }
  override def commit(): Unit = {
    println("commit")
  }
  override def rollback(): Unit = {
    println("rollback")
  }
  override def close(): Unit = {
    println("close")
  }
}

def transactional(block: Connection => Unit): Unit = {
  val conn: Connection = new ConnectionImpl()

  conn.begin()
  try {
    block(conn)
    conn.commit()
  } catch {
    case ex: Exception =>
      conn.rollback()
      throw ex
  } finally {
    conn.close()
  }
}

def save(uploadName: String)(implicit conn: Connection): Unit = {
  println(s"saving $uploadName using $conn")
}

transactional { implicit conn =>
  for (i <- 1 to 3 ) {
    save("aap")
  }
}


