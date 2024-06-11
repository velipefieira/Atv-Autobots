package com.autobots.automanager.controles;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

import com.autobots.automanager.adicionadores.AdicionadorLinkEmail;
import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.excluidores.EmailExcluidor;
import com.autobots.automanager.modelos.VerificadorPermissao;
import com.autobots.automanager.atualizadores.EmailAtualizador;
import com.autobots.automanager.cadastradores.EmailCadastrador;
import com.autobots.automanager.selecionadores.EmailSelecionador;
import com.autobots.automanager.selecionadores.UsuarioSelecionador;
import com.autobots.automanager.repositorios.EmailRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@RestController
@RequestMapping("/email")
public class EmailControle {

	@Autowired
	private AdicionadorLinkEmail adicionadorLink;

	@Autowired
	private EmailRepositorio repositorio;

	@Autowired
	private UsuarioRepositorio usuarioRepositorio;

	@Autowired
	private EmailSelecionador selecionador;
	
	@Autowired
	private UsuarioSelecionador usuarioSelecionador;
	
	@Autowired
	private EmailCadastrador cadastrador;
	
	@Autowired
	private VerificadorPermissao verificadorPermissao;

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@GetMapping("/{id}")
	public ResponseEntity<Email> obterEmail(@PathVariable long id) {
		List<Email> emails = repositorio.findAll();
		Email email = selecionador.selecionar(emails, id);
		if (email == null) {
			ResponseEntity<Email> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(email);
			ResponseEntity<Email> resposta = new ResponseEntity<Email>(email, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/emails")
	public ResponseEntity<List<Email>> obterEmails(Authentication authentication) {
	    String username = authentication.getName();
	    
	    List<Usuario> usuarios = usuarioRepositorio.findAll();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    List<Email> emails = new ArrayList<Email>();
	    
	    if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_ADMIN) || usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_GERENTE)) {
	        emails = repositorio.findAll();
	    } else if(usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
	        emails = usuarioLogado.getEmails().stream().collect(Collectors.toList());
	    } else if(usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_CLIENTE)) {
	        emails = usuarioLogado.getEmails().stream().collect(Collectors.toList());
	    }
	    
		if (emails.isEmpty()) {
			ResponseEntity<List<Email>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(emails);
			ResponseEntity<List<Email>> resposta = new ResponseEntity<>(emails, HttpStatus.FOUND);
			return resposta;
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/usuario/{id}")
	public ResponseEntity<List<Email>> obterusuarioEmail(@PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }
	    
		List<Email> emails = usuario.getEmails().stream().collect(Collectors.toList());
		if (emails.isEmpty()) {
			ResponseEntity<List<Email>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(emails);
			ResponseEntity<List<Email>> resposta = new ResponseEntity<>(emails, HttpStatus.FOUND);
			return resposta;
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@PostMapping("/cadastro/{id}")
	public ResponseEntity<Email> cadastrarEmail(@RequestBody List<Email> email, @PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }
	    
		cadastrador.cadastrar(usuario, email);
		usuarioRepositorio.save(usuario);
        return new ResponseEntity<>(HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@PutMapping("/atualizar/usuario/{id}")
	public ResponseEntity<Email> atualizarusuarioEmail(@RequestBody List<Email> atualizacao, @PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }
	    
	    Set<Email> emails = usuario.getEmails();
		EmailAtualizador atualizador = new EmailAtualizador();
		atualizador.atualizar(emails, atualizacao);
		usuarioRepositorio.save(usuario);
        return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@PutMapping("/atualizar/{id}")
	public void atualizarEmail(@RequestBody Email atualizacao, @PathVariable long id) {
		List<Email> emails = repositorio.findAll();
		Email email = selecionador.selecionar(emails, id);
		EmailAtualizador atualizador = new EmailAtualizador();
		atualizador.atualizar(email, atualizacao);
		repositorio.save(email);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@DeleteMapping("/excluir/usuario/{id}")
	public ResponseEntity<Email> excluirUsuarioEmail(@RequestBody List<Email> emails, @PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }
	    
		EmailExcluidor excluidor = new EmailExcluidor();
		excluidor.excluir(usuario, emails);
		usuarioRepositorio.save(usuario);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	public ResponseEntity<List<Email>> obterEmails() {
	    List<Email> emails = repositorio.findAll();
		if (emails.isEmpty()) {
			ResponseEntity<List<Email>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(emails);
			ResponseEntity<List<Email>> resposta = new ResponseEntity<>(emails, HttpStatus.FOUND);
			return resposta;
		}
	}
}
