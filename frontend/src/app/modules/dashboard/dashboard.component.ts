import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <div class="dashboard">
      <div class="dashboard-header">
        <h1>Sistema de Encuestas</h1>
        <div class="user-info">
          <span>{{ user?.nombre }}</span>
          <button (click)="logout()">Cerrar sesión</button>
        </div>
      </div>
      <div class="dashboard-body">
        <p>Bienvenido, <strong>{{ user?.nombre }}</strong>. El sistema está listo.</p>
      </div>
    </div>
  `,
  styles: [`
    .dashboard { font-family: 'Segoe UI', system-ui, sans-serif; min-height: 100vh; background: #f4f5f7; }
    .dashboard-header { background: #fff; padding: 16px 32px; border-bottom: 1px solid #e5e7eb; display: flex; justify-content: space-between; align-items: center; }
    .dashboard-header h1 { font-size: 1.2rem; font-weight: 700; color: #111827; }
    .user-info { display: flex; align-items: center; gap: 16px; color: #6b7280; font-size: 0.9rem; }
    .user-info button { padding: 7px 16px; background: #4f46e5; color: #fff; border: none; border-radius: 7px; cursor: pointer; font-size: 0.85rem; font-weight: 600; }
    .user-info button:hover { background: #4338ca; }
    .dashboard-body { padding: 40px 32px; color: #374151; }
  `]
})
export class DashboardComponent {
  user: { nombre: string; email: string } | null = null;

  constructor(private authService: AuthService, private router: Router) {
    this.user = this.authService.getUser();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
