package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.modelo.TelefoneAtualizador;
import com.autobots.automanager.modelo.TelefoneSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.TelefoneRepositorio;

@RestController
@RequestMapping("/cliente/{clienteId}/telefone")
public class TelefoneControle {
	private final TelefoneRepositorio repositorio;
	private final ClienteRepositorio clienteRepositorio;
	private final TelefoneSelecionador selecionador;

	public TelefoneControle(TelefoneRepositorio repositorio, ClienteRepositorio clienteRepositorio,
			TelefoneSelecionador selecionador) {
		this.repositorio = repositorio;
		this.clienteRepositorio = clienteRepositorio;
		this.selecionador = selecionador;
	}

	private Cliente obterClienteOuFalhar(long clienteId) {
		return clienteRepositorio.findById(clienteId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Telefone> obterTelefone(@PathVariable long clienteId, @PathVariable long id) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		Telefone telefone = selecionador.selecionar(cliente.getTelefones(), id);
		if (telefone == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(telefone);
	}

	@GetMapping("/lista")
	public ResponseEntity<List<Telefone>> obterTelefones(@PathVariable long clienteId) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		return ResponseEntity.ok(cliente.getTelefones());
	}

	@PostMapping("/cadastro")
	public ResponseEntity<Telefone> cadastrarTelefone(@PathVariable long clienteId,
			@RequestBody Telefone telefone) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		cliente.getTelefones().add(telefone);
		clienteRepositorio.save(cliente);
		return ResponseEntity.status(HttpStatus.CREATED).body(telefone);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<Telefone> atualizarTelefone(@PathVariable long clienteId,
			@RequestBody Telefone atualizacao) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		Telefone telefone = selecionador.selecionar(cliente.getTelefones(), atualizacao.getId());
		if (telefone == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Telefone não encontrado para este cliente");
		}
		TelefoneAtualizador atualizador = new TelefoneAtualizador();
		atualizador.atualizar(telefone, atualizacao);
		clienteRepositorio.save(cliente);
		return ResponseEntity.ok(telefone);
	}

	@DeleteMapping("/excluir/{id}")
	public ResponseEntity<Void> excluirTelefone(@PathVariable long clienteId, @PathVariable long id) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		Telefone telefone = selecionador.selecionar(cliente.getTelefones(), id);
		if (telefone == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Telefone não encontrado para este cliente");
		}
		cliente.getTelefones().remove(telefone);
		clienteRepositorio.save(cliente);
		return ResponseEntity.noContent().build();
	}
}
