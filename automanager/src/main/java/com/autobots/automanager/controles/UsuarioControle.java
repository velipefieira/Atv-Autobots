package com.autobots.automanager.controles;

import java.util.ArrayList;
import java.util.List;

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

import com.autobots.automanager.adicionadores.AdicionadorLinkUsuario;
import com.autobots.automanager.atualizadores.EnderecoAtualizador;
import com.autobots.automanager.atualizadores.UsuarioAtualizador;
import com.autobots.automanager.cadastradores.UsuarioCadastrador;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.excluidores.UsuarioExcluidor;
import com.autobots.automanager.modelos.UsuarioDto;
import com.autobots.automanager.modelos.VerificadorPermissao;
import com.autobots.automanager.selecionadores.EmpresaSelecionador;
import com.autobots.automanager.selecionadores.UsuarioSelecionador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@RestController
@RequestMapping("/usuario")
public class UsuarioControle {
	
	@Autowired
	private UsuarioRepositorio repositorio;
	
	@Autowired
	private AdicionadorLinkUsuario adicionadorLink;
	
	@Autowired
	private UsuarioSelecionador selecionador;
	
	@Autowired
	private UsuarioExcluidor usuarioExcluidor;
	
	@Autowired
	private UsuarioAtualizador atualizador;
	
	@Autowired
	private EmpresaRepositorio empresaRepositorio;
	
	@Autowired 
	private EmpresaSelecionador empresaSelecionador;
	
	@Autowired
	private VerificadorPermissao verificadorPermissao;

	
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@GetMapping("/{id}")
	public ResponseEntity<Usuario> obterUsuario(@PathVariable long id) {
		List<Usuario> usuarios = repositorio.findAll();
		Usuario usuario = selecionador.selecionar(usuarios, id);
		
		if (usuario == null) {
			ResponseEntity<Usuario> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(usuario);
			ResponseEntity<Usuario> resposta = new ResponseEntity<Usuario>(usuario, HttpStatus.FOUND);
			return resposta;
		}
	}
	

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/usuarios")
	public ResponseEntity<List<Usuario>> obterUsuarios(Authentication authentication) {
		String username = authentication.getName();
		
		List<Usuario> usuarios = repositorio.findAll();
		Usuario usuarioLogado = selecionador.selecionadorPorUsername(usuarios, username);
				
		if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_GERENTE)) {
			usuarios = selecionador.selecionarPorCargo(usuarios, PerfilUsuario.ROLE_GERENTE);
		} else if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
			usuarios = selecionador.selecionarPorCargo(usuarios, PerfilUsuario.ROLE_VENDEDOR);
		} else if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_CLIENTE)) {
			usuarios = new ArrayList<Usuario>();
			usuarios.add(usuarioLogado);
		}
		if (usuarios.isEmpty()) {
			ResponseEntity<List<Usuario>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(usuarios);
			ResponseEntity<List<Usuario>> resposta = new ResponseEntity<>(usuarios, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
	@PostMapping("/cadastro")
	public ResponseEntity<Usuario> cadastrarUsuario(@RequestBody UsuarioDto usuario, Authentication authentication) {
		List<Usuario> usuarios = repositorio.findAll();

		String username = authentication.getName();
	    Usuario usuarioLogado = selecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }
	    
		UsuarioCadastrador cadastrador = new UsuarioCadastrador();
		Usuario novoUsuario = cadastrador.cadastrar(usuario);
		repositorio.save(novoUsuario);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@PostMapping("/cadastro/empresa/{id}")
	public ResponseEntity<Usuario> cadastrarUsuarioEmpresa(@RequestBody UsuarioDto usuario, @PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = repositorio.findAll();

		String username = authentication.getName();
	    Usuario usuarioLogado = selecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }
		
		List<Empresa> empresas = empresaRepositorio.findAll();
		Empresa empresa = empresaSelecionador.selecionar(empresas, id);
		UsuarioCadastrador cadastrador = new UsuarioCadastrador();
		Usuario novoUsuario = cadastrador.cadastrar(usuario);
		empresa.getUsuarios().add(novoUsuario);
		empresaRepositorio.save(empresa);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@PutMapping("/atualizar/{id}")
	public ResponseEntity<Usuario> atualizarUsuario(@RequestBody UsuarioDto atualizacao, @PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = repositorio.findAll();

		String username = authentication.getName();
	    Usuario usuarioLogado = selecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),atualizacao.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }
	    
		Usuario usuario = repositorio.findById(id).get();
		atualizador.atualizar(usuario, atualizacao);
		EnderecoAtualizador enderecoAtualizador = new EnderecoAtualizador();
		if (usuario.getEndereco() == null) {
			Endereco end = new Endereco();
			usuario.setEndereco(end);
		}
		enderecoAtualizador.atualizar(usuario.getEndereco(), atualizacao.getEndereco());
		repositorio.save(usuario);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@DeleteMapping("/excluir/{id}")
	public ResponseEntity<Usuario> excluirUsuario(@PathVariable long id, Authentication authentication) {
		Usuario usuario = repositorio.findById(id).get();
		List<Usuario> usuarios = repositorio.findAll();

		String username = authentication.getName();
	    Usuario usuarioLogado = selecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }
		usuarioExcluidor.excluir(usuario);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	public ResponseEntity<List<Usuario>> obterUsuarios() {
		List<Usuario> usuarios = repositorio.findAll();
		if (usuarios.isEmpty()) {
			ResponseEntity<List<Usuario>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(usuarios);
			ResponseEntity<List<Usuario>> resposta = new ResponseEntity<>(usuarios, HttpStatus.FOUND);
			return resposta;
		}
	}
}
