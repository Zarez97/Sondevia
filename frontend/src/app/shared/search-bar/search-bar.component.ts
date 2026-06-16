import { Component, model, input } from '@angular/core';
import { FormsModule } from '@angular/forms';

/**
 * Barra de búsqueda reutilizable para filtrado dinámico (en tiempo real).
 * Uso: <app-search-bar [(valor)]="busqueda" placeholder="Buscar..." />
 */
@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.css'
})
export class SearchBarComponent {
  valor = model<string>('');
  placeholder = input<string>('Buscar...');
}
