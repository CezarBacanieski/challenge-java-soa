package br.com.fiap.fordvinshare.config;

import br.com.fiap.fordvinshare.entity.Concessionaria;
import br.com.fiap.fordvinshare.entity.Perfil;
import br.com.fiap.fordvinshare.entity.Usuario;
import br.com.fiap.fordvinshare.repository.ConcessionariaRepository;
import br.com.fiap.fordvinshare.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria concessionárias e usuários de teste na primeira execução (banco vazio),
 * para que a API possa ser avaliada sem cadastro manual.
 * Desative em produção com APP_SEED_ENABLED=false.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

	private final UsuarioRepository usuarioRepository;
	private final ConcessionariaRepository concessionariaRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.seed.enabled:true}")
	private boolean seedHabilitado;

	@Override
	@Transactional
	public void run(String... args) {
		if (!seedHabilitado || usuarioRepository.count() > 0) {
			return;
		}

		Concessionaria centro = concessionariaRepository.save(Concessionaria.builder()
				.nome("Ford Centro SP").cnpj("11222333000181").cidade("São Paulo").uf("SP").build());
		Concessionaria campinas = concessionariaRepository.save(Concessionaria.builder()
				.nome("Ford Campinas").cnpj("44555666000172").cidade("Campinas").uf("SP").build());

		criarUsuario("Administrador Ford", "admin@ford.com", "Admin@123", Perfil.ADMIN, null);
		criarUsuario("Gestor Centro SP", "gestor@ford.com", "Gestor@123", Perfil.GESTOR, centro);
		criarUsuario("Consultor Centro SP", "consultor@ford.com", "Consultor@123", Perfil.CONSULTOR, centro);
		criarUsuario("Gestor Campinas", "gestor.campinas@ford.com", "Gestor@123", Perfil.GESTOR, campinas);

		log.info("Carga inicial criada: 2 concessionárias e 4 usuários de teste (ver README).");
	}

	private void criarUsuario(String nome, String email, String senha, Perfil perfil, Concessionaria concessionaria) {
		usuarioRepository.save(Usuario.builder()
				.nome(nome)
				.email(email)
				.senha(passwordEncoder.encode(senha))
				.perfil(perfil)
				.concessionaria(concessionaria)
				.ativo(true)
				.build());
	}
}
