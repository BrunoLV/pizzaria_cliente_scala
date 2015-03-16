package controllers

import dao.ClienteDAO
import model._
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.db.DB
import play.api.mvc._
import scala.collection.mutable._

import scala.collection.mutable.ListBuffer

/**
 * Controller para interacao de modelo com a pagina relacionada a Cliente.
 */
object ClienteController extends Controller {

  // Será usado apenas para interacao de tela
  case class ClienteView(id: Int, nome: String, idade: Int)

  // DAO utilizado no Controller
  val dao = ClienteDAO

  /**
   * Form para cadastro e clientes.
   */
  val clienteForm: Form[ClienteView] = Form(
    mapping("id" -> number, "nome" -> text, "idade" -> number)(ClienteView.apply)(ClienteView.unapply)
  ) // fim do Form

  /**
   * Funcao para montar lista a ser renderizada.
   * @param lista
   * @return
   */
  def listarView(lista: List[Cliente]): List[ClienteView] = {
    var listaViewMutavel: ListBuffer[ClienteView] = ListBuffer[ClienteView]()
    for (cliente <- lista) {
      listaViewMutavel += ClienteView(cliente.id, cliente.nome, cliente.idade)
    } // fim do bloco for
    listaViewMutavel.toList
  } // fim da funcao listarView

  /**
   * Funcao utilizada para ir para a tela de listage.
   * @return
   */
  def listar = Action {
    DB.withConnection {
      implicit connection =>
        val listaClientes = dao.listarTodos()
        Ok(views.html.cliente_listagem(null, listarView(listaClientes)))
    } // fim do bloco com conexao gerenciada
  } // fim da funcao listar

  /**
   * Funcao utilizada para executar a acao de criacao de novos registros.
   * @return
   */
  def criar = Action {
    Ok(views.html.cliente_cadastro(null,
      clienteForm.fill(ClienteView(null.asInstanceOf[Int], null.asInstanceOf[String], null.asInstanceOf[Int]))))
  } // fim da funcao criar

  /**
   * Funcao utlizada para a acao de edicao de registros.
   * @param id
   * @return
   */
  def editar(id: Int) = Action {
    DB.withConnection {
      implicit connection =>
        val cliente = dao.consultarPorId(id)
        Ok(views.html.cliente_cadastro(null, clienteForm.fill(ClienteView(cliente.id, cliente.nome, cliente.idade))))
    } // fim do bloco
  } // fim da funcao editar

  /**
   * Funcao utilizada para executar a acao de deletar
   * @param id
   * @return
   */
  def deletar(id: Int) = Action {
    var erro: Boolean = false
    var mensagem: String = null.asInstanceOf[String]
    DB.withTransaction {
      implicit connection =>
        try {
          dao.deletarCliente(id)
          mensagem = "Exclusao realizada com sucesso!!!"
        } catch {
          case ex: Exception => {
            erro = true
            mensagem = "Ocorreu um erro durante a execução da acao. Contate o administrador do sistema."
          } // fim da funcao anonima
        } // fim do bloco try/catch
    } // fim do bloco transacional
    DB.withConnection {
      implicit connection =>
        val listaClientes = dao.listarTodos()
        if (!erro) {
          Ok(views.html.cliente_listagem(mensagem, listarView(listaClientes)))
        } else {
          BadRequest(views.html.cliente_listagem(mensagem, listarView(listaClientes)))
        } // fim do bloco if
    } // fim do bloco com conexao gerenciada
  } // fim da funcao deletar

  def salvar = Action {
    implicit request => clienteForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.cliente_cadastro(null, formWithErrors))
      },
      clienteView => {
        var erro: Boolean = false
        var mensagem: String = null.asInstanceOf[String]
        DB.withTransaction {
          implicit connection =>
            try {
              if (clienteView.id.equals(0)) {
                val id = dao.incluirCliente(Cliente(clienteView.id, clienteView.nome, clienteView.idade))
                mensagem = "Inclusao realizada com sucesso!!!!"
              } else {
                dao.editarCliente(Cliente(clienteView.id, clienteView.nome, clienteView.idade))
                mensagem = "Edicao realizada com sucesso!!!"
              } // fim do bloco if
            } catch {
              case ex: Exception => {
                erro = true
                mensagem = "Ocorreu um erro durante a execução da acao. Contate o administrador do sistema."
              } // fim da funcao anonima
            } // fim do bloco try/catch
        } // fim do withTransaction
        DB.withConnection {
          implicit connection =>
            val listaClientes = dao.listarTodos()
            if (!erro) {
              Ok(views.html.cliente_listagem(mensagem, listarView(listaClientes)))
            } else {
              BadRequest(views.html.cliente_listagem(mensagem, listarView(listaClientes)))
            } // fim do bloco if
        } // fim do bloco com conexao gerenciada
      } // fim da funcao
    ) // fim da funcao
  } // fim da funcao salvar

} // fim do object ClienteController