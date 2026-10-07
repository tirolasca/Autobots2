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
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.modelo.DocumentoAtualizador;
import com.autobots.automanager.modelo.DocumentoSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.DocumentoRepositorio;

@RestController
@RequestMapping("/cliente/{clienteId}/documento")
public class DocumentoControle {
	private final DocumentoRepositorio repositorio;
	private final ClienteRepositorio clienteRepositorio;
	private final DocumentoSelecionador selecionador;

	public DocumentoControle(DocumentoRepositorio repositorio, ClienteRepositorio clienteRepositorio,
			DocumentoSelecionador selecionador) {
		this.repositorio = repositorio;
		this.clienteRepositorio = clienteRepositorio;
		this.selecionador = selecionador;
	}

	private Cliente obterClienteOuFalhar(long clienteId) {
		return clienteRepositorio.findById(clienteId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
	}

	private EntityModel<Documento> montarModelo(long clienteId, Documento documento) {
		Link self = linkTo(methodOn(DocumentoControle.class).obterDocumento(clienteId, documento.getId()))
				.withSelfRel();
		Link lista = linkTo(methodOn(DocumentoControle.class).obterDocumentos(clienteId)).withRel("documentos");
		Link cliente = linkTo(methodOn(ClienteControle.class).obterCliente(clienteId)).withRel("cliente");
		return EntityModel.of(documento, self, lista, cliente);
	}

	@GetMapping("/{id}")
	public ResponseEntity<EntityModel<Documento>> obterDocumento(@PathVariable long clienteId, @PathVariable long id) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		Documento documento = selecionador.selecionar(cliente.getDocumentos(), id);
		if (documento == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(montarModelo(clienteId, documento));
	}

	@GetMapping("/lista")
	public ResponseEntity<CollectionModel<EntityModel<Documento>>> obterDocumentos(@PathVariable long clienteId) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		List<EntityModel<Documento>> modelos = cliente.getDocumentos().stream()
				.map(documento -> montarModelo(clienteId, documento)).toList();
		Link self = linkTo(methodOn(DocumentoControle.class).obterDocumentos(clienteId)).withSelfRel();
		Link clienteLink = linkTo(methodOn(ClienteControle.class).obterCliente(clienteId)).withRel("cliente");
		return ResponseEntity.ok(CollectionModel.of(modelos, self, clienteLink));
	}

	@PostMapping("/cadastro")
	public ResponseEntity<EntityModel<Documento>> cadastrarDocumento(@PathVariable long clienteId,
			@RequestBody Documento documento) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		cliente.getDocumentos().add(documento);
		clienteRepositorio.save(cliente);
		EntityModel<Documento> modelo = montarModelo(clienteId, documento);
		return ResponseEntity.status(HttpStatus.CREATED)
				.location(modelo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(modelo);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<EntityModel<Documento>> atualizarDocumento(@PathVariable long clienteId,
			@RequestBody Documento atualizacao) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		Documento documento = selecionador.selecionar(cliente.getDocumentos(), atualizacao.getId());
		if (documento == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado para este cliente");
		}
		DocumentoAtualizador atualizador = new DocumentoAtualizador();
		atualizador.atualizar(documento, atualizacao);
		clienteRepositorio.save(cliente);
		return ResponseEntity.ok(montarModelo(clienteId, documento));
	}

	@DeleteMapping("/excluir/{id}")
	public ResponseEntity<Void> excluirDocumento(@PathVariable long clienteId, @PathVariable long id) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		Documento documento = selecionador.selecionar(cliente.getDocumentos(), id);
		if (documento == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado para este cliente");
		}
		cliente.getDocumentos().remove(documento);
		clienteRepositorio.save(cliente);
		return ResponseEntity.noContent().build();
	}
}
