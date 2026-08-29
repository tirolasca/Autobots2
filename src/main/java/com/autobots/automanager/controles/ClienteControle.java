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
import com.autobots.automanager.modelo.ClienteAtualizador;
import com.autobots.automanager.modelo.ClienteSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;

@RestController
@RequestMapping("/cliente")
public class ClienteControle {
	private final ClienteRepositorio repositorio;
	private final ClienteSelecionador selecionador;

	public ClienteControle(ClienteRepositorio repositorio, ClienteSelecionador selecionador) {
		this.repositorio = repositorio;
		this.selecionador = selecionador;
	}

	@GetMapping("/{id}")
	public ResponseEntity<Cliente> obterCliente(@PathVariable long id) {
		List<Cliente> clientes = repositorio.findAll();
		Cliente cliente = selecionador.selecionar(clientes, id);
		if (cliente == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(cliente);
	}

	@GetMapping("/lista")
	public ResponseEntity<List<Cliente>> obterClientes() {
		List<Cliente> clientes = repositorio.findAll();
		return ResponseEntity.ok(clientes);
	}

	@PostMapping("/cadastro")
	public ResponseEntity<Cliente> cadastrarCliente(@RequestBody Cliente cliente) {
		Cliente salvo = repositorio.save(cliente);
		return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<Cliente> atualizarCliente(@RequestBody Cliente atualizacao) {
		Cliente cliente = repositorio.findById(atualizacao.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
		ClienteAtualizador atualizador = new ClienteAtualizador();
		atualizador.atualizar(cliente, atualizacao);
		Cliente salvo = repositorio.save(cliente);
		return ResponseEntity.ok(salvo);
	}

	@DeleteMapping("/excluir/{id}")
	public ResponseEntity<Void> excluirCliente(@PathVariable long id) {
		Cliente cliente = repositorio.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
		repositorio.delete(cliente);
		return ResponseEntity.noContent().build();
	}
}
