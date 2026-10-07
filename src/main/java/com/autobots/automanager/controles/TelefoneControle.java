package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.hateoas.CollectionModel;
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

	private EntityModel<Telefone> montarModelo(long clienteId, Telefone telefone) {
		Link self = linkTo(methodOn(TelefoneControle.class).obterTelefone(clienteId, telefone.getId()))
				.withSelfRel();
		Link lista = linkTo(methodOn(TelefoneControle.class).obterTelefones(clienteId)).withRel("telefones");
		Link cliente = linkTo(methodOn(ClienteControle.class).obterCliente(clienteId)).withRel("cliente");
		return EntityModel.of(telefone, self, lista, cliente);
	}

	@GetMapping("/{id}")
	public ResponseEntity<EntityModel<Telefone>> obterTelefone(@PathVariable long clienteId, @PathVariable long id) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		Telefone telefone = selecionador.selecionar(cliente.getTelefones(), id);
		if (telefone == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(montarModelo(clienteId, telefone));
	}

	@GetMapping("/lista")
	public ResponseEntity<CollectionModel<EntityModel<Telefone>>> obterTelefones(@PathVariable long clienteId) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		List<EntityModel<Telefone>> modelos = cliente.getTelefones().stream()
				.map(telefone -> montarModelo(clienteId, telefone)).toList();
		Link self = linkTo(methodOn(TelefoneControle.class).obterTelefones(clienteId)).withSelfRel();
		Link clienteLink = linkTo(methodOn(ClienteControle.class).obterCliente(clienteId)).withRel("cliente");
		return ResponseEntity.ok(CollectionModel.of(modelos, self, clienteLink));
	}

	@PostMapping("/cadastro")
	public ResponseEntity<EntityModel<Telefone>> cadastrarTelefone(@PathVariable long clienteId,
			@RequestBody Telefone telefone) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		cliente.getTelefones().add(telefone);
		clienteRepositorio.save(cliente);
		EntityModel<Telefone> modelo = montarModelo(clienteId, telefone);
		return ResponseEntity.status(HttpStatus.CREATED)
				.location(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<EntityModel<Telefone>> atualizarTelefone(@PathVariable long clienteId,
			@RequestBody Telefone atualizacao) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		Telefone telefone = selecionador.selecionar(cliente.getTelefones(), atualizacao.getId());
		if (telefone == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Telefone não encontrado para este cliente");
		}
		TelefoneAtualizador atualizador = new TelefoneAtualizador();
		atualizador.atualizar(telefone, atualizacao);
		clienteRepositorio.save(cliente);
		return ResponseEntity.ok(montarModelo(clienteId, telefone));
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
