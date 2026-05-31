import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { MenuService, MenuItem } from '../../core/services/menu.service';

const ICONOS: Record<string, string> = {
  'Gestionar Usuarios':    'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 7a4 4 0 1 0 0-8 4 4 0 0 0 0 8zm8 4v6m3-3h-6',
  'Asignar Roles':         'M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z',
  'Gestionar Privilegios': 'M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0 3 3L22 7l-3-3m-3.5 3.5L19 4',
  'Desbloquear Usuarios':  'M8 11V7a4 4 0 0 1 8 0m-4 8v2m-6 4h12a2 2 0 0 0 2-2v-6a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2z',
  'Gestionar Encuestas':   'M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2M9 5a2 2 0 0 1 4 0M9 12h6M9 16h4',
  'Responder Encuestas':   'M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z',
  'Ver Resultados':        'M18 20V10M12 20V4M6 20v-6',
};

const ICONO_DEFAULT = 'M4 6h16M4 12h16M4 18h16';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  menuItems: MenuItem[] = [];
  user: { nombre: string; email: string } | null = null;
  sidebarColapsado = false;

  constructor(
    private authService: AuthService,
    private menuService: MenuService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.user = this.authService.getUser();
  }

  ngOnInit(): void {
    this.menuService.obtenerMenu().subscribe({
      next: (items) => { this.menuItems = items; this.cdr.detectChanges(); },
      error: () => this.logout()
    });
  }

  getIcono(nombre: string): string {
    return ICONOS[nombre] ?? ICONO_DEFAULT;
  }

  toggleSidebar(): void {
    this.sidebarColapsado = !this.sidebarColapsado;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
