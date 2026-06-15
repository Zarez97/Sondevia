import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PublicoService, MiEncuesta } from '../../core/services/publico.service';

@Component({
  selector: 'app-mis-encuestas',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mis-encuestas.component.html',
  styleUrl: './mis-encuestas.component.css'
})
export class MisEncuestasComponent implements OnInit {
  cargando = true;
  error = '';
  enProgreso: MiEncuesta[] = [];
  respondidas: MiEncuesta[] = [];

  constructor(
    private publicoService: PublicoService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.publicoService.misEncuestas().subscribe({
      next: (data) => {
        this.enProgreso = data.filter(e => e.estadoRespuesta === 1);
        this.respondidas = data.filter(e => e.estadoRespuesta === 2);
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'No se pudieron cargar tus encuestas.';
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  continuar(e: MiEncuesta): void {
    this.router.navigate(['/responder', e.tokenPublico]);
  }
}
