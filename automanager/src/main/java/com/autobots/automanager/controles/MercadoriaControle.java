package com.autobots.automanager.controles;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.adicionadores.AdicionadorLinkMercadoria;
import com.autobots.automanager.atualizadores.MercadoriaAtualizador;
import com.autobots.automanager.cadastradores.MercadoriaCadastrador;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.excluidores.MercadoriaExcluidor;
import com.autobots.automanager.selecionadores.EmpresaSelecionador;
import com.autobots.automanager.selecionadores.MercadoriaSelecionador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.MercadoriaRepositorio;

@RestController
@RequestMapping("/mercadoria")
public class MercadoriaControle {

	@Autowired
	private AdicionadorLinkMercadoria adicionadorLink;

	@Autowired
	private MercadoriaRepositorio repositorio;

	@Autowired
	private MercadoriaSelecionador selecionador;
	
	@Autowired
	private EmpresaRepositorio empresaRepositorio;
	
	@Autowired
	private EmpresaSelecionador empresaSelecionador;
	
	@Autowired
	private	MercadoriaExcluidor excluidor;
	
	@Autowired
	private MercadoriaCadastrador cadastrador;

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE', 'VENDEDOR')")
	@GetMapping("/{id}")
	public ResponseEntity<Mercadoria> obterMercadoria(@PathVariable long id) {
		List<Mercadoria> mercadorias = repositorio.findAll();
		Mercadoria mercadoria = selecionador.selecionar(mercadorias, id);
		if (mercadoria == null) {
			ResponseEntity<Mercadoria> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(mercadoria);
			ResponseEntity<Mercadoria> resposta = new ResponseEntity<Mercadoria>(mercadoria, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@GetMapping("/mercadorias")
	public ResponseEntity<List<Mercadoria>> obterMercadorias() {
		List<Mercadoria> mercadorias = repositorio.findAll();
		if (mercadorias.isEmpty()) {
			ResponseEntity<List<Mercadoria>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(mercadorias);
			ResponseEntity<List<Mercadoria>> resposta = new ResponseEntity<>(mercadorias, HttpStatus.FOUND);
			return resposta;
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@GetMapping("/mercadoria/{id}")
	public ResponseEntity<List<Mercadoria>> obterempresaMercadoria(@PathVariable long id) {
		List<Empresa> empresas = empresaRepositorio.findAll();
		Empresa empresa = empresaSelecionador.selecionar(empresas, id);
		List<Mercadoria> mercadorias = empresa.getMercadorias().stream().collect(Collectors.toList());
		if (mercadorias.isEmpty()) {
			ResponseEntity<List<Mercadoria>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(mercadorias);
			ResponseEntity<List<Mercadoria>> resposta = new ResponseEntity<>(mercadorias, HttpStatus.FOUND);
			return resposta;
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
	@PostMapping("/cadastro/{id}")
	public void cadastrarMercadoria(@RequestBody List<Mercadoria> Mercadoria, @PathVariable long id) {
		List<Empresa> empresas = empresaRepositorio.findAll();
		Empresa empresa = empresaSelecionador.selecionar(empresas, id);
		cadastrador.cadastrar(empresa.getMercadorias(), Mercadoria);
		empresaRepositorio.save(empresa);
	}

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
	@PutMapping("/atualizar/{id}")
	public void atualizarMercadoria(@RequestBody Mercadoria atualizacao, @PathVariable long id) {
		List<Mercadoria> mercadorias = repositorio.findAll();
		Mercadoria mercadoria = selecionador.selecionar(mercadorias, id);
		MercadoriaAtualizador atualizador = new MercadoriaAtualizador();
		atualizador.atualizar(mercadoria, atualizacao);
		repositorio.save(mercadoria);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
	@PutMapping("/atualizar/empresa/{id}")
	public void atualizarEmpresaMercadoria(@RequestBody List<Mercadoria> atualizacao, @PathVariable long id) {
		List<Empresa> empresas = empresaRepositorio.findAll();
		Empresa empresa = empresaSelecionador.selecionar(empresas, id);
		MercadoriaAtualizador atualizador = new MercadoriaAtualizador();
		atualizador.atualizar(empresa.getMercadorias(), atualizacao);
		empresaRepositorio.save(empresa);
	}

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
	@DeleteMapping("/excluir/empresa/{id}")
	public void excluirEmpresaMercadoria(@RequestBody List<Mercadoria> mercadorias, @PathVariable long id) {
		Empresa empresa = empresaRepositorio.findById(id).get();
		excluidor.excluir(empresa, mercadorias);
		empresaRepositorio.save(empresa);
	}
}
