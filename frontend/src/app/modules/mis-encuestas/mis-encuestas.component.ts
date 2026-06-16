import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PublicoService, MiEncuesta } from '../../core/services/publico.service';
import { SearchBarComponent } from '../../shared/search-bar/search-bar.component';
import { coincide } from '../../core/utils/search.util';

@Component({
  selector: 'app-mis-encuestas',
  standalone: true,
  imports: [CommonModule, SearchBarComponent],
  templateUrl: './mis-encuestas.component.html',
  styleUrl: './mis-encuestas.component.css'
})
export class MisEncuestasComponent implements OnInit {
  cargando = true;
  error = '';
  busqueda = '';
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

  get enProgresoFiltradas(): MiEncuesta[] {
    return this.enProgreso.filter(e => coincide(this.busqueda, e.tituloEncuesta, e.objetivoEncuesta));
  }

  get respondidasFiltradas(): MiEncuesta[] {
    return this.respondidas.filter(e => coincide(this.busqueda, e.tituloEncuesta, e.objetivoEncuesta));
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
