package com.autobots.automanager.controles;

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

import com.autobots.automanager.adicionadores.AdicionadorLinkCredencialCodigoBarra;
import com.autobots.automanager.adicionadores.AdicionadorLinkCredencialUsuarioSenha;
import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.excluidores.CredencialExcluidor;
import com.autobots.automanager.modelos.VerificadorPermissao;
import com.autobots.automanager.atualizadores.CredencialAtualizadorCodigoBarra;
import com.autobots.automanager.atualizadores.CredencialAtualizadorUsuarioSenha;
import com.autobots.automanager.cadastradores.CredencialCodigoBarraCadastrador;
import com.autobots.automanager.cadastradores.CredencialUsuarioSenhaCadastrador;
import com.autobots.automanager.selecionadores.UsuarioSelecionador;
import com.autobots.automanager.repositorios.CredencialCodigoBarraRepositorio;
import com.autobots.automanager.repositorios.CredencialRepositorio;
import com.autobots.automanager.repositorios.CredencialUsuarioSenhaRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@RestController
@RequestMapping("/credencial")
public class CredencialControle {

	@Autowired
	private AdicionadorLinkCredencialUsuarioSenha adicionadorLinkUsuarioSenha;
	
	@Autowired
	private AdicionadorLinkCredencialCodigoBarra adicionadorLinkCodigoBarra;

	@Autowired
	private CredencialUsuarioSenhaRepositorio repositorioUsuarioSenha;
	
	@Autowired
	private CredencialCodigoBarraRepositorio repositorioCodigoBarra;
	
	@Autowired
	private CredencialRepositorio repositorio;

	@Autowired
	private UsuarioRepositorio usuarioRepositorio;
	
	@Autowired
	private UsuarioSelecionador usuarioSelecionador;
	
