package com.autobots.automanager.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entidades.Venda;

public interface VendaRepositorio extends JpaRepository<Venda, Long>{
    List<Venda> findByClienteId(long clienteId);
	List<Venda> findByVendedorId(Long id);
}
