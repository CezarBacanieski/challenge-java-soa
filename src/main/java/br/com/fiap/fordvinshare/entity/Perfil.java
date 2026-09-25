package br.com.fiap.fordvinshare.entity;

/**
 * Perfis de acesso da API.
 * ADMIN: equipe Ford, acesso total.
 * GESTOR: gestor de pós-venda de uma concessionária, vê indicadores apenas da própria concessionária.
 * CONSULTOR: consultor de serviços, cadastra clientes, veículos, manutenções e trabalha os leads.
 */
public enum Perfil {
	ADMIN,
	GESTOR,
	CONSULTOR
}
