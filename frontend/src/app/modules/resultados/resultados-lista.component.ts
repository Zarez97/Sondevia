import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { EncuestaService, Encuesta } from '../../core/services/encuesta.service';

@Component({
  selector: 'app-resultados-lista',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './resultados-lista.component.html',
  styleUrl: './resultados-lista.component.css'
})
export class ResultadosListaComponent implements OnInit {
  encuestas: Encuesta[] = [];
  cargando = true;
  error = '';

  constructor(
    private encuestaService: EncuestaService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.encuestaService.listar().subscribe({
      next: (data) => {
        // Solo encuestas publicadas o cerradas tienen resultados
        this.encuestas = data.filter(e => e.estadoEncuesta === 2 || e.estadoEncuesta === 3);
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'No se pudieron cargar las encuestas.';
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  verResultados(e: Encuesta): void {
    this.router.navigate(['/dashboard/encuestas', e.idEncuesta, 'resultados']);
  }

  estadoClass(estado: number): string {
    return { 2: 'badge-publicada', 3: 'badge-cerrada' }[estado] ?? '';
  }
}
