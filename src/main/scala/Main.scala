import doobie.util.transactor.Transactor
import doobie.util.update.{Update, Update0}
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import zio._
import zio.console._
import zio.interop.catz._
import sttp.client._
import sttp.model.Uri
import ujson.Value

import scala.collection.mutable.ArrayBuffer

object Main extends App {
  val xa: Aux[Task, Unit] = Transactor.fromDriverManager[Task](
    "org.sqlite.JDBC", "jdbc:sqlite:sample.db", "", ""
  )


  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    myAppLogic.fold(_ => 1, _ => 0)

  val myAppLogic: ZIO[Console, Throwable, Unit] =
    asynchttpclient.zio.AsyncHttpClientZioBackend().flatMap { implicit zioBackend =>
      {
        for {
          _ <- createTable.run
            .attemptSql
            .transact(xa)
            .absolve
          userResponse <- basicRequest.get(uri"https://api.github.com/users/geggo98/repos").send().retry(Schedule.recurs(5))
          uris = userResponse.body.toOption
            .flatMap(ujson.read(_).arrOpt).getOrElse(ArrayBuffer.empty[Value])
            .flatMap(_.obj("url").strOpt)
          repoResponses <- Task.collectAllParN(5)(uris.map(uri => basicRequest.get(uri"${uri}").send().retry(Schedule.recurs(5))))
          _ <- putStrLn(repoResponses.toString())
        } yield ()
      }.ensuring(zioBackend.close().ignore)
    }

  case class Repository(id : Int, name : String)
  def createTable: Update0 = sql"""
       |CREATE TABLE IF NOT EXISTS
       | repository (
       |   id int PRIMARY KEY,
       |    name text unique)""".stripMargin.update

  def insertRepository(r : Repository) =
    Update[Repository](sql"""
         |INSERT INTO reposirory VALUES (?,?) ON CONFLICT DO NOTHING
         |""".stripMargin.toString()).run(r)

  def findAll =
    sql"""
         | SELECT id, name FROM repository
         |""".stripMargin.query[Repository].to[List]
}