	@Autowired
	private VerificadorPermissao verificadorPermissao;

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/usuariosenha/{id}")
	public ResponseEntity<CredencialUsuarioSenha> obterCredencialUsuarioSenha(@PathVariable long id) {
		CredencialUsuarioSenha credencial = repositorioUsuarioSenha.findById(id).orElse(null);
		if (credencial == null) {
			ResponseEntity<CredencialUsuarioSenha> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLinkUsuarioSenha.adicionarLink(credencial);
			ResponseEntity<CredencialUsuarioSenha> resposta = new ResponseEntity<CredencialUsuarioSenha>(credencial, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/codigobarra/{id}")
	public ResponseEntity<Credencial> obterCredencialCodigoBarra(@PathVariable long id) {
		CredencialCodigoBarra credencial = repositorioCodigoBarra.findById(id).orElse(null);
		if (credencial == null) {
			ResponseEntity<Credencial> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLinkCodigoBarra.adicionarLink(credencial);
			ResponseEntity<Credencial> resposta = new ResponseEntity<Credencial>(credencial, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/usuariosenha/credenciais")
	public ResponseEntity<List<CredencialUsuarioSenha>> obterCredenciaisUsuarioSenha() {
		List<CredencialUsuarioSenha> credenciais = repositorioUsuarioSenha.findAll();
		if (credenciais.isEmpty()) {
			ResponseEntity<List<CredencialUsuarioSenha>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLinkUsuarioSenha.adicionarLink(credenciais);
			ResponseEntity<List<CredencialUsuarioSenha>> resposta = new ResponseEntity<>(credenciais, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/codigobarra/credenciais")
	public ResponseEntity<List<CredencialCodigoBarra>> obterCredenciaisCodigoBarra() {
		List<CredencialCodigoBarra> credenciais = repositorioCodigoBarra.findAll();
		if (credenciais.isEmpty()) {
			ResponseEntity<List<CredencialCodigoBarra>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLinkCodigoBarra.adicionarLink(credenciais);
			ResponseEntity<List<CredencialCodigoBarra>> resposta = new ResponseEntity<>(credenciais, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/usuarosenha/credenciais/{id}")
	public ResponseEntity<List<CredencialUsuarioSenha>> obterUsuarioCredenciaisUsuarioSenha(@PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }
	    
		List<CredencialUsuarioSenha> credenciais = usuario.getCredenciaisUsuarioSenha().stream().collect(Collectors.toList());
		if (credenciais.isEmpty()) {
			ResponseEntity<List<CredencialUsuarioSenha>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLinkUsuarioSenha.adicionarLink(credenciais);
			ResponseEntity<List<CredencialUsuarioSenha>> resposta = new ResponseEntity<>(credenciais, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/codigobarra/credenciais/{id}")
	public ResponseEntity<List<CredencialCodigoBarra>> obterUsuarioCredenciaisCodigoBarra(@PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }
		List<CredencialCodigoBarra> credenciais = usuario.getCredenciaisCodigoBarra().stream().collect(Collectors.toList());
		if (credenciais.isEmpty()) {
			ResponseEntity<List<CredencialCodigoBarra>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLinkCodigoBarra.adicionarLink(credenciais);
			ResponseEntity<List<CredencialCodigoBarra>> resposta = new ResponseEntity<>(credenciais, HttpStatus.FOUND);
			return resposta;
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@PostMapping("/cadastro/usuariosenha/{id}")
	public ResponseEntity<Credencial> cadastrarCredencialUsuarioSenha(@RequestBody List<CredencialUsuarioSenha> credencial, @PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }		CredencialUsuarioSenhaCadastrador cadastrador = new CredencialUsuarioSenhaCadastrador();
		cadastrador.cadastrar(usuario, credencial);
		usuarioRepositorio.save(usuario);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@PostMapping("/cadastro/codigobarra/{id}")
	public ResponseEntity<Credencial> cadastrarCredencialCodigoBarra(@RequestBody List<CredencialCodigoBarra> credencial, @PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }		CredencialCodigoBarraCadastrador cadastrador = new CredencialCodigoBarraCadastrador();
		cadastrador.cadastrar(usuario, credencial);
		usuarioRepositorio.save(usuario);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@PutMapping("/atualizar/usuariosenha/usuario/{id}")
	public ResponseEntity<Credencial> atualizarUsuarioCredencialUsuarioSenha(@RequestBody List<CredencialUsuarioSenha> atualizacao, @PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }
		Set<CredencialUsuarioSenha> credenciais = usuario.getCredenciaisUsuarioSenha();
		CredencialAtualizadorUsuarioSenha atualizador = new CredencialAtualizadorUsuarioSenha();
		atualizador.atualizar(credenciais, atualizacao);
		usuarioRepositorio.save(usuario);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN')")
	@PutMapping("/atualizar/usuariosenha/{id}")
	public void atualizarCredencialUsuarioSenha(@RequestBody CredencialUsuarioSenha atualizacao, @PathVariable long id) {
		CredencialUsuarioSenha credencial = repositorioUsuarioSenha.findById(id).get();
		CredencialAtualizadorUsuarioSenha atualizador = new CredencialAtualizadorUsuarioSenha();
		atualizador.atualizar(credencial, atualizacao);
		repositorio.save(credencial);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@PutMapping("/atualizar/codigobarra/usuario/{id}")
	public ResponseEntity<Credencial> atualizarUsuarioCredencialCodigoBarra(@RequestBody List<CredencialCodigoBarra> atualizacao, @PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }
	    
		Set<CredencialCodigoBarra> credenciais = usuario.getCredenciaisCodigoBarra();
		CredencialAtualizadorCodigoBarra atualizador = new CredencialAtualizadorCodigoBarra();
		atualizador.atualizar(credenciais, atualizacao);
		usuarioRepositorio.save(usuario);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN')")
	@PutMapping("/atualizar/codigobarra/{id}")
	public void atualizarCredencialCodigoBarra(@RequestBody CredencialCodigoBarra atualizacao, @PathVariable long id) {
		CredencialCodigoBarra credencial = repositorioCodigoBarra.findById(id).get();
		CredencialAtualizadorCodigoBarra atualizador = new CredencialAtualizadorCodigoBarra();
		atualizador.atualizar(credencial, atualizacao);
		repositorio.save(credencial);
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@DeleteMapping("/excluir/{id}")
	public void excluirCredencial(@PathVariable long id) {
		Credencial credencial = repositorio.findById(id).get();
		CredencialExcluidor credencialExcluidor = new CredencialExcluidor();
		credencialExcluidor.excluir(credencial, credencial);
		repositorio.save(credencial);
	}
}
