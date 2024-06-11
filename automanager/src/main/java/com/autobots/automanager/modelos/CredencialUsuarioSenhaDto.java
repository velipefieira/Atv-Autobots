package com.autobots.automanager.modelos;

import java.io.Serializable;

import lombok.Data;

@Data
public class CredencialUsuarioSenhaDto implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String nomeUsuario;
	private String senha;
}