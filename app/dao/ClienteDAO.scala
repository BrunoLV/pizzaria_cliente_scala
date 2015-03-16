package dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import model.Cliente

/**
 * Object utilizado para comunicacao com o banco de dados para manipulacao de dados de Cliente.
 */
object ClienteDAO {

  val SQL_INSERT: String = "insert into tb_cliente (nome, idade) values ({nome}, {idade})"
  val SQL_UPDATE: String = "update tb_cliente set nome = {nome}, idade = {idade} where id = {id}"
  val SQL_DELETE: String = "delete from tb_cliente where id = {id}"
  val SQL_FIND_BY_ID: String = "select * from tb_cliente where id = {id}"
  val SQL_FIND_ALL: String = "select * from tb_cliente"

  /**
   * Mapeamento do resultado de queries
   */
  val cliente = {
    get[Int]("id") ~
      get[String]("nome") ~
      get[Int]("idade") map {
      case id ~ nome ~ idade => Cliente(id, nome, idade)
    } // fim da funcao
  } // fim

  def incluirCliente(cliente: Cliente)(implicit connection: Connection): Option[Long] = {
    SQL(SQL_INSERT).on("nome" -> cliente.nome, "idade" -> cliente.idade).executeInsert()
  }

  def editarCliente(cliente: Cliente)(implicit connection: Connection): Int = {
    SQL(SQL_UPDATE).on("nome" -> cliente.nome, "idade" -> cliente.idade, "id" -> cliente.id).executeUpdate()
  }

  def deletarCliente(id: Int)(implicit connection: Connection): Int = {
    SQL(SQL_DELETE).on("id" -> id).executeUpdate()
  }

  def consultarPorId(id: Int)(implicit connection: Connection): Cliente = {
    SQL(SQL_FIND_BY_ID).on("id" -> id).as(cliente.single)
  }

  def listarTodos()(implicit connection: Connection): List[Cliente] = {
    SQL(SQL_FIND_ALL).as(cliente *)
  }

}