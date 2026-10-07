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

	private EntityModel<Cliente> montarModelo(Cliente cliente) {
		Link self = linkTo(methodOn(ClienteControle.class).obterCliente(cliente.getId())).withSelfRel();
		Link lista = linkTo(methodOn(ClienteControle.class).obterClientes()).withRel("clientes");
		Link documentos = linkTo(methodOn(DocumentoControle.class).obterDocumentos(cliente.getId()))
				.withRel("documentos");
		Link endereco = linkTo(methodOn(EnderecoControle.class).obterEndereco(cliente.getId())).withRel("endereco");
		Link telefones = linkTo(methodOn(TelefoneControle.class).obterTelefones(cliente.getId()))
				.withRel("telefones");
		return EntityModel.of(cliente, self, lista, documentos, endereco, telefones);
	}

	@GetMapping("/{id}")
	public ResponseEntity<EntityModel<Cliente>> obterCliente(@PathVariable long id) {
		List<Cliente> clientes = repositorio.findAll();
		Cliente cliente = selecionador.selecionar(clientes, id);
		if (cliente == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(montarModelo(cliente));
	}

	@GetMapping("/lista")
	public ResponseEntity<CollectionModel<EntityModel<Cliente>>> obterClientes() {
		List<Cliente> clientes = repositorio.findAll();
		List<EntityModel<Cliente>> modelos = clientes.stream().map(this::montarModelo).toList();
		Link self = linkTo(methodOn(ClienteControle.class).obterClientes()).withSelfRel();
		return ResponseEntity.ok(CollectionModel.of(modelos, self));
	}

	@PostMapping("/cadastro")
	public ResponseEntity<EntityModel<Cliente>> cadastrarCliente(@RequestBody Cliente cliente) {
		Cliente salvo = repositorio.save(cliente);
		EntityModel<Cliente> modelo = montarModelo(salvo);
		return ResponseEntity.status(HttpStatus.CREATED)
				.location(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<EntityModel<Cliente>> atualizarCliente(@RequestBody Cliente atualizacao) {
		Cliente cliente = repositorio.findById(atualizacao.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
		ClienteAtualizador atualizador = new ClienteAtualizador();
		atualizador.atualizar(cliente, atualizacao);
		Cliente salvo = repositorio.save(cliente);
		return ResponseEntity.ok(montarModelo(salvo));
	}

	@DeleteMapping("/excluir/{id}")
	public ResponseEntity<Void> excluirCliente(@PathVariable long id) {
		Cliente cliente = repositorio.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
		repositorio.delete(cliente);
		return ResponseEntity.noContent().build();
	}
}
