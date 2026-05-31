import { Component } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  template: `
    <div class="home">
      <h2>Bienvenido, {{ user?.nombre }}</h2>
      <p>Selecciona una opción del menú para comenzar.</p>
    </div>
  `,
  styles: [`
    .home { padding: 40px 32px; font-family: 'Segoe UI', system-ui, sans-serif; }
    h2 { font-size: 1.5rem; font-weight: 700; color: #111827; margin-bottom: 8px; }
    p { color: #6b7280; font-size: 0.95rem; }
  `]
})
export class HomeComponent {
  user: { nombre: string; email: string } | null = null;

  constructor(private auth: AuthService) {
    this.user = this.auth.getUser();
  }
}
