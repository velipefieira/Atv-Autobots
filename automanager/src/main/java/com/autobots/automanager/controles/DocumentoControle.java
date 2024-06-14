package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.modelo.ClienteSelecionador;
import com.autobots.automanager.modelo.DocumentoAtualizador;
import com.autobots.automanager.modelo.DocumentoCadastrador;
import com.autobots.automanager.modelo.DocumentoExcluidor;
import com.autobots.automanager.repositorios.ClienteRepositorio;

@RestController
@RequestMapping("/documento")
public class DocumentoControle {
	@Autowired
	private ClienteRepositorio repositorio;
		
	@Autowired
	private ClienteSelecionador selecionador;
	
	@GetMapping("/documento/{id}")
	public List<Documento> obterClienteDocumento(@PathVariable long id) {
		List<Cliente> clientes = repositorio.findAll();
		return selecionador.selecionar(clientes, id).getDocumentos();
	}
	
	@PostMapping("/cadastro/{id}")
	public void cadastrarDocumento(@RequestBody List<Documento> documento, @PathVariable long id) {
		Cliente cliente = repositorio.getById(id);
		DocumentoCadastrador cadastrador = new DocumentoCadastrador();
		cadastrador.cadastrar(cliente, documento);
		repositorio.save(cliente);
	}
	
	@PutMapping("/atualizar/{id}")
	public void atualizarClienteDocumento(@RequestBody List<Documento> atualizacao, @PathVariable long id) {
		Cliente cliente = repositorio.getById(id);
		DocumentoAtualizador atualizador = new DocumentoAtualizador();
		atualizador.atualizar(cliente.getDocumentos(), atualizacao);
		repositorio.save(cliente);
	}
	
	@DeleteMapping("/excluir/{id}")
	public void excluirClienteDocumento(@RequestBody List<Documento> documentos, @PathVariable long id) {
		Cliente cliente = repositorio.getById(id);
		DocumentoExcluidor excluidor = new DocumentoExcluidor();
		excluidor.excluir(cliente, documentos);
		repositorio.save(cliente);
	}
}
