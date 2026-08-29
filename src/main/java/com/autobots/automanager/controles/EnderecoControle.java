package com.autobots.automanager.controles;

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
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.modelo.EnderecoAtualizador;
import com.autobots.automanager.repositorios.ClienteRepositorio;

/**
 * Endereco possui relação @OneToOne com Cliente, então o CRUD é feito
 * sobre o endereço único de cada cliente (sem lista nem seleção por id).
 */
@RestController
@RequestMapping("/cliente/{clienteId}/endereco")
public class EnderecoControle {
	private final ClienteRepositorio clienteRepositorio;

	public EnderecoControle(ClienteRepositorio clienteRepositorio) {
		this.clienteRepositorio = clienteRepositorio;
	}

	private Cliente obterClienteOuFalhar(long clienteId) {
		return clienteRepositorio.findById(clienteId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
	}

	@GetMapping
	public ResponseEntity<Endereco> obterEndereco(@PathVariable long clienteId) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		if (cliente.getEndereco() == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(cliente.getEndereco());
	}

	@PostMapping("/cadastro")
	public ResponseEntity<Endereco> cadastrarEndereco(@PathVariable long clienteId,
			@RequestBody Endereco endereco) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		cliente.setEndereco(endereco);
		clienteRepositorio.save(cliente);
		return ResponseEntity.status(HttpStatus.CREATED).body(endereco);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<Endereco> atualizarEndereco(@PathVariable long clienteId,
			@RequestBody Endereco atualizacao) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		if (cliente.getEndereco() == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não possui endereço cadastrado");
		}
		EnderecoAtualizador atualizador = new EnderecoAtualizador();
		atualizador.atualizar(cliente.getEndereco(), atualizacao);
		clienteRepositorio.save(cliente);
		return ResponseEntity.ok(cliente.getEndereco());
	}

	@DeleteMapping("/excluir")
	public ResponseEntity<Void> excluirEndereco(@PathVariable long clienteId) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		if (cliente.getEndereco() == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não possui endereço cadastrado");
		}
		cliente.setEndereco(null);
		clienteRepositorio.save(cliente);
		return ResponseEntity.noContent().build();
	}
}
