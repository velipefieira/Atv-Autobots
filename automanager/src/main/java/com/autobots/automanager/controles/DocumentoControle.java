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

import com.autobots.automanager.adicionadores.AdicionadorLinkDocumento;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.excluidores.DocumentoExcluidor;
import com.autobots.automanager.modelos.VerificadorPermissao;
import com.autobots.automanager.atualizadores.DocumentoAtualizador;
import com.autobots.automanager.cadastradores.DocumentoCadastrador;
import com.autobots.automanager.selecionadores.DocumentoSelecionador;
import com.autobots.automanager.selecionadores.UsuarioSelecionador;
import com.autobots.automanager.repositorios.DocumentoRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@RestController
@RequestMapping("/documento")
public class DocumentoControle {

	@Autowired
	private AdicionadorLinkDocumento adicionadorLink;

	@Autowired
	private DocumentoRepositorio repositorio;

	@Autowired
	private UsuarioRepositorio usuarioRepositorio;

	@Autowired
	private DocumentoSelecionador selecionador;
	
	@Autowired
	private UsuarioSelecionador usuarioSelecionador;
	
	@Autowired
	private DocumentoCadastrador cadastrador;
	
	@Autowired
	private VerificadorPermissao verificadorPermissao;

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
	@GetMapping("/{id}")
	public ResponseEntity<Documento> obterDocumento(@PathVariable long id) {
		List<Documento> documentos = repositorio.findAll();
		Documento documento = selecionador.selecionar(documentos, id);
		if (documento == null) {
			ResponseEntity<Documento> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(documento);
			ResponseEntity<Documento> resposta = new ResponseEntity<Documento>(documento, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/documentos")
	public ResponseEntity<List<Documento>> obterDocumentos(Authentication authentication) {
	    String username = authentication.getName();
	    
	    List<Usuario> usuarios = usuarioRepositorio.findAll();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    List<Documento> documentos = new ArrayList<Documento>();
	    
	    if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_ADMIN) || usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_GERENTE)) {
	        documentos = repositorio.findAll();
	    } else if(usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
	        documentos = usuarioLogado.getDocumentos().stream().collect(Collectors.toList());
	    } else if(usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_CLIENTE)) {
	        documentos = usuarioLogado.getDocumentos().stream().collect(Collectors.toList());
	    }
	    
		if (documentos.isEmpty()) {
			ResponseEntity<List<Documento>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(documentos);
			ResponseEntity<List<Documento>> resposta = new ResponseEntity<>(documentos, HttpStatus.FOUND);
			return resposta;
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/usuario/{id}")
	public ResponseEntity<List<Documento>> obterusuarioDocumento(@PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }
	    
		List<Documento> documentos = usuario.getDocumentos().stream().collect(Collectors.toList());
		if (documentos.isEmpty()) {
			ResponseEntity<List<Documento>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(documentos);
			ResponseEntity<List<Documento>> resposta = new ResponseEntity<>(documentos, HttpStatus.FOUND);
			return resposta;
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@PostMapping("/cadastro/{id}")
	public ResponseEntity<Documento> cadastrarDocumento(@RequestBody List<Documento> documento, @PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }
	    
		cadastrador.cadastrar(usuario, documento);
		usuarioRepositorio.save(usuario);
        return new ResponseEntity<>(HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@PutMapping("/atualizar/usuario/{id}")
	public ResponseEntity<Documento> atualizarusuarioDocumento(@RequestBody List<Documento> atualizacao, @PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }
	    
	    Set<Documento> documentos = usuario.getDocumentos();
		DocumentoAtualizador atualizador = new DocumentoAtualizador();
		atualizador.atualizar(documentos, atualizacao);
		usuarioRepositorio.save(usuario);
        return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@PutMapping("/atualizar/{id}")
	public ResponseEntity<Documento> atualizarDocumento(@RequestBody Documento atualizacao, @PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }
	    
		List<Documento> documentos = repositorio.findAll();
		Documento documento = selecionador.selecionar(documentos, id);
		DocumentoAtualizador atualizador = new DocumentoAtualizador();
		atualizador.atualizar(documento, atualizacao);
		repositorio.save(documento);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
	@DeleteMapping("/excluir/usuario/{id}")
	public ResponseEntity<Documento> excluirUsuarioDocumento(@RequestBody List<Documento> documentos, @PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    
	    boolean permissao = verificadorPermissao.verificar(usuarioLogado.getPerfis(),usuario.getPerfis());
	    
	    if (permissao == false) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }
		DocumentoExcluidor excluidor = new DocumentoExcluidor();
		excluidor.excluir(usuario, documentos);
		usuarioRepositorio.save(usuario);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	public ResponseEntity<List<Documento>> obterDocumentos() {
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
}
