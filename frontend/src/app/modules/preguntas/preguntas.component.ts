import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PreguntaService, Pregunta, PreguntaRequest } from '../../core/services/pregunta.service';
import { EncuestaService, Encuesta } from '../../core/services/encuesta.service';

@Component({
  selector: 'app-preguntas',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './preguntas.component.html',
  styleUrl: './preguntas.component.css'
})
export class PreguntasComponent implements OnInit {
  encuesta: Encuesta | null = null;
  preguntas: Pregunta[] = [];
  cargando = true;
  mostrarModal = false;
  mostrarPreview = false;
  editando: Pregunta | null = null;
  error = '';
  errorModal = '';
  form: FormGroup;
  idEncuesta!: number;

  tipoTextoSeleccionado: 'corta' | 'larga' = 'corta';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private preguntaService: PreguntaService,
    private encuestaService: EncuestaService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      descripcionPregunta: ['', [Validators.required, Validators.maxLength(500)]],
      obligatoriaPregunta: [false],
      tipoTexto: ['corta']
    });
  }

  ngOnInit(): void {
    this.idEncuesta = Number(this.route.snapshot.paramMap.get('idEncuesta'));
    this.cargarEncuesta();
    this.cargarPreguntas();
  }

  cargarEncuesta(): void {
    this.encuestaService.buscar(this.idEncuesta).subscribe({
      next: (e) => { this.encuesta = e; this.cdr.detectChanges(); },
      error: () => this.router.navigate(['/dashboard/encuestas'])
    });
  }

  cargarPreguntas(): void {
    this.cargando = true;
    this.preguntaService.listar(this.idEncuesta).subscribe({
      next: (data) => { this.preguntas = data; this.cargando = false; this.cdr.detectChanges(); },
      error: () => { this.error = 'Error al cargar preguntas.'; this.cargando = false; this.cdr.detectChanges(); }
    });
  }

  abrirAgregar(): void {
    this.editando = null;
    this.form.reset({ obligatoriaPregunta: false, tipoTexto: 'corta' });
    this.tipoTextoSeleccionado = 'corta';
    this.errorModal = '';
    this.mostrarModal = true;
    this.mostrarPreview = false;
  }

  abrirEditar(p: Pregunta): void {
    this.editando = p;
    this.form.patchValue({
      descripcionPregunta: p.descripcionPregunta,
      obligatoriaPregunta: p.obligatoriaPregunta,
      tipoTexto: 'corta'
    });
    this.errorModal = '';
    this.mostrarModal = true;
    this.mostrarPreview = false;
  }

  seleccionarTipoTexto(tipo: 'corta' | 'larga'): void {
    this.tipoTextoSeleccionado = tipo;
    this.form.patchValue({ tipoTexto: tipo });
  }

  guardar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    const data: PreguntaRequest = {
      descripcionPregunta: this.form.value.descripcionPregunta,
      obligatoriaPregunta: this.form.value.obligatoriaPregunta ?? false,
      tipoPregunta: 'ABIERTA'
    };

    const accion = this.editando
      ? this.preguntaService.actualizar(this.idEncuesta, this.editando.idPregunta, data)
      : this.preguntaService.agregar(this.idEncuesta, data);

    accion.subscribe({
      next: () => { this.mostrarModal = false; this.cargarPreguntas(); },
      error: (e) => { this.errorModal = e.error?.mensaje || 'Error al guardar.'; this.cdr.detectChanges(); }
    });
  }

  eliminar(p: Pregunta): void {
    if (!confirm(`¿Eliminar la pregunta "${p.descripcionPregunta}"?`)) return;
    this.preguntaService.eliminar(this.idEncuesta, p.idPregunta).subscribe({
      next: () => this.cargarPreguntas(),
      error: (e) => alert(e.error?.mensaje || 'No se pudo eliminar.')
    });
  }

  togglePreview(): void {
    this.mostrarPreview = !this.mostrarPreview;
  }

  volver(): void {
    this.router.navigate(['/dashboard/encuestas']);
  }

  cerrarModal(): void {
    this.mostrarModal = false;
  }

  get f() { return this.form.controls; }
  get enDiseno(): boolean { return this.encuesta?.estadoEncuesta === 1; }
}
