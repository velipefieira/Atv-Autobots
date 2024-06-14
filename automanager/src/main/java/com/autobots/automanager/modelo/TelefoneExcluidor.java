package com.autobots.automanager.modelo;

import java.util.List;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Telefone;

public class TelefoneExcluidor {
		
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void excluir(Cliente cliente, Telefone telefone) {
		if (telefone != null) {
			if (
					!verificador.verificar(telefone.getDdd()) && 
					!verificador.verificar(telefone.getNumero())) {
				cliente.getTelefones().remove(telefone);
			}
		}
	}
		
	public void excluir(Cliente cliente, List<Telefone> telefones) {
		for (Telefone telefone : telefones) {
			if (telefone.getId() != null) {
				excluir(cliente, telefone);
			}
		}
	}
}
