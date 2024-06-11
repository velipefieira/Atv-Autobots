package com.autobots.automanager.controles;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.adicionadores.AdicionadorLinkTelefone;
import com.autobots.automanager.atualizadores.TelefoneAtualizador;
import com.autobots.automanager.cadastradores.TelefoneCadastrador;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.excluidores.TelefoneExcluidor;
import com.autobots.automanager.selecionadores.UsuarioSelecionador;
import com.autobots.automanager.selecionadores.EmpresaSelecionador;
import com.autobots.automanager.selecionadores.TelefoneSelecionador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.TelefoneRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@RestController
@RequestMapping("/telefone")
public class TelefoneControle {

	@Autowired
	private AdicionadorLinkTelefone adicionadorLink;

	@Autowired
	private TelefoneRepositorio repositorio;

	@Autowired
	private UsuarioRepositorio usuarioRepositorio;

	@Autowired
	private TelefoneSelecionador selecionador;
	
	@Autowired
	private UsuarioSelecionador usuarioSelecionador;
	
	@Autowired
	private EmpresaRepositorio empresaRepositorio;
	
	@Autowired
	private EmpresaSelecionador empresaSelecionador;
	
	@Autowired
	private TelefoneCadastrador telefoneCadastrador;

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@GetMapping("/{id}")
	public ResponseEntity<Telefone> obterTelefone(@PathVariable long id) {
		List<Telefone> Telefones = repositorio.findAll();
		Telefone Telefone = selecionador.selecionar(Telefones, id);
		if (Telefone == null) {
			ResponseEntity<Telefone> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(Telefone);
			ResponseEntity<Telefone> resposta = new ResponseEntity<Telefone>(Telefone, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/telefones")
	public ResponseEntity<List<Telefone>> obterTelefones(Authentication authentication) {
	    String username = authentication.getName();
	    
	    List<Usuario> usuarios = usuarioRepositorio.findAll();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    List<Telefone> telefones = new ArrayList<Telefone>();
	    
	    if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_ADMIN) || usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_GERENTE)) {
	        telefones = repositorio.findAll();
	    } else if(usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
	        telefones = usuarioLogado.getTelefones().stream().collect(Collectors.toList());
	    } else if(usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_CLIENTE)) {
	        telefones = usuarioLogado.getTelefones().stream().collect(Collectors.toList());
	    }
	    
	    if (telefones.isEmpty()) {
			ResponseEntity<List<Telefone>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(telefones);
			ResponseEntity<List<Telefone>> resposta = new ResponseEntity<>(telefones, HttpStatus.FOUND);
			return resposta;
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@GetMapping("/usuario/{id}")
	public ResponseEntity<List<Telefone>> obterUsuarioTelefone(@PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
	    	if (!(usuario.getPerfis().contains(PerfilUsuario.ROLE_CLIENTE))) {
    	        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    	}
	    }
	    
		List<Telefone> Telefones = usuario.getTelefones().stream().collect(Collectors.toList());
		if (Telefones.isEmpty()) {
			ResponseEntity<List<Telefone>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(Telefones);
			ResponseEntity<List<Telefone>> resposta = new ResponseEntity<>(Telefones, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
	@GetMapping("/empresa/{id}")
	public ResponseEntity<List<Telefone>> obterEmpresaTelefone(@PathVariable long id) {
		List<Empresa> empresas = empresaRepositorio.findAll();
		Empresa empresa = empresaSelecionador.selecionar(empresas, id);
		List<Telefone> Telefones = empresa.getTelefones().stream().collect(Collectors.toList());
		if (Telefones.isEmpty()) {
			ResponseEntity<List<Telefone>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(Telefones);
			ResponseEntity<List<Telefone>> resposta = new ResponseEntity<>(Telefones, HttpStatus.FOUND);
			return resposta;
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@PostMapping("/cadastro/usuario/{id}")
	public ResponseEntity<Telefone> cadastrarUsuarioTelefone(@RequestBody List<Telefone> telefone, @PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
	    	if (!(usuario.getPerfis().contains(PerfilUsuario.ROLE_CLIENTE))) {
    	        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    	}
	    }
		telefoneCadastrador.cadastrar(usuario.getTelefones(), telefone);
		usuarioRepositorio.save(usuario);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
	@PostMapping("/cadastro/empresa/{id}")
	public void cadastrarEmpresaTelefone(@RequestBody List<Telefone> telefone, @PathVariable long id) {
		Empresa empresa = empresaRepositorio.findById(id).get();
		telefoneCadastrador.cadastrar(empresa.getTelefones(), telefone);
		empresaRepositorio.save(empresa);
	}

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE', 'VENDEDOR')")
	@PutMapping("/atualizar/usuario/{id}")
	public ResponseEntity<Telefone> atualizarUsuarioTelefone(@RequestBody List<Telefone> atualizacao, @PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
	    	if (!(usuario.getPerfis().contains(PerfilUsuario.ROLE_CLIENTE))) {
    	        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    	}
	    }
		TelefoneAtualizador atualizador = new TelefoneAtualizador();
		atualizador.atualizar(usuario.getTelefones(), atualizacao);
		usuarioRepositorio.save(usuario);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN')")
	@PutMapping("/atualizar/empresa/{id}")
	public void atualizarEmpresaTelefone(@RequestBody List<Telefone> atualizacao, @PathVariable long id) {
		List<Empresa> empresas = empresaRepositorio.findAll();
		Empresa empresa = empresaSelecionador.selecionar(empresas, id);
		TelefoneAtualizador atualizador = new TelefoneAtualizador();
		atualizador.atualizar(empresa.getTelefones(), atualizacao);
		empresaRepositorio.save(empresa);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
	@PutMapping("/atualizar/{id}")
	public void atualizarTelefone(@RequestBody Telefone atualizacao, @PathVariable long id) {
		List<Telefone> telefones = repositorio.findAll();
		Telefone telefone = selecionador.selecionar(telefones, id);
		TelefoneAtualizador atualizador = new TelefoneAtualizador();
		atualizador.atualizar(telefone, atualizacao);
		repositorio.save(telefone);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@DeleteMapping("/excluir/usuario/{id}")
	public ResponseEntity<Telefone> excluirUsuarioTelefone(@RequestBody List<Telefone> telefones, @PathVariable long id, Authentication authentication) {		
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
	    	if (!(usuario.getPerfis().contains(PerfilUsuario.ROLE_CLIENTE))) {
    	        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    	}
	    }
	    
		TelefoneExcluidor excluidor = new TelefoneExcluidor();
		excluidor.excluir(usuario, telefones);
		usuarioRepositorio.save(usuario);
        return new ResponseEntity<>(HttpStatus.OK);
	}
	
	public ResponseEntity<List<Telefone>> obterTelefones() {
	    List<Telefone>telefones = repositorio.findAll();
	    
	    if (telefones.isEmpty()) {
			ResponseEntity<List<Telefone>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(telefones);
			ResponseEntity<List<Telefone>> resposta = new ResponseEntity<>(telefones, HttpStatus.FOUND);
			return resposta;
		}
	}
}
