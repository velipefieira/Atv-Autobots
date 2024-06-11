package com.autobots.automanager.controles;

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

import com.autobots.automanager.adicionadores.AdicionadorLinkVeiculo;
import com.autobots.automanager.atualizadores.VeiculoAtualizador;
import com.autobots.automanager.cadastradores.VeiculoCadastrador;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.excluidores.VeiculoExcluidor;
import com.autobots.automanager.modelos.VeiculoDto;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.selecionadores.UsuarioSelecionador;
import com.autobots.automanager.selecionadores.VeiculoSelecionador;
import com.autobots.automanager.repositorios.VeiculoRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@RestController
@RequestMapping("/veiculo")
public class VeiculoControle {

	@Autowired
	private AdicionadorLinkVeiculo adicionadorLink;

	@Autowired
	private VeiculoRepositorio repositorio;

	@Autowired
	private UsuarioRepositorio usuarioRepositorio;

	@Autowired
	private VeiculoSelecionador selecionador;
	
	@Autowired
	private UsuarioSelecionador usuarioSelecionador;
	
	@Autowired
	private VeiculoCadastrador cadastrador;
	
	@Autowired
	private VeiculoExcluidor excluidor;

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
	@GetMapping("/{id}")
	public ResponseEntity<Veiculo> obterVeiculo(@PathVariable long id, Authentication authentication) {
		List<Veiculo> Veiculos = repositorio.findAll();
		Veiculo veiculo = selecionador.selecionar(Veiculos, id);

		String username = authentication.getName();
	    List<Usuario> usuarios = usuarioRepositorio.findAll();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
	    	if (!(veiculo.getProprietario().getPerfis().contains(PerfilUsuario.ROLE_CLIENTE))) {
    	        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    	}
	    }
	    
		if (veiculo == null) {
			ResponseEntity<Veiculo> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(veiculo);
			ResponseEntity<Veiculo> resposta = new ResponseEntity<Veiculo>(veiculo, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
	@GetMapping("/veiculos")
	public ResponseEntity<List<Veiculo>> obterVeiculos() {
		List<Veiculo> Veiculos = repositorio.findAll();
		if (Veiculos.isEmpty()) {
			ResponseEntity<List<Veiculo>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(Veiculos);
			ResponseEntity<List<Veiculo>> resposta = new ResponseEntity<>(Veiculos, HttpStatus.FOUND);
			return resposta;
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
	@GetMapping("/veiculo/{id}")
	public ResponseEntity<List<Veiculo>> obterusuarioVeiculo(@PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
	    	if (!(usuario.getPerfis().contains(PerfilUsuario.ROLE_CLIENTE))) {
    	        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    	}
	    }
	    
		List<Veiculo> Veiculos = usuario.getVeiculos().stream().collect(Collectors.toList());
		if (Veiculos.isEmpty()) {
			ResponseEntity<List<Veiculo>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(Veiculos);
			ResponseEntity<List<Veiculo>> resposta = new ResponseEntity<>(Veiculos, HttpStatus.FOUND);
			return resposta;
		}
	}
	
	@PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
	@PostMapping("/cadastro/{id}")
	public ResponseEntity<Veiculo> cadastrarVeiculo(@RequestBody List<VeiculoDto> veiculos, @PathVariable long id, Authentication authentication) {
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
	    	if (!(usuario.getPerfis().contains(PerfilUsuario.ROLE_CLIENTE))) {
    	        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    	}
	    }
		
		cadastrador.cadastrar(usuario, veiculos);
		usuarioRepositorio.save(usuario);
        return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
	@PutMapping("/atualizar/{id}")
	public ResponseEntity<Veiculo> atualizarVeiculo(@RequestBody VeiculoDto atualizacao, @PathVariable long id, Authentication authentication) {
		Veiculo veiculo = repositorio.findById(id).get();
		

		String username = authentication.getName();
	    List<Usuario> usuarios = usuarioRepositorio.findAll();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
	    	if (!(veiculo.getProprietario().getPerfis().contains(PerfilUsuario.ROLE_CLIENTE))) {
    	        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    	}
	    }
		VeiculoAtualizador atualizador = new VeiculoAtualizador();
		atualizador.atualizar(veiculo, atualizacao);
		repositorio.save(veiculo);
		return new ResponseEntity<Veiculo>(HttpStatus.OK);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
	@DeleteMapping("/excluir/{id}")
	public ResponseEntity<Veiculo> excluirVeiculo(@PathVariable long id, Authentication authentication) {
		Veiculo veiculo = repositorio.findById(id).get();
		List<Usuario> usuarios = usuarioRepositorio.findAll();
		Usuario usuario = usuarioSelecionador.selecionar(usuarios, id);
		
		String username = authentication.getName();
	    Usuario usuarioLogado = usuarioSelecionador.selecionadorPorUsername(usuarios, username);
	    if (usuarioLogado.getPerfis().contains(PerfilUsuario.ROLE_VENDEDOR)) {
	    	if (!(usuario.getPerfis().contains(PerfilUsuario.ROLE_CLIENTE))) {
    	        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    	}
	    }
	    
		excluidor.excluir(usuario, veiculo);
		repositorio.delete(veiculo);
        return new ResponseEntity<>(HttpStatus.OK);
	}
	
	public ResponseEntity<Veiculo> obterVeiculo(@PathVariable long id) {
		List<Veiculo> Veiculos = repositorio.findAll();
		Veiculo veiculo = selecionador.selecionar(Veiculos, id);
	    
		if (veiculo == null) {
			ResponseEntity<Veiculo> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} else {
			adicionadorLink.adicionarLink(veiculo);
			ResponseEntity<Veiculo> resposta = new ResponseEntity<Veiculo>(veiculo, HttpStatus.FOUND);
			return resposta;
		}
	}
}
