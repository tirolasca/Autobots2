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

	@GetMapping("/{id}")
	public ResponseEntity<Documento> obterDocumento(@PathVariable long clienteId, @PathVariable long id) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		Documento documento = selecionador.selecionar(cliente.getDocumentos(), id);
		if (documento == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(documento);
	}

	@GetMapping("/lista")
	public ResponseEntity<List<Documento>> obterDocumentos(@PathVariable long clienteId) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		return ResponseEntity.ok(cliente.getDocumentos());
	}

	@PostMapping("/cadastro")
	public ResponseEntity<Documento> cadastrarDocumento(@PathVariable long clienteId,
			@RequestBody Documento documento) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		cliente.getDocumentos().add(documento);
		clienteRepositorio.save(cliente);
		return ResponseEntity.status(HttpStatus.CREATED).body(documento);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<Documento> atualizarDocumento(@PathVariable long clienteId,
			@RequestBody Documento atualizacao) {
		Cliente cliente = obterClienteOuFalhar(clienteId);
		Documento documento = selecionador.selecionar(cliente.getDocumentos(), atualizacao.getId());
		if (documento == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado para este cliente");
		}
		DocumentoAtualizador atualizador = new DocumentoAtualizador();
		atualizador.atualizar(documento, atualizacao);
		clienteRepositorio.save(cliente);
		return ResponseEntity.ok(documento);
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
