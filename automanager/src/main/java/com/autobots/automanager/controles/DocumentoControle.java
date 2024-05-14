package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.autobots.automanager.modelo.AdicionadorLinkDocumento;
import com.autobots.automanager.modelo.ClienteSelecionador;
import com.autobots.automanager.modelo.DocumentoAtualizador;
import com.autobots.automanager.modelo.DocumentoCadastrador;
import com.autobots.automanager.modelo.DocumentoExcluidor;
import com.autobots.automanager.modelo.DocumentoSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.DocumentoRepositorio;

@RestController
@RequestMapping("/documento")
public class DocumentoControle {
	
	@Autowired
	private AdicionadorLinkDocumento adicionadorLink;
	
	@Autowired
	private DocumentoRepositorio repositorio;
	
	@Autowired
	private ClienteRepositorio clienteRepositorio;
		
	@Autowired
	private ClienteSelecionador clienteSelecionador;
	
	@Autowired
	private DocumentoSelecionador selecionador;
	
	@GetMapping("/documentos")
	public ResponseEntity<List<Documento>> obterDocumento() {
		List<Documento> documentos = repositorio.findAll();
		if (documentos.isEmpty()) {
			ResponseEntity<List<Documento>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(documentos);
			ResponseEntity<List<Documento>> resposta = new ResponseEntity<>(documentos, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Documento> obterDocumento(@PathVariable long id) {
		List<Documento> documentos = repositorio.findAll();
		Documento documento = selecionador.selecionar(documentos, id);
		if (documento == null) {
			ResponseEntity<Documento> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(documentos);
			ResponseEntity<Documento> resposta = new ResponseEntity<Documento>(documento, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@GetMapping("/documento/{id}")
	public ResponseEntity<List<Documento>> obterClienteDocumento(@PathVariable long id) {
		List<Cliente> clientes = clienteRepositorio.findAll();
		List<Documento> documentos = clienteSelecionador.selecionar(clientes, id).getDocumentos();
		if (documentos.isEmpty()) {
			ResponseEntity<List<Documento>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(documentos);
			ResponseEntity<List<Documento>> resposta = new ResponseEntity<>(documentos, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@PostMapping("/cadastro/{id}")
	public void cadastrarDocumento(@RequestBody List<Documento> documento, @PathVariable long id) {
		Cliente cliente = clienteRepositorio.getById(id);
		DocumentoCadastrador cadastrador = new DocumentoCadastrador();
		cadastrador.cadastrar(cliente, documento);
		clienteRepositorio.save(cliente);
	}
	
	@PutMapping("/atualizar/{id}")
	public void atualizarClienteDocumento(@RequestBody List<Documento> atualizacao, @PathVariable long id) {
		Cliente cliente = clienteRepositorio.getById(id);
		DocumentoAtualizador atualizador = new DocumentoAtualizador();
		atualizador.atualizar(cliente.getDocumentos(), atualizacao);
		clienteRepositorio.save(cliente);
	}
	
	@DeleteMapping("/excluir/{id}")
	public void excluirClienteDocumento(@RequestBody List<Documento> documentos, @PathVariable long id) {
		Cliente cliente = clienteRepositorio.getById(id);
		DocumentoExcluidor excluidor = new DocumentoExcluidor();
		excluidor.excluir(cliente, documentos);
		clienteRepositorio.save(cliente);
		
	}
}
