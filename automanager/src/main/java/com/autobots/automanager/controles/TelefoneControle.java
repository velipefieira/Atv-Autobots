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
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.modelo.ClienteSelecionador;
import com.autobots.automanager.modelo.TelefoneAtualizador;
import com.autobots.automanager.modelo.TelefoneCadastrador;
import com.autobots.automanager.modelo.TelefoneExcluidor;
import com.autobots.automanager.repositorios.ClienteRepositorio;

@RestController
@RequestMapping("/telefone")
public class TelefoneControle {
	
	@Autowired
	private ClienteRepositorio repositorio;
	@Autowired
	private ClienteSelecionador selecionador;

	@GetMapping("/telefone/{id}")
	public List<Telefone> obterClienteTelefone(@PathVariable long id) {
		List<Cliente> clientes = repositorio.findAll();
		return selecionador.selecionar(clientes, id).getTelefones();
	}
	
	@PostMapping("/cadastro/{id}")
	public void cadastrarTelefone(@RequestBody List<Telefone> telefone, @PathVariable long id) {
		Cliente cliente = repositorio.getById(id);
		TelefoneCadastrador cadastrador = new TelefoneCadastrador();
		cadastrador.cadastrar(cliente, telefone);
		repositorio.save(cliente);
	}
	
	@PutMapping("/atualizar/{id}")
	public void atualizarClienteTelefone(@RequestBody List<Telefone> atualizacao, @PathVariable long id) {
		Cliente cliente = repositorio.getById(id);
		TelefoneAtualizador atualizador = new TelefoneAtualizador();
		atualizador.atualizar(cliente.getTelefones(), atualizacao);
		repositorio.save(cliente);
	}
	
	@DeleteMapping("/excluir/{id}")
	public void excluirClienteTelefone(@RequestBody List<Telefone> telefones, @PathVariable long id) {
		Cliente cliente = repositorio.getById(id);
		TelefoneExcluidor excluidor = new TelefoneExcluidor();
		excluidor.excluir(cliente, telefones);
		repositorio.save(cliente);
	}
}
