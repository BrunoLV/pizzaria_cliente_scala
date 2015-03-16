package dao

import java.sql.Connection

import model.{Cliente, Endereco}
import anorm.SqlParser._
import anorm._
import model.Cliente

object EnderecoDAO {

  val SQL_INSERT: String = "insert into tb_endereco (id_cliente, descricao_completo) values ({id_cliente}, {descricao_completo})"
  val SQL_UPDATE: String = "update tb_endereco set descricao_completo = {descricao_completo} where id = {id}"
  val SQL_DELETE: String = "delete from tb_endereco where id = {id}"
  val SQL_FIND_BY_ID: String = "select * from tb_endereco where id = {id}"
  val SQL_FIND_ALL: String = "select * from tb_endereco"
  val SQL_FIND_ALL_BY_ID_CLIENTE: String = "select * from tb_endereco where id_cliente = {id_cliente}"

  val endereco = {
    get[Int]("id")~
    get[Int]("id_cliente")~
    get[String]("descricao_completo") map {
      case id ~ id_cliente ~ descricao_completo => Endereco(
        id,
        descricao_completo,
        Cliente(
          id_cliente,
          null.asInstanceOf[String],
          null.asInstanceOf[Int]))
    }
  }

  def incluirEndereco(endereco: Endereco)(implicit connection: Connection): Option[Long] = {
    SQL(SQL_INSERT).on("id_cliente" -> endereco.cliente.id, "descricao_completo" -> endereco.descricaoCompleta).executeInsert()
  }

  def editarEndereco(endereco: Endereco)(implicit connection: Connection): Int = {
    SQL(SQL_UPDATE).on("descricao_completo" -> endereco.descricaoCompleta, "id" -> endereco.id).executeUpdate()
  }

  def deletarEndereco(id: Int)(implicit connection: Connection): Int = {
    SQL(SQL_DELETE).on("id" -> id).executeUpdate()
  }

  def consultarPorId(id: Int)(implicit connection: Connection): Endereco = {
    SQL(SQL_FIND_BY_ID).on("id" -> id).as(endereco.single)
  }

  def listarTodos()(implicit connection: Connection): List[Endereco] = {
    SQL(SQL_FIND_ALL).as(endereco *)
  }

  def listarTodosPorIdCliente(id: Int)(implicit connection: Connection): List[Endereco] = {
    SQL(SQL_FIND_ALL_BY_ID_CLIENTE).on("id_cliente" -> id).as(endereco *)
  }

}
