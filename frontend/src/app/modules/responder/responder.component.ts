import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { PublicoService, EncuestaPublica, ParticipanteResponse } from '../../core/services/publico.service';

type Paso = 'cargando' | 'error' | 'datos' | 'preguntas';

function noFuturo(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;
  return new Date(control.value) > new Date() ? { futuro: true } : null;
}

@Component({
  selector: 'app-responder',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './responder.component.html',
  styleUrl: './responder.component.css'
})
export class ResponderComponent implements OnInit {
  paso: Paso = 'cargando';
  token = '';
  encuesta: EncuestaPublica | null = null;
  errorCarga = '';

  form: FormGroup;
  errorForm = '';
  enviando = false;
  participante: ParticipanteResponse | null = null;
  hoy = new Date().toISOString().split('T')[0];

  constructor(
    private route: ActivatedRoute,
    private publicoService: PublicoService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(128)]],
      email: ['', [Validators.required, Validators.email]],
      fechaNacimiento: ['', [Validators.required, noFuturo]]
    });
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.paramMap.get('token') ?? '';
    this.cargar();
  }

  cargar(): void {
    this.publicoService.cargarEncuesta(this.token).subscribe({
      next: (e) => { this.encuesta = e; this.paso = 'datos'; this.cdr.detectChanges(); },
      error: (err) => {
        this.errorCarga = err.error?.mensaje || 'No se pudo cargar la encuesta.';
        this.paso = 'error';
        this.cdr.detectChanges();
      }
    });
  }

  comenzar(): void {
    if (this.form.invalid || this.enviando) { this.form.markAllAsTouched(); return; }
    this.enviando = true;
    this.errorForm = '';
    this.publicoService.participar(this.token, this.form.value).subscribe({
      next: (res) => {
        this.participante = res;
        this.enviando = false;
        this.paso = 'preguntas';
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.enviando = false;
        this.errorForm = err.error?.mensaje || 'No se pudieron registrar tus datos.';
        this.cdr.detectChanges();
      }
    });
  }

  get f() { return this.form.controls; }
}
