package com.autobots.automanager.modelo;

import java.util.List;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Documento;

public class DocumentoExcluidor {
	
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void excluir(Cliente cliente, Documento documento) {
		if (documento != null) {
			if (
					!verificador.verificar(documento.getTipo()) && 
					!verificador.verificar(documento.getNumero())) {
				cliente.getDocumentos().remove(documento);
			}
		}
	}
	
	public void excluir(Cliente cliente, List<Documento> documentos) {
		for (Documento documentoExcluido : documentos) {
			if (documentoExcluido.getId() != null) {
				excluir(cliente, documentoExcluido);
			}
		}
	}
}
