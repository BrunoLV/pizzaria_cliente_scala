package controllers

import dao.EnderecoDAO
import model._
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.db.DB
import play.api.mvc._
import scala.collection.mutable._

/**
 * Created by Bruno on 04/03/2015.
 */
object EnderecoController extends Controller {

  case class EnderecoView(id: Int, descricao: String)

  val dao = EnderecoDAO
  val enderecoForm: Form[EnderecoView] = Form(mapping("id" -> number, "descricao" -> text)(EnderecoView.apply)(EnderecoView.unapply))

  def recuperarIdClienteSession(session: Session): Option[Int] = {
    val valor = session.get("idCliente").getOrElse(null.asInstanceOf[String])
    if (valor != null) {
      Some(Integer.parseInt(valor))
    } else {
      None
    }
  }

  def criar = Action {
    Ok(views.html.endereco_cadastro(null,
      enderecoForm.fill(EnderecoView(null.asInstanceOf[Int], null.asInstanceOf[String]))))
  } // fim da funcao criar

  def listarView(lista: List[Endereco]): List[EnderecoView] = {
    var listaViewMutavel: ListBuffer[EnderecoView] = ListBuffer[EnderecoView]()
    for (endereco <- lista) {
      listaViewMutavel += EnderecoView(endereco.id, endereco.descricaoCompleta)
    } // fim do bloco for
    listaViewMutavel.toList
  }

  /**
   *
   * @param idCliente
   * @return
   */
  def listarEnderecosCliente(idCliente: Int) = Action {
    implicit request =>
    DB.withConnection {
      implicit connection => Ok(views.html.endereco_listagem(null,listarView(dao.listarTodosPorIdCliente(idCliente)))).withSession(request.session + ("idCliente" -> idCliente.toString))
    }
  }

  def editar(id: Int) = Action {
    DB.withConnection {
      implicit connection =>
        val endereco = dao.consultarPorId(id)
        Ok(views.html.endereco_cadastro(null,
          enderecoForm.fill(EnderecoView(endereco.id, endereco.descricaoCompleta))))
    }
  }

  def salvar = Action {
    implicit request => enderecoForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.endereco_cadastro(null, formWithErrors))
      },
      enderecoView => {
        var erro: Boolean = false
        var mensagem: String = null.asInstanceOf[String]
        DB.withTransaction {
          implicit connection =>
            try {
              if (enderecoView.id.equals(0)) {
                recuperarIdClienteSession(request.session).map {
                  idCliente =>
                    val id = dao.incluirEndereco(Endereco(enderecoView.id, enderecoView.descricao, Cliente(idCliente, null.asInstanceOf[String],null.asInstanceOf[Int])))
                    mensagem = "Inclusao realizada com sucesso!!!!"
                }.getOrElse {
                  erro = true
                  mensagem = "Ocorreu um erro durante a execução da acao. Cliente nao identificado para realizar a acao. Contate o administrador do sistema."
                }
              } else {
                dao.editarEndereco(Endereco(enderecoView.id, enderecoView.descricao, Cliente(null.asInstanceOf[Int], null.asInstanceOf[String],null.asInstanceOf[Int])))
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
            if (!erro) {
              recuperarIdClienteSession(request.session).map {
                idCliente => Ok(views.html.endereco_listagem(mensagem, listarView(dao.listarTodosPorIdCliente(idCliente))))
              }.getOrElse {
                BadRequest(views.html.endereco_listagem(mensagem, List[EnderecoView]()))
              } // fim da funcao map
            } else {
              BadRequest(views.html.endereco_listagem(mensagem, List[EnderecoView]()))
            } // fim do bloco if
        } // fim do bloco com conexao gerenciada
      } // fim da funcao
    ) // fim da funcao
  }

  /**
   *
   * @param id
   * @return
   */
  def deletar(id: Int) = Action {
    implicit request => {
      var erro: Boolean = false
      var mensagem: String = null.asInstanceOf[String]
      DB.withTransaction {
        implicit connection =>
          try {
            dao.deletarEndereco(id)
            mensagem = "Exclusao realizada com sucesso!!!"
          } catch {
            case ex: Exception => {
              erro = true
              mensagem = "Ocorreu um erro durante a execução da acao. Contate o administrador do sistema."
            } // fim da funcao anonima
          } // fim do bloco try/catch
      } // fim do bloco de transaction gerenciado
      DB.withConnection {
        implicit connection =>
          if (!erro) {
            recuperarIdClienteSession(request.session).map {
              idCliente => Ok(views.html.endereco_listagem(mensagem, listarView(dao.listarTodosPorIdCliente(idCliente))))
            }.getOrElse {
              BadRequest(views.html.endereco_listagem(mensagem, List[EnderecoView]()))
            }
          } else {
            BadRequest(views.html.endereco_listagem(mensagem, List[EnderecoView]()))
          } // fim do bloco if
      } // fim do bloco com conexao gerenciada
    } // fim da funcao anonima
  } // fim da funcao deletar

} // fim do object EnderecoController