package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
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

	private EntityModel<Endereco> montarModelo(long clienteId, Endereco endereco) {
		Link self = linkTo(methodOn(EnderecoControle.class).obterEndereco(clienteId)).withSelfRel();
		Link cliente = linkTo(methodOn(ClienteControle.class).obterCliente(clienteId)).withRel("cliente");
		return EntityModel.of(endereco, self, cliente);
	}

	@GetMapping
	public ResponseEntity<EntityModel<Endereco>> obterEndereco(@PathVariable long clienteId) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		if (cliente.getEndereco() == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(montarModelo(clienteId, cliente.getEndereco()));
	}

	@PostMapping("/cadastro")
	public ResponseEntity<EntityModel<Endereco>> cadastrarEndereco(@PathVariable long clienteId,
			@RequestBody Endereco endereco) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		cliente.setEndereco(endereco);
		clienteRepositorio.save(cliente);
		EntityModel<Endereco> modelo = montarModelo(clienteId, endereco);
		return ResponseEntity.status(HttpStatus.CREATED)
				.location(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<EntityModel<Endereco>> atualizarEndereco(@PathVariable long clienteId,
			@RequestBody Endereco atualizacao) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		if (cliente.getEndereco() == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não possui endereço cadastrado");
		}
		EnderecoAtualizador atualizador = new EnderecoAtualizador();
		atualizador.atualizar(cliente.getEndereco(), atualizacao);
		clienteRepositorio.save(cliente);
		return ResponseEntity.ok(montarModelo(clienteId, cliente.getEndereco()));
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
