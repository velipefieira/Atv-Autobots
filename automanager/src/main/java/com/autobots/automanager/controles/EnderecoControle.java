package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.modelo.AdicionadorLinkEndereco;
import com.autobots.automanager.modelo.ClienteSelecionador;
import com.autobots.automanager.modelo.EnderecoAtualizador;
import com.autobots.automanager.modelo.EnderecoSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.EnderecoRepositorio;

@RestController
@RequestMapping("/endereco")
public class EnderecoControle {
	
	@Autowired
	private EnderecoRepositorio repositorio;
	
	@Autowired
	private ClienteRepositorio clienteRepositorio;
	
	@Autowired
	private ClienteSelecionador clienteSelecionador;
	
	@Autowired
	private EnderecoSelecionador selecionador;
	
	@Autowired
	private AdicionadorLinkEndereco adicionadorLink;
	
	@GetMapping("/enderecos")
	public ResponseEntity<List<Endereco>> obterEndereco() {
		List<Endereco> enderecos = repositorio.findAll();
		if (enderecos.isEmpty()) {
			ResponseEntity<List<Endereco>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(enderecos);
			ResponseEntity<List<Endereco>> resposta = new ResponseEntity<>(enderecos, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Endereco> obterEndereco(@PathVariable long id) {
		List<Endereco> enderecos = repositorio.findAll();
		Endereco endereco = selecionador.selecionar(enderecos, id);
		if (endereco == null) {
			ResponseEntity<Endereco> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(enderecos);
			ResponseEntity<Endereco> resposta = new ResponseEntity<Endereco>(endereco, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	
	@GetMapping("/endereco/{id}")
	public ResponseEntity<Endereco> obterClienteEndereco(@PathVariable long id) {
		List<Cliente> clientes = clienteRepositorio.findAll();
		Cliente cliente = clienteSelecionador.selecionar(clientes, id);
		if (cliente == null) {
			ResponseEntity<Endereco> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(cliente.getEndereco());
			ResponseEntity<Endereco> resposta = new ResponseEntity<Endereco>(cliente.getEndereco(), HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@PutMapping("/atualizar/{id}")
	public void atualizarClienteEndereco(@RequestBody Endereco atualizacao, @PathVariable long id) {
		Cliente cliente = clienteRepositorio.getById(id);
		EnderecoAtualizador atualizador = new EnderecoAtualizador();
		atualizador.atualizar(cliente.getEndereco(), atualizacao);
		clienteRepositorio.save(cliente);
	}

}
